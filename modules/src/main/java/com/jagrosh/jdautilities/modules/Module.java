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
import javax.annotation.WillNotClose;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A abstract {@link com.jagrosh.jdautilities.command.Command Command} module,
 * usable with hot-loading and unloading commands from external jars.
 *
 * <p>Documentation on how to properly use this can be found in the
 * {@link com.jagrosh.jdautilities.modules.ModuleManager ModuleManager documentation}.
 *
 * @param  <T>
 *         The type of object that serves as the {@link #moduleConfig configurator of the module}.
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
     * be supported by the Command Modules library, as well as perform
     * as many initialization operations for your implementation as you
     * can inside the {@link #init(URLClassLoader) init} method override.
     *
     * @param  classLoader
     *         A {@link java.net.URLClassLoader URLClassLoader} pointing to
     *         a {@code .jar} file that can be used to load modular commands.
     * @param  configInit
     *         A {@link ConfigLoaderFunction ConfigLoaderFunction} used to
     *         initialize {@link #moduleConfig this Module's config}.
     *
     * @throws ModuleException
     *         If any sort of errors occur while instantiating the Module.
     *         <br>Implementations who perform operations in the body of
     *         their sub-constructors should opt to mimic this behavior.
     */
    public Module(@WillNotClose URLClassLoader classLoader, ConfigLoaderFunction<T> configInit)
    {
        this.commands = new HashMap<>();

        try
        {
            this.moduleConfig = configInit.load(classLoader);
            init(classLoader);
        }
        catch(Exception e)
        {
            throw wrap(e);
        }

        if(name == null)
            throw new ModuleException("Name was still null after instantiation of module!");
    }

    /**
     * Initializes this Module.
     *
     * <p>In general implementations of Module when overriding this should keep in mind
     * the following ideas and goals:
     * <ul>
     *     <li>The {@link #moduleConfig moduleConfig} has already been initialized.</li>
     *     <li>The primary goal of this method is to initialize the {@link #name name} and
     *     populate the {@link #commands commands} map with it's contents.</li>
     *     <li>The {@link java.net.URLClassLoader URLClassLoader} argument is <b>not to be
     *     closed</b> by the implementation.</li>
     *     <li>A resource with the extension returned by
     *     {@link ModuleFactory#getFileExtension() ModuleFactory#getFileExtension()}
     *     exists, and should not be checked to see if it exists again.</li>
     * </ul>
     *
     * @param  classLoader
     *         The URLClassLoader, linked to the jar from which this Module is going to represent.
     *         <br><b>Should not be closed, this will be closed by the constructing
     *         {@link ModuleManager ModuleManager}</b>!
     *
     * @throws Exception
     *         If an error occurs. This can be for any reason a implementation might
     *         want. These will be wrapped and rethrown as {@link ModuleException ModuleExceptions}.
     */
    protected abstract void init(@WillNotClose URLClassLoader classLoader) throws Exception;

    /**
     * Gets an unmodifiable list of {@link Module.Entry Entries} represented by this module.
     *
     * @return An unmodifiable list of Entries represented by this module.
     */
    public List<Module.Entry<T>> getCommands()
    {
        return Collections.unmodifiableList(commands.values().stream().collect(Collectors.toList()));
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

    /**
     * Returns {@code true} if this Module has an {@link Module.Entry Entry}
     * mapped to the provided name.
     *
     * @param  name
     *         The name of the entry, case insensitive.
     *
     * @return {@code true} if this Module has an Entry by the name provided, {@code false} otherwise.
     */
    public boolean hasEntry(String name)
    {
        return commands.containsKey(name.toLowerCase());
    }

    /**
     * Returns an {@link Module.Entry Entry} from this module key'd to the provided name,
     * or {@code null} if one does not exist by the name.
     * <br>Names are case-insensitive.
     *
     * @param  name
     *         The name of the Entry to get.
     *
     * @return The entry represented by the name, or {@code null} if one doesn't exist.
     */
    public Module.Entry<T> getEntry(String name)
    {
        return commands.get(name.toLowerCase());
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
            return (ModuleException) e; // If the exception is a ModuleException, we just return it.
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

    /**
     * An entry object that wraps a {@link java.lang.Class Class} extending Command.
     * <br>This also provides a more safe form of reflection instantiation through
     * {@link Module.Entry#createInstance(Object...)}
     *
     * @param  <T>
     *         The type of {@link Module Module} this Entry comes from.
     */
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

        /**
         * Gets the {@link Module Module} that manages this Entry.
         *
         * @return The Module that manages this Entry.
         */
        public Module<T> getModule()
        {
            return module;
        }

        /**
         * Gets the name of this Entry, which is more precisely the
         * {@link java.lang.Class#getSimpleName() simple name}
         * of the {@link Entry#getCommandClass() command class}
         * wrapped by this entry.
         *
         * @return The name of the entry.
         */
        public String getName()
        {
            return name;
        }

        /**
         * Gets the raw {@link java.lang.Class Class} wrapped by this
         * Entry.
         * <br>This always extends {@link com.jagrosh.jdautilities.command.Command Command}
         *
         * @return The Class wrapped by this Entry.
         */
        public Class<? extends Command> getCommandClass()
        {
            return command;
        }

        /**
         * Creates a new instance of the {@link #getCommandClass() command class} wrapped
         * by this Entry.
         *
         * <p>This is <b>NOT</b> the same as calling {@link java.lang.Class#newInstance() Class#newInstance()},
         * as this looks for a constructor matching the provided arguments and the instantiates it.
         *
         * @throws ModuleException
         *         If one of the following occurs:
         *         <ul>
         *             <li>There is no constructor that matches the types of the arguments provided.</li>
         *             <li>The matching constructor is not public.</li>
         *             <li>An error occurs while {@link java.lang.reflect.Constructor#newInstance(Object...) creating a new instance}.</li>
         *         </ul>
         *
         * @param  arguments
         *         Arguments matching the types of one of the Entry's constructors.
         *
         * @return The newly constructed {@link com.jagrosh.jdautilities.command.Command Command}.
         */
        public Command createInstance(Object... arguments)
        {
            final Constructor<? extends Command> constructor;
            try {
                constructor = getCommandClass().getConstructor(Stream.of(arguments)
                                                                     .map(Object::getClass)
                                                                     .toArray(Class<?>[]::new));
            } catch(NoSuchMethodException e) {
                throw new ModuleException("No such constructor for "+toString(), e);
            }

            // Make sure it's public
            if(!Modifier.isPublic(constructor.getModifiers()))
            {
                throw new ModuleException("Could not instantiate Entry with arguments because it is not a public constructor!");
            }

            try {
                return constructor.newInstance(arguments);
            } catch(InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new ModuleException("Encountered an exception when instantiating "+toString(), e);
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
