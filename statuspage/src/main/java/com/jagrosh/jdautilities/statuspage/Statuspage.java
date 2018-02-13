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
package com.jagrosh.jdautilities.statuspage;

import com.jagrosh.jdautilities.statuspage.data.*;
import com.jagrosh.jdautilities.statuspage.endpoints.*;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.io.Reader;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Immutable
public class Statuspage
{
    @Nonnull
    protected static final String URL_API_BASE = "https://status.discordapp.com/api/v2/";
    @Nonnull
    protected static final String URL_SUMMARY = URL_API_BASE + "summary.json";
    @Nonnull
    protected static final String URL_SERVICE_STATUS = URL_API_BASE + "status.json";
    @Nonnull
    protected static final String URL_COMPONENTS = URL_API_BASE + "components.json";
    @Nonnull
    protected static final String URL_INCIDENTS_ALL = URL_API_BASE + "incidents.json";
    @Nonnull
    protected static final String URL_INCIDENTS_UNRESOLVED = URL_API_BASE + "incidents/unresolved.json";
    @Nonnull
    protected static final String URL_SCHEDULED_MAINTENANCES_ALL = URL_API_BASE + "scheduled-maintenances.json";
    @Nonnull
    protected static final String URL_SCHEDULED_MAINTENANCES_ACTIVE = URL_API_BASE + "scheduled-maintenances/active.json";
    @Nonnull
    protected static final String URL_SCHEDULED_MAINTENANCES_UPCOMING = URL_API_BASE + "scheduled-maintenances/upcoming.json";

    @Nonnull
    protected final OkHttpClient client;
    @Nonnull
    protected final ExecutorService pool;

    public Statuspage()
    {
        this(null, null);
    }

    public Statuspage(@Nullable OkHttpClient client)
    {
        this(client, null);
    }

    public Statuspage(@Nullable ExecutorService pool)
    {
        this(null, pool);
    }

    public Statuspage(@Nullable OkHttpClient client, @Nullable ExecutorService pool)
    {
        this.client = client == null ? new OkHttpClient.Builder().build() : client;
        this.pool = pool == null ? Executors.newSingleThreadExecutor() : pool;
    }

    /**
     * Get a summary of the status page, including a status indicator, component statuses, unresolved incidents, and any upcoming or
     * in-progress scheduled maintenances.
     */
    @Nonnull
    public CompletableFuture<Summary> getSummary()
    {
        return createFuture(f -> f.complete(createSummary(get(URL_SUMMARY))));
    }

    /**
     * Get a list of the 50 most recent incidents. This includes all unresolved incidents as described above,
     * as well as those in the Resolved and Postmortem state.
     */
    @Nonnull
    public CompletableFuture<Incidents> getIncidentsAll()
    {
        return createFuture(f -> f.complete(createIncidents(get(URL_INCIDENTS_ALL))));
    }

    /**
     * Get a list of any unresolved incidents. This endpoint will only return incidents in the Investigating, Identified, or Monitoring state.
     */
    @Nonnull
    public CompletableFuture<Incidents> getIncidentsUnresolved()
    {
        return createFuture(f -> f.complete(createIncidents(get(URL_INCIDENTS_UNRESOLVED))));
    }

    /**
     * Get the components for the page. Each component is listed along with its status - one of operational,
     * degraded_performance, partial_outage, or major_outage.
     */
    @Nonnull
    public CompletableFuture<Components> getComponents()
    {
        return createFuture(f -> f.complete(createComponents(get(URL_COMPONENTS))));
    }

    /**
     * Get a list of the 50 most recent scheduled maintenances. This includes scheduled maintenances as described in the above two endpoints, as well as those in the Completed state.
     */
    @Nonnull
    public CompletableFuture<ScheduledMaintenances> getScheduledMaintenancesAll()
    {
        return createFuture(f -> f.complete(createScheduledMaintenances(get(URL_SCHEDULED_MAINTENANCES_ALL))));
    }

