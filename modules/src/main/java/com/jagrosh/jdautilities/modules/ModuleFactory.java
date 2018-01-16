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

import javax.annotation.WillNotClose;
import java.net.URLClassLoader;

/**
 * @author Kaidan Gustave
 */
@SuppressWarnings("RedundantThrows")
public interface ModuleFactory<T, M extends Module<T>>
{
    /**
     * Creates a new {@link com.jagrosh.jdautilities.modules.Module Module} of type {@link T T}
     * using a {@link java.net.URLClassLoader URLClassLoader} linking to the jar represented by
     * the Module returned.
     *
     * @param  classLoader
     *         A URLClassLoader linking to the jar represented by the Module returned.
     *
     * @throws ModuleException
     *         If an error occurs while creating the Module.
     *
     * @return The newly created Module of type T.
     */
    M create(@WillNotClose URLClassLoader classLoader);

    /**
     * Gets the file name extension for the configuration resource that will be used by
     * the {@link com.jagrosh.jdautilities.modules.Module Module} to initialize and properly
     * load + map commands.
     *
     * <p>This must be provided here in order to verify it exists in the modular jar when
     * discerning whether or not this factory is valid for creating a Module from the jar.
     *
     * <p>Note: modular jars that wish to be used must therefor have a file named {@code module.X},
     * where {@code X} is the string returned by this method packaged as a part of their resources.
     *
     * @return The valid file extension of the file used to describe the module.
     */
    String getFileExtension();

    /**
     * Runs checks regarding the underlying module returned from {@link #create(URLClassLoader)}.
     *
     * <p>This is generally good for organizing post-construction checks, as opposed to running them
     * inside of {@code #create(URLClassLoader)}.
     *
     * @throws Exception
     *         If the developer implementing this interface wants to create an error if a check fails.
     *
     * @param  module
     *         The {@link com.jagrosh.jdautilities.modules.Module Module} to be checked.
     */
    default void check(M module) throws Exception {}

    /**
     * Loads a {@link com.jagrosh.jdautilities.modules.Module Module} of type {@link M M}
     * using the provided {@link java.net.URLClassLoader URLClassLoader}.
     *
     * @param  classLoader
     *         A URLClassLoader pointing to a jar to be used when constructing the Module of type M.
     *
     * @throws ModuleException
     *         If an error occurs.
     *
     * @return A newly created Module of type M.
     *
     * @implSpec
     *         <b>It is not a good idea to override this.</b>
     *         <br>Just don't do it! It's a dumb idea! You have nothing to gain from doing it!
     *         Developers who want to checks certain aspects of a newly created Module after
     *         instantiating it should just override {@link #check(Module)}.
     */
    default M load(@WillNotClose URLClassLoader classLoader)
    {
        M module = create(classLoader);

        try {
            check(module);
        } catch(Throwable t) {
            if(t instanceof ModuleException)
                throw (ModuleException) t;
            throw new ModuleException("Failed to load module!", t);
        }

        return module;
    }
}
