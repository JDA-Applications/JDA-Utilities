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
 * Specifies an {@link java.lang.annotation.Annotation Annotation} can be converted
 * using the specified {@link com.jagrosh.jdautilities.doc.DocConverter DocConverter}
 * value.
 *
 * <p>Only annotations with this annotation applied to it are valid for processing
 * via an instance of {@link com.jagrosh.jdautilities.doc.DocGenerator DocGenerator}.
 *
 * @see    DocConverter
 *
 * @since  2.0
 * @author Kaidan Gustave
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ConvertedBy
{
    /**
     * The {@link com.jagrosh.jdautilities.doc.DocConverter DocConverter}
     * Class that the annotation this is applied to provides to
     * {@link com.jagrosh.jdautilities.doc.DocConverter#read(Annotation)
     * DocConverter#read(Annotation)}.
     *
     * @return The DocConverter Class to use.
     */
    Class<? extends DocConverter> value();
}
