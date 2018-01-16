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
package com.jagrosh.jdautilities.examples.doc;

import com.jagrosh.jdautilities.doc.ConvertedBy;
import com.jagrosh.jdautilities.doc.DocConverter;

import java.lang.annotation.*;

/**
 * Annotation to mark a command's specific author.
 *
 * @since  2.0
 * @author Kaidan Gustave
 */
@ConvertedBy(Author.Converter.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Author
{
    String value();

    class Converter implements DocConverter<Author>
    {
        @Override
        public String read(Author annotation)
        {
            return String.format("**Author:** %s", annotation.value());
        }
    }
}
