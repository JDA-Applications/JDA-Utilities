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

import com.jagrosh.jdautilities.commons.async.AsyncFuture;
import com.jagrosh.jdautilities.commons.async.AsyncTask;
import com.jagrosh.jdautilities.statuspage.data.*;
import com.jagrosh.jdautilities.statuspage.endpoints.*;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.io.Reader;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Immutable
public class Statuspage
{
    @Nonnull
    protected static final String URL_API_BASE = "https://status.discordapp.com/api/v2";
    @Nonnull
    protected static final String URL_SUMMARY = URL_API_BASE + "/summary.json";
    @Nonnull
    protected static final String URL_SERVICE_STATUS = URL_API_BASE + "/status.json";
    @Nonnull
    protected static final String URL_COMPONENTS = URL_API_BASE + "/components.json";
    @Nonnull
    protected static final String URL_SCHEDULED_INCIDENTS_BASE = URL_API_BASE + "/incidents";
    @Nonnull
    protected static final String URL_INCIDENTS_ALL = URL_SCHEDULED_INCIDENTS_BASE + ".json";
    @Nonnull
    protected static final String URL_INCIDENTS_UNRESOLVED = URL_SCHEDULED_INCIDENTS_BASE + "/unresolved.json";
    @Nonnull
    protected static final String URL_SCHEDULED_MAINTENANCES_BASE = URL_API_BASE + "/scheduled-maintenances";
    @Nonnull
    protected static final String URL_SCHEDULED_MAINTENANCES_ALL = URL_SCHEDULED_MAINTENANCES_BASE + ".json";
    @Nonnull
    protected static final String URL_SCHEDULED_MAINTENANCES_ACTIVE = URL_SCHEDULED_MAINTENANCES_BASE + "/active.json";
    @Nonnull
    protected static final String URL_SCHEDULED_MAINTENANCES_UPCOMING = URL_SCHEDULED_MAINTENANCES_BASE + "/upcoming.json";

    @Nonnull
    protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    @Nonnull
    protected final OkHttpClient client;

    public Statuspage()
    {
        this(null);
    }

    public Statuspage(@Nullable OkHttpClient client)
    {
        this.client = client == null ? new OkHttpClient.Builder().build() : client;
    }

    /**
     * Get a summary of the status page, including a status indicator, component statuses, unresolved incidents, and any upcoming or
     * in-progress scheduled maintenances.
     */
    @Nonnull
    public AsyncFuture<Summary> getSummary()
    {
        return get(URL_SUMMARY, this::createSummary);
    }

    /**
     * Get the components for the page. Each component is listed along with its status - one of operational,
     * degraded_performance, partial_outage, or major_outage.
     */
    @Nonnull
    public AsyncFuture<Components> getComponents()
    {
        return get(URL_COMPONENTS, this::createComponents);
    }

    /**
     * Get a list of the 50 most recent incidents. This includes all unresolved incidents as described above,
     * as well as those in the Resolved and Postmortem state.
     */
    @Nonnull
    public AsyncFuture<Incidents> getIncidentsAll()
    {
        return get(URL_INCIDENTS_ALL, this::createIncidents);
    }

    /**
     * Get a list of any unresolved incidents. This endpoint will only return incidents in the Investigating, Identified, or Monitoring state.
     */
    @Nonnull
    public AsyncFuture<Incidents> getIncidentsUnresolved()
    {
        return get(URL_INCIDENTS_UNRESOLVED, this::createIncidents);
    }

    /**
     * Get a list of the 50 most recent scheduled maintenances. This includes scheduled maintenances as described in the above two endpoints, as well as those in the Completed state.
     */
    @Nonnull
    public AsyncFuture<ScheduledMaintenances> getScheduledMaintenancesAll()
    {
        return get(URL_SCHEDULED_MAINTENANCES_ALL, this::createScheduledMaintenances);
    }

    /**
     * Get a list of any active maintenances. This endpoint will only return scheduled maintenances in the In Progress or Verifying state.
     */
    @Nonnull
    public AsyncFuture<ScheduledMaintenances> getScheduledMaintenancesActive()
    {
        return get(URL_SCHEDULED_MAINTENANCES_ACTIVE, this::createScheduledMaintenances);
    }

    /**
     * Get a list of any upcoming maintenances. This endpoint will only return scheduled maintenances still in the Scheduled state.
     */
    @Nonnull
    public AsyncFuture<ScheduledMaintenances> getScheduledMaintenancesUpcoming()
    {
        return get(URL_SCHEDULED_MAINTENANCES_UPCOMING, this::createScheduledMaintenances);
    }

