/*
 * Copyright 2016 John Grosh (jagrosh).
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
package com.jagrosh.jdautilities.commandclient;

import com.jagrosh.jdautilities.commandclient.annotation.JDACommand;
import java.util.List;

/**
 * A "compiler" for {@link java.lang.Object Object}s that uses {@link java.lang.annotation.Annotation Annotation}s
 * as helpers for creating {@link com.jagrosh.jdautilities.commandclient.Command Command}s.
 *
 * <p>Previous to version 1.6 all Commands required the Command abstract class to be extended in source.
 * The primary issue that came with this was that Commands were restricted to that method of creation, offering
 * no support for popular means such as annotated commands.
 *
 * <p>Since 1.6 the introduction of {@link com.jagrosh.jdautilities.commandclient.CommandBuilder CommandBuilder}
 * has allowed the potential to create unique {@link com.jagrosh.jdautilities.commandclient.Command Command}
 * objects after compilation.
 * <br>The primary duty of this class is to provide a "in runtime" converter for generics that are annotated with
 * the {@link JDACommand.Module JDACommand.Module}
 *
 * @since  1.7
 * @author Kaidan Gustave
 */
public interface AnnotatedModuleCompiler
{
    /**
     * Compiles one or more {@link com.jagrosh.jdautilities.commandclient.Command Command}s
     * using method annotations as for properties from the specified {@link java.lang.Object
     * Object}.
     *
     * <p><b>This Object must be annotated with {@link
     * com.jagrosh.jdautilities.commandclient.annotation.JDACommand.Module @JDACommand.Module}!</b>
     *
     * @param  o
     *         The Object, annotated with {@code @JDACommand.Module}.
     *
     * @return A {@link java.util.List} of Commands generated from the provided Object
     */
    List<Command> compile(Object o);
}