    /**
     * Get a list of any active maintenances. This endpoint will only return scheduled maintenances in the In Progress or Verifying state.
     */
    @Nonnull
    public CompletableFuture<ScheduledMaintenances> getScheduledMaintenancesActive()
    {
        return createFuture(f -> f.complete(createScheduledMaintenances(get(URL_SCHEDULED_MAINTENANCES_ACTIVE))));
    }

    /**
     * Get a list of any upcoming maintenances. This endpoint will only return scheduled maintenances still in the Scheduled state.
     */
    @Nonnull
    public CompletableFuture<ScheduledMaintenances> getScheduledMaintenancesUpcoming()
    {
        return createFuture(f -> f.complete(createScheduledMaintenances(get(URL_SCHEDULED_MAINTENANCES_UPCOMING))));
    }

    /**
     * Get the status rollup for the whole page. This endpoint includes an indicator - one of none, minor, major, or critical,
     * as well as a human description of the blended component status. Examples of the blended status include
     * "All Systems Operational", "Partial System Outage", and "Major Service Outage".
     */
    @Nonnull
    public CompletableFuture<ServiceStatus> getServiceStatus()
    {
        return createFuture(f -> f.complete(createServiceStatus(get(URL_SERVICE_STATUS))));
    }

    @Nonnull
    protected Summary createSummary(@Nonnull JSONObject object)
    {
        @Nonnull
        final JSONArray componentsArray = object.getJSONArray("components");
        @Nonnull
        final List<Component> components = new ArrayList<>(componentsArray.length());
        for (int i = 0; i < componentsArray.length(); i++)
            components.add(createComponent(componentsArray.getJSONObject(i)));
        @Nonnull
        final JSONArray incidentsArray = object.getJSONArray("incidents");
        @Nonnull
        final List<Incident> incidents = new ArrayList<>(incidentsArray.length());
        for (int i = 0; i < incidentsArray.length(); i++)
            incidents.add(createIncident(incidentsArray.getJSONObject(i)));
        @Nonnull
        final JSONArray scheduledMaintenancesArray = object.getJSONArray("scheduled_maintenances");
        @Nonnull
        final List<ScheduledMaintenance> scheduledMaintenances = new ArrayList<>(scheduledMaintenancesArray.length());
        for (int i = 0; i < scheduledMaintenancesArray.length(); i++)
            scheduledMaintenances.add(createScheduledMaintenance(scheduledMaintenancesArray.getJSONObject(i)));
        @Nonnull
        final JSONObject pageObject = object.getJSONObject("page");
        @Nonnull
        final Page page = createPage(pageObject);
        @Nonnull
        final JSONObject statusObject = object.getJSONObject("status");
        @Nonnull
        final Status status = createStatus(statusObject);

        return new Summary(components, incidents, page, scheduledMaintenances, status);
    }

    @Nonnull
    protected Components createComponents(@Nonnull JSONObject object)
    {
        @Nonnull
        final JSONObject pageObject = object.getJSONObject("page");
        @Nonnull
        final Page page = createPage(pageObject);
        @Nonnull
        final JSONArray componentsArray = object.getJSONArray("components");
        @Nonnull
        final List<Component> components = new ArrayList<>(componentsArray.length());
        for (int i = 0; i < componentsArray.length(); i++)
            components.add(createComponent(componentsArray.getJSONObject(i)));

        return new Components(page, components);
    }

    @Nonnull
    protected ServiceStatus createServiceStatus(@Nonnull JSONObject object)
    {
        @Nonnull
        final JSONObject pageObject = object.getJSONObject("page");
        @Nonnull
        final Page page = createPage(pageObject);
        @Nonnull
        final JSONObject statusObject = object.getJSONObject("status");
        @Nonnull
        final Status status = createStatus(statusObject);

        return new ServiceStatus(page, status);
    }

