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
package com.jagrosh.jdautilities.statuspage.data;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

@Immutable
public class Status
{
    @Nonnull
    protected final Indicator indicator;
    @Nonnull
    protected final String description;

    public Status(@Nonnull Indicator indicator, @Nonnull String description)
    {
        this.indicator = indicator;
        this.description = description;
    }

    @Nonnull
    public Indicator getIndicator()
    {
        return indicator;
    }

    @Nonnull
    public String getDescription()
    {
        return description;
    }

    public enum Indicator
    {
        NONE,
        MINOR,
        MAJOR,
        CRITICAL,

        UNKNOWN;

        @Nonnull
        public static Indicator from(String severity)
        {
            switch (severity.toLowerCase())
            {
                case "none":
                    return NONE;
                case "minor":
                    return MINOR;
                case "major":
                    return MAJOR;
                case "critical":
                    return CRITICAL;
                default:
                    return UNKNOWN;
            }
        }
    }
}
