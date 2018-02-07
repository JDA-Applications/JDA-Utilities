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
package com.jagrosh.jdautilities.doc;

import java.lang.annotation.*;

/**
 * A helper {@link java.lang.annotation.Annotation Annotation}, useful for
 * formatting multiple occurrences of the same CommandDoc annotation.
 *
 * <p>This is best coupled with usage of an {@link java.lang.annotation.Repeatable @Repeatable}
 * annotation and a similarly named holder annotation for multiple occurrences.
 * <br>{@link com.jagrosh.jdautilities.doc.standard.Error @Error} and {@link
 * com.jagrosh.jdautilities.doc.standard.Errors @Errors} are an example of such practice.
 *
 * @see    com.jagrosh.jdautilities.doc.standard.Error
 * @see    com.jagrosh.jdautilities.doc.standard.Errors
 *
 * @since  2.0
 * @author Kaidan Gustave
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface DocMultiple
{
    /**
     * Text that occurs before all occurrences of the annotation
     * this is applied to.
     * <br>Default this is an empty String.
     *
     * @return The preface text
     */
    String preface() default "";

    /**
     * A prefix annotation appended to the front of each occurrence.
     * <br>Default this is an empty string.
     *
     * @return The prefix String.
     */
    String prefixEach() default "";

    /**
     * A separator String applied in-between occurrences.
     * <br>Default this is an empty string.
     *
     * @return The separator String.
     */
    String separateBy() default "";
}