    @Nonnull
    protected ScheduledMaintenances createScheduledMaintenances(@Nonnull JSONObject object)
    {
        @Nonnull
        final JSONObject pageObject = object.getJSONObject("page");
        @Nonnull
        final Page page = createPage(pageObject);
        @Nonnull
        final JSONArray scheduledMaintenancesArray = object.getJSONArray("scheduled_maintenances");
        @Nonnull
        final List<ScheduledMaintenance> scheduledMaintenances = new ArrayList<>(scheduledMaintenancesArray.length());
        for (int i = 0; i < scheduledMaintenancesArray.length(); i++)
            scheduledMaintenances.add(createScheduledMaintenance(scheduledMaintenancesArray.getJSONObject(i)));

        return new ScheduledMaintenances(page, scheduledMaintenances);
    }

    @Nonnull
    protected <T> CompletableFuture<T> createFuture(@Nonnull Consumer<CompletableFuture<T>> action)
    {
        CompletableFuture<T> future = new CompletableFuture<>();

        pool.submit(() -> {
            try
            {
                action.accept(future);
            }
            catch (Exception e)
            {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    @Nonnull
    protected JSONObject get(@Nonnull String url)
    {
        try
        {
            // @formatter:off
            Request request = new Request.Builder()
                .get()
                .url(url)
                .header("Content-Type", "application/json")
                .build();
            // @formatter:on

            Call call = client.newCall(request);

            try (Response response = call.execute())
            {
                ResponseBody body = response.body();

                if (body == null)
                    throw new IllegalStateException("response has no body");

                Reader reader = body.charStream();

                return new JSONObject(new JSONTokener(reader));
            }
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    protected Incident createIncident(@Nonnull JSONObject object)
    {
        @Nullable
        final String monitoringAt = object.optString("monitoring_at");
        @Nonnull
        final String pageId = object.getString("page_id");
        @Nonnull
        final String updatedAt = object.getString("updated_at");
        @Nullable
        final String resolvedAt = object.optString("resolved_at");
        @Nonnull
        final String impact = object.getString("impact");
        @Nonnull
        final String name = object.getString("name");
        @Nonnull
        final String createdAt = object.getString("created_at");
        @Nonnull
        final JSONArray updatesArray = object.getJSONArray("incident_updates");
        @Nonnull
        final List<Incident.Update> updates = new ArrayList<>(updatesArray.length());
        for (int i = 0; i < updatesArray.length(); i++)
            updates.add(createIncidentUpdate(updatesArray.getJSONObject(i)));
        @Nonnull
        final String id = object.getString("id");
        @Nonnull
        final String shortlink = object.getString("shortlink");
        @Nonnull
        final String status = object.getString("status");

        return new Incident(toOffsetDateTime(monitoringAt), pageId, toOffsetDateTime(updatedAt), toOffsetDateTime(resolvedAt), impact, name, toOffsetDateTime(createdAt), updates, id, shortlink, Incident.Status.from(status));
    }

    @Nonnull
    protected Incidents createIncidents(@Nonnull JSONObject object)
    {
        @Nonnull
        final JSONArray incidentsArray = object.getJSONArray("incidents");
        @Nonnull
        final List<Incident> incidents = new ArrayList<>(incidentsArray.length());
        for (int i = 0; i < incidentsArray.length(); i++)
            incidents.add(createIncident(incidentsArray.getJSONObject(i)));
        @Nonnull
        final JSONObject pageObject = object.getJSONObject("page");
        @Nonnull
        final Page page = createPage(pageObject);

        return new Incidents(page, incidents);
    }

    @Nonnull
    protected Incident.Update createIncidentUpdate(@Nonnull JSONObject object)
    {
        @Nonnull
        final String incidentId = object.getString("incident_id");
        @Nonnull
        final String updatedAt = object.getString("updated_at");
        @Nonnull
        final String createdAt = object.getString("created_at");
        @Nonnull
        final String id = object.getString("id");
        @Nonnull
        final String body = object.getString("body");
        @Nonnull
        final String displayAt = object.getString("display_at");
        @Nonnull
        final String status = object.getString("status");

        // There are 3 more fields but they aren't part of the public API
        // and I'm unsure what they are used for:
        // 'affected_components', 'custom_tweet' and 'deliver_notifications'

        return new Incident.Update(incidentId, toOffsetDateTime(updatedAt), toOffsetDateTime(createdAt), id, body, toOffsetDateTime(displayAt), Incident.Status.from(status));
    }

    @Nonnull
    protected Page createPage(@Nonnull JSONObject object)
    {
        @Nonnull
        final String name = object.getString("name");
        @Nonnull
        final String id = object.getString("id");
        @Nonnull
        final String url = object.getString("url");
        @Nonnull
        final String updatedAt = object.getString("updated_at");

        return new Page(name, id, url, toOffsetDateTime(updatedAt));
    }

    @Nonnull
    protected Status createStatus(@Nonnull JSONObject object)
    {
        @Nonnull
        final String indicator = object.getString("indicator");
        @Nonnull
        final String description = object.getString("description");

        return new Status(Status.Indicator.from(indicator), description);
    }

    @Nonnull
    protected Component createComponent(@Nonnull JSONObject object)
    {
        @Nonnull
        final String pageId = object.getString("page_id");
        @Nonnull
        final String updatedAt = object.getString("updated_at");
        @Nonnull
        final String name = object.getString("name");
        @Nonnull
        final String createdAt = object.getString("created_at");
        @Nullable
        final String description = object.optString("description");
        @Nonnull
        final String id = object.getString("id");
        final int position = object.getInt("position");
        @Nonnull
        final String status = object.getString("status");
        final boolean showcase = object.getBoolean("showcase");
        @Nullable
        final String groupId = object.optString("group_id");
        final boolean group = object.getBoolean("group");
        final boolean showOnlyIfDegraded = object.getBoolean("only_show_if_degraded");

        return new Component(pageId, toOffsetDateTime(updatedAt), name, toOffsetDateTime(createdAt), description, id, position, Component.Status.from(status), showcase, groupId, group, showOnlyIfDegraded);
    }

    @Nonnull
    protected ScheduledMaintenance createScheduledMaintenance(@Nonnull JSONObject object)
    {
        @Nullable
        final String monitoringAt = object.optString("monitoring_at");
        @Nonnull
        final String pageId = object.getString("page_id");
        @Nonnull
        final String updatedAt = object.getString("updated_at");
        @Nullable
        final String resolvedAt = object.optString("resolved_at");
        @Nonnull
        final String impact = object.getString("impact");
        @Nonnull
        final String name = object.getString("name");
        @Nonnull
        final String createdAt = object.getString("created_at");
        final JSONArray updatesArray = object.getJSONArray("incident_updates");
        @Nonnull
        final List<Incident.Update> updates = new ArrayList<>(updatesArray.length());
        for (int i = 0; i < updatesArray.length(); i++)
            updates.add(createIncidentUpdate(updatesArray.getJSONObject(i)));
        @Nonnull
        final String id = object.getString("id");
        @Nonnull
        final String shortlink = object.getString("shortlink");
        @Nonnull
        final String status = object.getString("status");
        @Nonnull
        final String scheduledFor = object.getString("updated_at");
        @Nonnull
        final String scheduledUntil = object.getString("updated_at");

        return new ScheduledMaintenance(toOffsetDateTime(monitoringAt), pageId, toOffsetDateTime(updatedAt), toOffsetDateTime(resolvedAt), impact, name, toOffsetDateTime(createdAt), updates, id, shortlink, Incident.Status.from(status), toOffsetDateTime(scheduledFor), toOffsetDateTime(scheduledUntil));
    }

    protected OffsetDateTime toOffsetDateTime(String time)
    {
        return time == null ? null : OffsetDateTime.parse(time);
    }
}
