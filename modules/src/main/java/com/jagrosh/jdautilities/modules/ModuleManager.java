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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

/**
 * @author Kaidan Gustave
 */
@SuppressWarnings("Convert2Lambda")
public class ModuleManager
{
    private static final Logger LOG = LoggerFactory.getLogger(ModuleManager.class);
    private static final String MODULE_FILE_FORMAT = "%s.jar";
    private static final String MODULE_DESCRIBER_FORMAT = "module.%s";

    private final Map<String, ModuleFactory> loadedFactories;
    private final CommandClient representedClient;
    private final Map<String, Module> loadedModules;

    public ModuleManager()
    {
        this(null);
    }

    public ModuleManager(CommandClient representedClient)
    {
        this.loadedFactories = new HashMap<>();
        this.representedClient = representedClient;
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
                    return getLoader(loader);
                }
            });
        }
        catch(ServiceConfigurationError error)
        {
            LOG.error("Failed to load ModuleFactory services!", error);
            return;
        }

        serviceLoader.forEach(factory -> loadedFactories.put(factory.getFileExtension().toLowerCase(), factory));
    }

    private ServiceLoader<ModuleFactory> getLoader(final ClassLoader loader)
    {
        if(loader == null)
            return ServiceLoader.loadInstalled(ModuleFactory.class);
        else
            return ServiceLoader.load(ModuleFactory.class, loader);
    }

    public <T> Module<T> getModule(String name)
    {
        return loadedModules.get(name.toLowerCase());
    }

    public void loadModule(String name)
    {
        File jarFile = FileUtil.getFile(String.format(MODULE_FILE_FORMAT, name));
        URLClassLoader classLoader = null;

        try {
            classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});

            String trueExtension = null;
            for(String extension : loadedFactories.keySet())
            {
                URL url = classLoader.getResource(String.format(MODULE_DESCRIBER_FORMAT, extension));
                if(url != null)
                {
                    trueExtension = extension;
                    break;
                }
            }

            if(trueExtension != null)
            {
                ModuleFactory factory = loadedFactories.get(trueExtension);
                Module module = factory.load(classLoader);
                loadedModules.put(module.getName().toLowerCase(), module);
            }
            else
            {
                // I believe it's appropriate to throw an exception
                // at this point. If you don't fight me irl
                throw new ModuleException("Could not load module '" + name + "' because no valid " +
                                          "ModuleFactory services were found!");
            }
        } catch(MalformedURLException e) {
            throw new ModuleException("Failed to load Module due to MalformedURL.", e);
        } finally {
            try {
                if(classLoader != null)
                    classLoader.close();
            } catch(IOException e) {
                LOG.error("Failed to close URLClassLoader for module: "+name, e);
            }
        }
    }

    public void unloadModule(String name)
    {
        loadedModules.values().stream().filter(module ->
            module.getName() != null && module.getName().equalsIgnoreCase(name)
        ).findFirst().ifPresent(module -> {
            // Remove it from the loaded
            loadedModules.remove(module.getName().toLowerCase());
            if(representedClient != null)
            {
                Collection<Class<? extends Command>> moduleContents = module.getCommands();
                representedClient.getCommands().forEach(command -> {
                    // Remove if the command is part of the module.
                    if(moduleContents.contains(command.getClass())) {
                        representedClient.removeCommand(command.getName());
                    }
                });
            }
        });
    }
}
