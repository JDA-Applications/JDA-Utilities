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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Immutable
public class Incident
{
    @Nullable
    protected final OffsetDateTime monitoringAt;
    @Nonnull
    protected final String pageId;
    @Nonnull
    protected final OffsetDateTime updatedAt;
    @Nullable
    protected final OffsetDateTime resolvedAt;
    @Nonnull
    protected final String impact;
    @Nonnull
    protected final String name;
    @Nonnull
    protected final OffsetDateTime createdAt;
    @Nonnull
    protected final List<Update> updates;
    @Nonnull
    protected final String id;
    @Nonnull
    protected final String shortlink;
    @Nonnull
    protected final Status status;

    public Incident(@Nullable OffsetDateTime monitoringAt, @Nonnull String pageId, @Nonnull OffsetDateTime updatedAt, @Nullable OffsetDateTime resolvedAt, @Nonnull String impact, @Nonnull String name, @Nonnull OffsetDateTime createdAt, @Nonnull List<Update> updates, @Nonnull String id, @Nonnull String shortlink, @Nonnull Status status)
    {
        this.monitoringAt = monitoringAt;
        this.pageId = pageId;
        this.updatedAt = updatedAt;
        this.resolvedAt = resolvedAt;
        this.impact = impact;
        this.name = name;
        this.createdAt = createdAt;
        this.updates = Collections.unmodifiableList(updates);
        this.id = id;
        this.shortlink = shortlink;
        this.status = status;
    }

    @Nullable
    public OffsetDateTime getMonitoringAt()
    {
        return monitoringAt;
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

    @Nullable
    public OffsetDateTime getResolvedAt()
    {
        return resolvedAt;
    }

    @Nonnull
    public String getImpact()
    {
        return impact;
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

    @Nonnull
    public List<Update> getUpdates()
    {
        return updates;
    }

    @Nonnull
    public String getId()
    {
        return id;
    }

    @Nonnull
    public String getShortlink()
    {
        return shortlink;
    }

    @Nonnull
    public Status getStatus()
    {
        return status;
    }

    public enum Status
    {
        INVESTIGATING("investigating"),
        IDENTIFIED("identified"),
        MONITORING("monitoring"),
        RESOLVED("resolved"),
        POSTMORTEM("postmortem"),

        UNKNOWN("");

        @Nonnull
        private static final Map<String, Status> MAP = new HashMap<>();

        static
        {
            for (Status status : Status.values())
                if (MAP.put(status.getKey(), status) != null)
                    throw new IllegalStateException("Duplicate key: " + status.getKey());
        }

        @Nonnull
        private final String key;

        Status(@Nonnull String key)
        {
            this.key = key;
        }

        @Nonnull
        public static Status from(@Nullable String key)
        {
            return MAP.getOrDefault(key, UNKNOWN);
        }

        @Nonnull
        public String getKey()
        {
            return key;
        }
    }

    public enum Impact
    {
        NONE("none"),
        MINOR("minor"),
        MAJOR("major"),
        CRITICAL("critical"),

        UNKNOWN("");

        @Nonnull
        private static final Map<String, Impact> MAP = new HashMap<>();

        static
        {
            for (Impact impact : Impact.values())
                if (MAP.put(impact.getKey(), impact) != null)
                    throw new IllegalStateException("Duplicate key: " + impact.getKey());
        }

        @Nonnull
        private final String key;

        Impact(@Nonnull String key)
        {
            this.key = key;
        }

        @Nonnull
        public static Impact from(@Nullable String key)
        {
            return MAP.getOrDefault(key, UNKNOWN);
        }

        @Nonnull
        public String getKey()
        {
            return key;
        }
    }

    public static class Update
    {
        @Nonnull
        protected final String incidentId;
        @Nonnull
        protected final OffsetDateTime updatedAt;
        @Nonnull
        protected final OffsetDateTime createdAt;
        @Nonnull
        protected final String id;
        @Nonnull
        protected final String body;
        @Nonnull
        protected final OffsetDateTime displayAt;
        @Nonnull
        protected final Status status;

        public Update(@Nonnull String incidentId, @Nonnull OffsetDateTime updatedAt, @Nonnull OffsetDateTime createdAt, @Nonnull String id, @Nonnull String body, @Nonnull OffsetDateTime displayAt, @Nonnull Status status)
        {
            this.incidentId = incidentId;
            this.updatedAt = updatedAt;
            this.createdAt = createdAt;
            this.id = id;
            this.body = body;
            this.displayAt = displayAt;
            this.status = status;
        }

        @Nonnull
        public String getIncidentId()
        {
            return incidentId;
        }

        @Nonnull
        public OffsetDateTime getUpdatedAt()
        {
            return updatedAt;
        }

        @Nonnull
        public OffsetDateTime getCreatedAt()
        {
            return createdAt;
        }

        @Nonnull
        public String getId()
        {
            return id;
        }

        @Nonnull
        public String getBody()
        {
            return body;
        }

        @Nonnull
        public OffsetDateTime getDisplayAt()
        {
            return displayAt;
        }

        @Nonnull
        public Status getStatus()
        {
            return status;
        }
    }
}
