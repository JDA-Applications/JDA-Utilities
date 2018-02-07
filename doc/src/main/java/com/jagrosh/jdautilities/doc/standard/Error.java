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
import com.jagrosh.jdautilities.doc.DocMultiple;

import java.lang.annotation.*;

/**
 * A CommandDoc {@link java.lang.annotation.Annotation Annotation} that describes
 * a possible error or termination clause a Command might have during it's runtime.
 *
 * <p>These are formatted ways to describe errors and provide the {@link com.jagrosh.jdautilities.doc.standard.Error#response()}
 * method for specifying the bot's response if the error occurs.
 *
 * <p>Multiples of these can be applied using the
 * {@link com.jagrosh.jdautilities.doc.standard.Errors @Errors} annotation, or simply
 * multiples of these can be attached to a class or method.
 *
 * <p>Below is a visual of what this should generally look like:
 * <pre>
 *     <b>Possible Errors:</b>
 *     &#8226; "I encountered an issue while processing this command!" - Houston had a problem!
 *     &#8226; "You used this command too fast" - b1nzy's fault!
 *     &#8226; "An unexpected error occurred!" - Let's just blame Onitor!
 * </pre>
 *
 * @see    com.jagrosh.jdautilities.doc.standard.Errors
 *
 * @since  2.0
 * @author Kaidan Gustave
 */
@ConvertedBy(Error.Converter.class)
@DocMultiple(
    preface = "**Possible Errors:**\n\n",
    prefixEach = "+ ",
    separateBy = "\n\n")
@Documented
@Repeatable(Errors.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Error
{
    /**
     * A brief description of what caused the error.
     *
     * @return A description of what caused the error.
     */
    String value();

    /**
     * A response message that would normally be sent if this
     * error occurs, as a means of users identifying the error
     * without an idea of what exactly went wrong.
     *
     * @return A response message.
     */
    String response() default "";

    /**
     * A prefix appended to the front of the produced String during
     * conversion.
     * <br>Only really useful or needed when a Command has multiple
     * {@link com.jagrosh.jdautilities.doc.standard.Error @Error}
     * annotations, for the purpose of listing.
     *
     * @return A prefix for the conversion, useful when multiple @Errors
     * are specified.
     */
    String prefix() default "";

    /**
     * The {@link com.jagrosh.jdautilities.doc.DocConverter DocConverter}
     * for the {@link com.jagrosh.jdautilities.doc.standard.Error @Error}
     * annotation.
     */
    class Converter implements DocConverter<Error>
    {
        @Override
        public String read(Error annotation)
        {
            StringBuilder b = new StringBuilder(annotation.prefix());
            if(!annotation.response().isEmpty())
                b.append("\"").append(annotation.response()).append("\" - ");
            b.append(annotation.value());
            return b.toString();
        }
    }
}
