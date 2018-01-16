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
import net.dv8tion.jda.core.Permission;

import java.lang.annotation.*;

/**
 * A CommandDoc {@link java.lang.annotation.Annotation Annotation} that lists
 * required {@link net.dv8tion.jda.core.Permission Permission}s a bot must have
 * to use a command on a {@link net.dv8tion.jda.core.entities.Guild Guild}.
 *
 * @since  2.0
 * @author Kaidan Gustave
 */
@ConvertedBy(RequiredPermissions.Converter.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequiredPermissions
{
    /**
     * An array of {@link net.dv8tion.jda.core.Permission Permission}s
     * a bot must have to run the command.
     *
     * @return The array of permissions
     */
    Permission[] value();

    /**
     * The {@link com.jagrosh.jdautilities.doc.DocConverter DocConverter} for the
     * {@link com.jagrosh.jdautilities.doc.standard.RequiredPermissions @RequiredPermissions}
     * annotation.
     */
    class Converter implements DocConverter<RequiredPermissions>
    {
        @Override
        public String read(RequiredPermissions annotation)
        {
            Permission[] permissions = annotation.value();

            StringBuilder b = new StringBuilder();

            b.append("Bot must have permissions:");
            switch (permissions.length)
            {
                case 0:
                    b.append(" None");
                    break;
                case 1:
                    b.append(" `").append(permissions[0].getName()).append("`");
                    break;
                default:
                    for (int i = 0; i < permissions.length; i++)
                    {
                        b.append(" `").append(permissions[i]).append("`");
                        if (i != permissions.length - 1)
                            b.append(",");
                    }
                    break;
            }
            return b.toString();
        }
    }
}
