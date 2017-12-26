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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Stream;

/**
 * A abstract {@link com.jagrosh.jdautilities.command.Command Command} module,
 * usable with hot-loading and unloading commands from external jars.
 *
 * @since  2.0
 * @author Kaidan Gustave
 */
public abstract class Module<T>
{
    /**
     * A {@link org.slf4j.Logger Logger}.
     * <br>Developers may use this as they see necessary to log module operations.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(Module.class);

    /**
     * A map of {@link com.jagrosh.jdautilities.command.Command Command} Classes
     * contained by this Module, where the key to each command class is the lower
     * case version of the {@link java.lang.Class#getSimpleName() class's simple name}.
     * <br>It's highly recommended you avoid addition of classes to this after
     * instantiation of a Module.
     */
    protected final HashMap<String, Module.Entry<T>> commands;

    /**
     * The module's configuration.
     * <br>Should contain all necessary info to properly load the
     * {@link Module#commands commands} contained by this Module.
     */
    protected final T moduleConfig;

    /**
     * The name of this module, used to bulk unload commands under the same module.
     * <br>This cannot be left {@code null}, as it is the only way to easily specify
     * which commands to unload via a {@link ModuleManager ModuleManager} instance.
     */
    protected String name = null;

    /**
     * Primary super-constructor for a Module implementation.
     *
     * <p>Note that it is highly recommended you do not encapsulate the
     * {@link java.net.URLClassLoader URLClassLoader} provided to this super
     * constructor in an implementation, and that you only use and close it
     * within the operation of your implementation's Constructor(s).
     *
     * <p>Also note that you <b>MUST</b> initialize {@link Module#name} as
     * a non-null value.
     *
     * <p>This is very important to do because of how the JVM handles
     * unloading externally loaded {@link java.lang.Class Classes}.
     * <br>When the JVM loads an external Class via a
     * {@link java.lang.ClassLoader ClassLoader}, unloading requires that
     * the {@link java.lang.System#gc() Garbage Collector} cleans up any
     * instances of any classes loaded by the ClassLoader, as well as the
     * ClassLoader itself.
     *
     * <p>Encapsulation of the URLClassLoader provided to this constructor
     * then might be considered a dangerous game, as it could ultimately
     * prevent unloading the Commands that are part of this module.
     *
     * <p>As a recommendation, you should opt to throw
     * {@link ModuleException ModuleExceptions} in place of other
     * exceptions, as they are the only internal exceptions that will
     * be supported by the Command Modules library.
     *
     * @param  classLoader
     *         A {@link java.net.URLClassLoader URLClassLoader} pointing to
     *         a {@code .jar} file that can be used to load modular commands.
     * @param  configInit
     *         A {@link ConfigLoaderFunction ConfigLoaderFunction} used to
     *         initialize {@link #moduleConfig this Module's config}.
     */
    public Module(URLClassLoader classLoader, ConfigLoaderFunction<T> configInit)
    {
        this.commands = new HashMap<>();

        try
        {
            this.moduleConfig = configInit.load(classLoader);
            init(classLoader);

            classLoader.close();
        }
        catch(Exception e)
        {
            throw wrap(e);
        }

        if(name == null)
            throw new ModuleException("Name was still null after instantiation of module!");
    }

    // TODO Documentation
    protected abstract void init(URLClassLoader classLoader) throws Exception;

    // TODO Documentation
    public List<Module.Entry<T>> getCommands()
    {
        return Collections.list(Collections.enumeration(commands.values()));
    }

    /**
     * Gets the non-null name of the module.
     *
     * @return The name of the module, never null.
     */
    @Nonnull
    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof Module))
            return false;

        Module module = (Module) obj;

        return commands.size() == module.commands.size() && (getName().equalsIgnoreCase(module.getName()));
    }

    @Override
    public int hashCode()
    {
        return Integer.hashCode(commands.size()) + getName().hashCode();
    }

    /**
     * Wraps an {@link java.lang.Exception Exception} simply as
     * a {@link ModuleException ModuleException}.
     * <br>Conventionally, Modules should only throw ModuleExceptions.
     *
     * @param  e
     *         The base Exception. May already be a ModuleException.
     *
     * @return The Exception, wrapped as a ModuleException
     */
    private static ModuleException wrap(Exception e)
    {
        if(e instanceof ModuleException)
            return  (ModuleException) e; // If the exception is a ModuleException, we just return it.
        return new ModuleException("Failed to load module!", e);
    }

    /**
     * Creates a new {@link Module.Entry module entry} for this Module,
     * mapping it to the class's {@link java.lang.Class#getSimpleName() simple name}.
     *
     * @param  clazz
     *         The command class.
     */
    protected final void createEntry(Class<? extends Command> clazz)
    {
        commands.put(clazz.getSimpleName().toLowerCase(), new Module.Entry(this, clazz));
    }

    // TODO Documentation
    public static class Entry<T>
    {
        private final Module<T> module;
        private final String name;
        private final Class<? extends Command> command;

        private Entry(Module<T> module, Class<? extends Command> command)
        {
            this.module = module;
            this.name = command.getSimpleName();
            this.command = command;
        }

        public Module<T> getModule()
        {
            return module;
        }

        public String getName()
        {
            return name;
        }

        public Class<? extends Command> getCommandClass()
        {
            return command;
        }

        public Command createInstance(Object... arguments)
        {
            final Constructor<? extends Command> constructor;
            try {
                constructor = getCommandClass().getConstructor(Stream.of(arguments)
                                                                     .map(Object::getClass)
                                                                     .toArray(Class<?>[]::new));
            } catch(NoSuchMethodException e) {
                throw new ModuleException("No such constructor for "+toString());
            }

            try {
                return constructor.newInstance(arguments);
            } catch(InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new ModuleException("Encountered an exception when instantiating "+toString());
            }
        }

        @Override
        public boolean equals(Object obj)
        {
            if(!(obj instanceof Entry))
                return false;

            Entry<T> entry = (Entry<T>)obj;
            return entry.getModule().equals(getModule()) && entry.getName().equals(getName());
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(module, name, command);
        }

        @Override
        public String toString()
        {
            return "ModuleEntry("+command.getCanonicalName()+")";
        }
    }
}
