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
import java.util.List;

@Immutable
public class ScheduledMaintenance extends Incident
{
    @Nonnull
    protected final OffsetDateTime scheduledFor;
    @Nonnull
    protected final OffsetDateTime scheduledUntil;

    public ScheduledMaintenance(OffsetDateTime monitoringAt, @Nonnull String pageId, @Nonnull OffsetDateTime updatedAt, OffsetDateTime resolvedAt, @Nonnull String impact, @Nonnull String name, @Nonnull OffsetDateTime createdAt, @Nonnull List<Update> updates, @Nonnull String id, @Nonnull String shortlink, @Nonnull Status status, @Nonnull OffsetDateTime scheduledFor, @Nonnull OffsetDateTime scheduledUntil)
    {
        super(monitoringAt, pageId, updatedAt, resolvedAt, impact, name, createdAt, updates, id, shortlink, status);

        this.scheduledFor = scheduledFor;
        this.scheduledUntil = scheduledUntil;
    }

    @Nonnull
    public OffsetDateTime getScheduledFor()
    {
        return scheduledFor;
    }

    @Nonnull
    public OffsetDateTime getScheduledUntil()
    {
        return scheduledUntil;
    }
}