    /**
     * Get the status rollup for the whole page. This endpoint includes an indicator - one of none, minor, major, or critical,
     * as well as a human description of the blended component status. Examples of the blended status include
     * "All Systems Operational", "Partial System Outage", and "Major Service Outage".
     */
    @Nonnull
    public AsyncFuture<ServiceStatus> getServiceStatus()
    {
        return get(URL_SERVICE_STATUS, this::createServiceStatus);
    }

    @Nonnull
    protected <T> AsyncFuture<T> get(@Nonnull String url, @Nonnull Function<JSONObject, T> funtion)
    {
        AsyncTask<T> future = new AsyncTask<>();

        // @formatter:off
            Request request = new Request.Builder()
                .get()
                .url(url)
                .header("Content-Type", "application/json")
                .build();
            // @formatter:on

        Call call = this.client.newCall(request);

        call.enqueue(new Callback()
        {
            @Override
            public void onResponse(@Nonnull Call call, @Nonnull Response response)
            {
                ResponseBody body = response.body();

                if (body == null)
                    throw new IllegalStateException("response has no body");

                Reader reader = body.charStream();

                JSONObject object = new JSONObject(new JSONTokener(reader));

                T t = funtion.apply(object);

                future.complete(t);
            }

            @Override
            public void onFailure(@Nonnull Call call, @Nonnull IOException e)
            {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    @Nonnull
    protected Summary createSummary(@Nonnull JSONObject object)
    {
        final JSONArray componentsArray = object.getJSONArray("components");
        final List<Component> components = new ArrayList<>(componentsArray.length());
        for (int i = 0; i < componentsArray.length(); i++)
            components.add(createComponent(componentsArray.getJSONObject(i)));
        final JSONArray incidentsArray = object.getJSONArray("incidents");
        final List<Incident> incidents = new ArrayList<>(incidentsArray.length());
        for (int i = 0; i < incidentsArray.length(); i++)
            incidents.add(createIncident(incidentsArray.getJSONObject(i)));
        final JSONArray scheduledMaintenancesArray = object.getJSONArray("scheduled_maintenances");
        final List<ScheduledMaintenance> scheduledMaintenances = new ArrayList<>(scheduledMaintenancesArray.length());
        for (int i = 0; i < scheduledMaintenancesArray.length(); i++)
            scheduledMaintenances.add(createScheduledMaintenance(scheduledMaintenancesArray.getJSONObject(i)));
        final JSONObject pageObject = object.getJSONObject("page");
        final Page page = createPage(pageObject);
        final JSONObject statusObject = object.getJSONObject("status");
        final Status status = createStatus(statusObject);

        return new Summary(components, incidents, page, scheduledMaintenances, status);
    }

    @Nonnull
    protected Components createComponents(@Nonnull JSONObject object)
    {
        final JSONObject pageObject = object.getJSONObject("page");
        final Page page = createPage(pageObject);
        final JSONArray componentsArray = object.getJSONArray("components");
        final List<Component> components = new ArrayList<>(componentsArray.length());
        for (int i = 0; i < componentsArray.length(); i++)
            components.add(createComponent(componentsArray.getJSONObject(i)));

        return new Components(page, components);
    }

    @Nonnull
    protected Incidents createIncidents(@Nonnull JSONObject object)
    {
        final JSONArray incidentsArray = object.getJSONArray("incidents");
        final List<Incident> incidents = new ArrayList<>(incidentsArray.length());
        for (int i = 0; i < incidentsArray.length(); i++)
            incidents.add(createIncident(incidentsArray.getJSONObject(i)));
        final JSONObject pageObject = object.getJSONObject("page");
        final Page page = createPage(pageObject);

        return new Incidents(page, incidents);
    }

    @Nonnull
    protected ScheduledMaintenances createScheduledMaintenances(@Nonnull JSONObject object)
    {
        final JSONObject pageObject = object.getJSONObject("page");
        final Page page = createPage(pageObject);
        final JSONArray scheduledMaintenancesArray = object.getJSONArray("scheduled_maintenances");
        final List<ScheduledMaintenance> scheduledMaintenances = new ArrayList<>(scheduledMaintenancesArray.length());
        for (int i = 0; i < scheduledMaintenancesArray.length(); i++)
            scheduledMaintenances.add(createScheduledMaintenance(scheduledMaintenancesArray.getJSONObject(i)));

        return new ScheduledMaintenances(page, scheduledMaintenances);
    }

    @Nonnull
    protected ServiceStatus createServiceStatus(@Nonnull JSONObject object)
    {
        final JSONObject pageObject = object.getJSONObject("page");
        final Page page = createPage(pageObject);
        final JSONObject statusObject = object.getJSONObject("status");
        final Status status = createStatus(statusObject);

        return new ServiceStatus(page, status);
    }

    @Nonnull
    protected Component createComponent(@Nonnull JSONObject object)
    {
        final String pageId = object.getString("page_id");
        final String updatedAt = object.getString("updated_at");
        final String name = object.getString("name");
        final String createdAt = object.getString("created_at");
        final String description = object.optString("description");
        final String id = object.getString("id");
        final int position = object.getInt("position");
        final String status = object.getString("status");
        final boolean showcase = object.getBoolean("showcase");
        final String groupId = object.optString("group_id");
        final boolean group = object.getBoolean("group");
        final boolean showOnlyIfDegraded = object.getBoolean("only_show_if_degraded");

        return new Component(pageId, toOffsetDateTime(updatedAt), name, toOffsetDateTime(createdAt), description, id, position, Component.Status.from(status), showcase, groupId, group, showOnlyIfDegraded);
    }

    @Nonnull
    protected Incident createIncident(@Nonnull JSONObject object)
    {
        final String monitoringAt = object.optString("monitoring_at");
        final String pageId = object.getString("page_id");
        final String updatedAt = object.getString("updated_at");
        final String resolvedAt = object.optString("resolved_at");
        final String impact = object.getString("impact");
        final String name = object.getString("name");
        final String createdAt = object.getString("created_at");
        final JSONArray updatesArray = object.getJSONArray("incident_updates");
        final List<Incident.Update> updates = new ArrayList<>(updatesArray.length());
        for (int i = 0; i < updatesArray.length(); i++)
            updates.add(createIncidentUpdate(updatesArray.getJSONObject(i)));
        final String id = object.getString("id");
        final String shortlink = object.getString("shortlink");
        final String status = object.getString("status");

        return new Incident(toOffsetDateTime(monitoringAt), pageId, toOffsetDateTime(updatedAt), toOffsetDateTime(resolvedAt), impact, name, toOffsetDateTime(createdAt), updates, id, shortlink, Incident.Status.from(status));
    }

    @Nonnull
    protected Incident.Update createIncidentUpdate(@Nonnull JSONObject object)
    {
        final String incidentId = object.getString("incident_id");
        final String updatedAt = object.getString("updated_at");
        final String createdAt = object.getString("created_at");
        final String id = object.getString("id");
        final String body = object.getString("body");
        final String displayAt = object.getString("display_at");
        final String status = object.getString("status");

        // There are 3 more fields but they aren't part of the public API
        // and I'm unsure what they are used for:
        // 'affected_components', 'custom_tweet' and 'deliver_notifications'

        return new Incident.Update(incidentId, toOffsetDateTime(updatedAt), toOffsetDateTime(createdAt), id, body, toOffsetDateTime(displayAt), Incident.Status.from(status));
    }

    @Nonnull
    protected ScheduledMaintenance createScheduledMaintenance(@Nonnull JSONObject object)
    {
        final String monitoringAt = object.optString("monitoring_at");
        final String pageId = object.getString("page_id");
        final String updatedAt = object.getString("updated_at");
        final String resolvedAt = object.optString("resolved_at");
        final String impact = object.getString("impact");
        final String name = object.getString("name");
        final String createdAt = object.getString("created_at");
        final JSONArray updatesArray = object.getJSONArray("incident_updates");
        final List<Incident.Update> updates = new ArrayList<>(updatesArray.length());
        for (int i = 0; i < updatesArray.length(); i++)
            updates.add(createIncidentUpdate(updatesArray.getJSONObject(i)));
        final String id = object.getString("id");
        final String shortlink = object.getString("shortlink");
        final String status = object.getString("status");
        final String scheduledFor = object.getString("updated_at");
        final String scheduledUntil = object.getString("updated_at");

        return new ScheduledMaintenance(toOffsetDateTime(monitoringAt), pageId, toOffsetDateTime(updatedAt), toOffsetDateTime(resolvedAt), impact, name, toOffsetDateTime(createdAt), updates, id, shortlink, Incident.Status.from(status), toOffsetDateTime(scheduledFor), toOffsetDateTime(scheduledUntil));
    }

    @Nonnull
    protected Status createStatus(@Nonnull JSONObject object)
    {
        final String indicator = object.getString("indicator");
        final String description = object.getString("description");

        return new Status(Status.Indicator.from(indicator), description);
    }

    @Nonnull
    protected Page createPage(@Nonnull JSONObject object)
    {
        final String name = object.getString("name");
        final String id = object.getString("id");
        final String url = object.getString("url");
        final String updatedAt = object.getString("updated_at");

        return new Page(name, id, url, toOffsetDateTime(updatedAt));
    }

    protected OffsetDateTime toOffsetDateTime(String time)
    {
        return time == null || time.isEmpty() ? null : OffsetDateTime.parse(time, DATE_TIME_FORMATTER);
    }
}
