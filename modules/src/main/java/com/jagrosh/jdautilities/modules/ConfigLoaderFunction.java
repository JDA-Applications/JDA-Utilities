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

import java.net.URLClassLoader;

/**
 * A special {@link java.lang.FunctionalInterface FunctionalInterface} that takes a
 * {@link java.net.URLClassLoader URLClassLoader} as an argument and allows
 * {@link java.lang.Exception Exceptions} to be thrown in the event of an error.
 *
 * <p>Thrown exceptions will be caught, wrapped, and rethrown as
 * {@link com.jagrosh.jdautilities.modules.ModuleException ModuleExceptions}, except
 * in the case that the caught exception is already a ModuleException, in which case
 * it will just be rethrown as is.
 *
 * @param  <C>
 *         The type of object representing a configuration container to return from {@link #load(URLClassLoader)}
 *
 * @since  2.0
 * @author Kaidan Gustave
 */
@FunctionalInterface
public interface ConfigLoaderFunction<C>
{
    /**
     * Provides the specified type of {@link java.net.URLClassLoader URLClassLoader}
     * and returns the specified type of configuration object as a result.
     *
     * <p>If a developer wishes to implement a custom implementation of the {@link Module Module},
     * it's strongly recommended that they opt to throw as many exceptions and let the Module
     * constructor catch and wrap them itself.
     *
     * @param  classLoader
     *         The {@link java.net.URLClassLoader URLClassLoader} pointing to a jar for a
     *         {@link Module Module to load}.
     *
     * @return A configuration object of the type {@link C} for this ConfigLoaderFunction.
     *
     * @throws Exception
     *         If the implementation of this requires an error to be thrown.
     */
    C load(URLClassLoader classLoader) throws Exception;
}
