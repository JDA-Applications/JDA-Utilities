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
package com.jagrosh.jdautilities.statuspage.endpoints;

import com.jagrosh.jdautilities.statuspage.data.*;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;

@Immutable
public class Summary
{
    @Nonnull
    protected final List<Component> components;
    @Nonnull
    protected final List<Incident> incidents;
    @Nonnull
    protected final Page page;
    @Nonnull
    protected final List<ScheduledMaintenance> scheduledMaintenances;
    @Nonnull
    protected final Status status;

    public Summary(@Nonnull List<Component> components, @Nonnull List<Incident> incidents, @Nonnull Page page, @Nonnull List<ScheduledMaintenance> scheduledMaintenances, @Nonnull Status status)
    {
        this.components = Collections.unmodifiableList(components);
        this.incidents = Collections.unmodifiableList(incidents);
        this.page = page;
        this.scheduledMaintenances = Collections.unmodifiableList(scheduledMaintenances);
        this.status = status;
    }

    @Nonnull
    public List<Component> getComponents()
    {
        return components;
    }

    @Nonnull
    public List<Incident> getIncidents()
    {
        return incidents;
    }

    @Nonnull
    public Page getPage()
    {
        return page;
    }

    @Nonnull
    public List<ScheduledMaintenance> getScheduledMaintenances()
    {
        return scheduledMaintenances;
    }

    @Nonnull
    public Status getStatus()
    {
        return status;
    }
}
