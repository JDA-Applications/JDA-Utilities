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
import java.time.OffsetDateTime;

@Immutable
public class Page
{
    @Nonnull
    protected final String name;
    @Nonnull
    protected final String id;
    @Nonnull
    protected final String url;
    @Nonnull
    protected final OffsetDateTime updatedAt;

    public Page(@Nonnull String name, @Nonnull String id, @Nonnull String url, @Nonnull OffsetDateTime updatedAt)
    {
        this.name = name;
        this.id = id;
        this.url = url;
        this.updatedAt = updatedAt;
    }

    @Nonnull
    public String getName()
    {
        return name;
    }

    @Nonnull
    public String getId()
    {
        return id;
    }

    @Nonnull
    public String getUrl()
    {
        return url;
    }

    @Nonnull
    public OffsetDateTime getUpdatedAt()
    {
        return updatedAt;
    }
}
