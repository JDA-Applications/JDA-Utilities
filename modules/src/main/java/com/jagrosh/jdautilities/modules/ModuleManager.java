/*
 * Copyright 2016 John Grosh (jagrosh)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jdautilities.modules;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.modules.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A service manager for hot-loading external jars containing classes that
 * extend {@link com.jagrosh.jdautilities.command.Command Command} and serve
 * as {@link com.jagrosh.jdautilities.modules.Module Module}s.
 *
 * <p>The following is a good example of how exactly this system works:
 *
 * <pre><code>
 * directory
 *    - bot.jar
 *       - MyBot.class
 *    - commands.jar
 *       - module.conf
 *       - MyCommand.class
 * </code></pre>
 *
 * In this example, two jar files ({@code bot.jar} and {@code commands.jar}) are located
 * in the same {@code directory}.
 * <br>{@code bot.jar} contains the class {@code MyBot.class}, which for the example we
 * will assume is the main method for the jar and the main for the central runtime of the
 * bot.
 * <br>The other jar in the {@code directory}, {@code commands.jar}, contains the class
 * {@code MyCommand.class}, which we will assume extends {@code Command} in this case.
 *
 * <p>From somewhere in the main method of {@code MyBot}, the following snippet of code is
 * invoked:
 *
 * <pre><code>
 * CommandClient client = // Defined
 *
 * ModuleManager moduleManager = new ModuleManager(client);
 *
 * Module commandsModule = moduleManager.loadModule("commands");
 *
 * client.addCommand(commandsModule.getEntry("MyCommand").createInstance());
 * </code></pre>
 *
 * This code creates an instance of ModuleManager, loads the modular jar {@code commands}, and
 * creates an instance of the entry {@code MyCommand}, as well as adding that instance to the
 * {@link com.jagrosh.jdautilities.command.CommandClient CommandClient}.
 *
 * @since  2.0
 * @author Kaidan Gustave
 */
@SuppressWarnings("Convert2Lambda")
public final class ModuleManager
{
    private static final Logger LOG = LoggerFactory.getLogger(ModuleManager.class);
    private static final String MODULE_FILE_FORMAT = "%s.jar";
    private static final String MODULE_DESCRIBER_FORMAT = "module.%s";

    private final Map<String, ModuleFactory> loadedFactories;
    private final CommandClient client;
    private final Map<String, Module<?>> loadedModules;

    /**
     * Creates a new ModuleManager, wrapping the provided
     * {@link com.jagrosh.jdautilities.command.CommandClient CommandClient} for easy
     * loading and unloading {@link com.jagrosh.jdautilities.modules.Module Module}s.
     *
     * @param  client
     *         The CommandClient for the ModuleManager to wrap, never {@code null}
     */
    public ModuleManager(@Nonnull CommandClient client)
    {
        this.loadedFactories = new HashMap<>();
        this.client = client;
        this.loadedModules = new HashMap<>();

        initFactories(Thread.currentThread().getContextClassLoader());
    }

    private void initFactories(final ClassLoader loader)
    {
        final ServiceLoader<ModuleFactory> serviceLoader;
        try
        {
            serviceLoader = AccessController.doPrivileged(new PrivilegedAction<ServiceLoader<ModuleFactory>>() {
                @Override
                public ServiceLoader<ModuleFactory> run()
                {
                    return loader == null? ServiceLoader.loadInstalled(ModuleFactory.class) :
                        ServiceLoader.load(ModuleFactory.class, loader);
                }
            });
        }
        catch(ServiceConfigurationError error)
        {
            LOG.error("Failed to load ModuleFactory services!", error);
            return;
        }

        serviceLoader.forEach(factory -> {
            System.out.println(factory.getFileExtension());
            // We should not overwrite already loaded factories.
            // Instead we provide a service warning telling the developer that something is wrong.
            // This is similar to an SLF4J "no log implementation found" warning.
            if(loadedFactories.containsKey(factory.getFileExtension().toLowerCase()))
            {
                LOG.warn("ModuleManager: Failed to load ModuleFactory '" +
                         factory.getClass().getCanonicalName() + "' due to it's " +
                         "extension being reserved by another ModuleFactory already.");

                return;
            }

            loadedFactories.put(factory.getFileExtension().toLowerCase(), factory);
        });
    }

    public Module getModule(String name)
    {
        return loadedModules.get(name.toLowerCase());
    }

    public Module loadModule(String name)
    {
        File jarFile = FileUtil.getFile(String.format(MODULE_FILE_FORMAT, name));
        URLClassLoader classLoader = null;

        try
        {
            classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});

            String trueExtension = null;
            for(String extension : loadedFactories.keySet())
            {
                String describer = String.format(MODULE_DESCRIBER_FORMAT, extension);
                System.out.println(describer);
                if(classLoader.getResource(describer) != null)
                {
                    trueExtension = extension;
                    break;
                }
            }

            if(trueExtension != null)
            {
                ModuleFactory factory = loadedFactories.get(trueExtension);
                Module module = factory.load(classLoader);
                String moduleMappedName = module.getName().toLowerCase();

                // We cannot accidentally overwrite a module because this will make it
                // potentially impossible for the garbage collector to clean it up when
                // we unload. For this reason, modules must never be overwritten in the
                // event we load a module with an identical name.
                if(loadedModules.containsKey(moduleMappedName)) {
                    throw new IllegalArgumentException("Could not load module '" + name + "' because there is " +
                                                       "already a module loaded with the name '" + module.getName() + "'!");
                }

                loadedModules.put(module.getName().toLowerCase(), module);

                return module;
            }
            else
            {
                // I believe it's appropriate to throw an exception
                // at this point. If you don't fight me irl
                throw new IllegalArgumentException("Could not load module '" + name + "' because no valid " +
                                                   "ModuleFactory services were found!");
            }
        }
        catch(MalformedURLException e)
        {
            throw new ModuleException("Failed to load Module due to MalformedURL.", e);
        }
        finally
        {
            try
            {
                if(classLoader != null)
                    classLoader.close();
            }
            catch(IOException e)
            {
                LOG.error("Failed to close URLClassLoader for module: "+name, e);
            }
        }
    }

    public void unloadModule(String name)
    {
        loadedModules.values().stream()
                     .filter(module -> module.getName().equalsIgnoreCase(name))
                     .findFirst().ifPresent(module -> {
            // Remove it from the loaded modules map
            loadedModules.remove(module.getName().toLowerCase());

            // Map classes of the module to a list
            List<Class<? extends Command>> moduleContents =
                module.getCommands().stream().map(Module.Entry::getCommandClass).collect(Collectors.toList());

            //
            client.getCommands().forEach(command -> {
                // Remove if the command is part of the module.
                if(moduleContents.contains(command.getClass())) {
                    client.removeCommand(command.getName());
                }
            });
        });
    }
}
