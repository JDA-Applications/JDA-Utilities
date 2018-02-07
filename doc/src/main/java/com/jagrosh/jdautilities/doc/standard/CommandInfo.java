/*
 * Copyright 2016-2018 John Grosh (jagrosh) & Kaidan Gustave (TheMonitorLizard)
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
package com.jagrosh.jdautilities.doc.standard;

import com.jagrosh.jdautilities.doc.ConvertedBy;
import com.jagrosh.jdautilities.doc.DocConverter;

import java.lang.annotation.*;

/**
 * A CommandDoc {@link java.lang.annotation.Annotation Annotation}
 * that contains basic information on command usage, declaration,
 * and requirements.
 *
 * <p>This annotation should be used for "primary" documented
 * information, and when applicable, developers who are using the
 * {@link com.jagrosh.jdautilities.doc.DocGenerator DocGenerator}
 * returned by {@link com.jagrosh.jdautilities.doc.DocGenerator#getDefaultGenerator()},
 * and who are not implementing their own CommandDoc systems, should
 * use this in place of creating a new annotation and converter if
 * possible.
 *
 * @since  2.0
 * @author Kaidan Gustave
 */
@ConvertedBy(CommandInfo.Converter.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CommandInfo
{
    /**
     * The name and aliases of a command.
     *
     * The first one should be the official name, and following
     * elements should be aliases (if any are allowed).
     *
     * @return The name and aliases
     */
    String[] name() default {};

    /**
     * A short format or explanation of a command's usage.
     *
     * @return The usage
     */
    String usage() default "";

    /**
     * A description of this command, what it does, and
     * (if needed) elaboration on the {@link com.jagrosh.jdautilities.doc.standard.CommandInfo#usage()}.
     *
     * @return The description of this command
     */
    String description() default "";

    /**
     * A series of prerequisites or requirements a user
     * must have to use this command.
     *
     * @return The requirements to use the command
     */
    String[] requirements() default {};

    /**
     * The {@link com.jagrosh.jdautilities.doc.DocConverter DocConverter} for
     * the {@link com.jagrosh.jdautilities.doc.standard.CommandInfo @CommandInfo}
     * annotation.
     */
    class Converter implements DocConverter<CommandInfo>
    {
        @Override
        public String read(CommandInfo annotation)
        {
            String[] names = annotation.name();
            String usage = annotation.usage();
            String description = annotation.description();
            String[] requirements = annotation.requirements();

            StringBuilder b = new StringBuilder();

            if(names.length > 0)
            {
                b.append("**Name:** `").append(names[0]).append("`").append("\n\n");
                if(names.length > 1)
                {
                    b.append("**Aliases:**");
                    for(int i = 1; i < names.length; i++)
                    {
                        b.append(" `").append(names[i]).append("`")
                                .append(i != names.length - 1? "," : "\n\n");
                    }
                }
            }

            if(!usage.isEmpty())
                b.append("**Usage:** ").append(usage).append("\n\n");

            if(!description.isEmpty())
                b.append("**Description:** ").append(description).append("\n\n");

            if(requirements.length == 1)
            {
                b.append("**Requirement:** ").append(requirements[0]).append("\n\n");
            }
            else if(requirements.length > 1)
            {
                b.append("**Requirements:**\n");
                for(int i = 1; i <= requirements.length; i++)
                {
                    b.append(i).append(") ").append(requirements[i - 1]);
                    if(i != requirements.length)
                        b.append("\n");
                }
            }

            return b.toString();
        }
    }
}
