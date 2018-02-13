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
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.time.OffsetDateTime;

@Immutable
public class Component
{
    @Nonnull
    protected final String pageId;
    @Nonnull
    protected final OffsetDateTime updatedAt;
    @Nonnull
    protected final String name;
    @Nonnull
    protected final OffsetDateTime createdAt;
    @Nullable
    protected final String description;
    @Nonnull
    protected final String id;
    protected final int position;
    @Nonnull
    protected final Status status;
    protected final boolean showcase;
    @Nullable
    protected final String groupId;
    protected final boolean group;
    protected final boolean showOnlyIfDegraded;

    public Component(@Nonnull String pageId, @Nonnull OffsetDateTime updatedAt, @Nonnull String name, @Nonnull OffsetDateTime createdAt, @Nullable String description, @Nonnull String id, int position, @Nonnull Status status, boolean showcase, @Nullable String groupId, boolean group, boolean showOnlyIfDegraded)
    {
        this.pageId = pageId;
        this.updatedAt = updatedAt;
        this.name = name;
        this.createdAt = createdAt;
        this.description = description;
        this.id = id;
        this.position = position;
        this.status = status;
        this.showcase = showcase;
        this.groupId = groupId;
        this.group = group;
        this.showOnlyIfDegraded = showOnlyIfDegraded;
    }

    @Nonnull
    public String getPageId()
    {
        return pageId;
    }

    @Nonnull
    public OffsetDateTime getUpdatedAt()
    {
        return updatedAt;
    }

    @Nonnull
    public String getName()
    {
        return name;
    }

    @Nonnull
    public OffsetDateTime getCreatedAt()
    {
        return createdAt;
    }

    @Nullable
    public String getDescription()
    {
        return description;
    }

    @Nonnull
    public String getId()
    {
        return id;
    }

    public int getPosition()
    {
        return position;
    }

    @Nonnull
    public Status getStatus()
    {
        return status;
    }

    public boolean isShowcase()
    {
        return showcase;
    }

    @Nullable
    public String getGroupId()
    {
        return groupId;
    }

    public boolean isGroup()
    {
        return group;
    }

    public boolean isShowOnlyIfDegraded()
    {
        return showOnlyIfDegraded;
    }

    public enum Status
    {
        OPERATIONAL,
        DEGRADED_PERFORMANCE,
        PARTIAL_OUTAGE,
        MAJOR_OUTAGE,

        UNKNOWN;

        @Nonnull
        public static Status from(String name)
        {
            switch (name.toLowerCase())
            {
                case "operational":
                    return OPERATIONAL;
                case "degraded_performance":
                    return DEGRADED_PERFORMANCE;
                case "partial_outage":
                    return PARTIAL_OUTAGE;
                case "major_outage":
                    return MAJOR_OUTAGE;
                default:
                    return UNKNOWN;
            }
        }
    }
}
