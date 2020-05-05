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
package com.jagrosh.jdautilities.oauth2.requests;

import com.jagrosh.jdautilities.oauth2.OAuth2Client;

/**
 * Simple formattable constants for various URLs used in the JDA-Utilities OAuth2.
 *
 * @author Kaidan Gustave
 */
public enum OAuth2URL
{
    AUTHORIZE("/oauth2/authorize",
        "client_id=%d",
        "redirect_uri=%s",
        "response_type=code",
        "scope=%s",
        "state=%s"),
    TOKEN("/oauth2/token",
        "client_id=%d",
        "redirect_uri=%s",
        "grant_type=authorization_code",
        "code=%s",
        "client_secret=%s",
        "scope=%s"),
    CURRENT_USER("/users/@me"),
    CURRENT_USER_GUILDS("/users/@me/guilds");

    public static final String BASE_API_URL = String.format("https://discord.com/api/v%d", OAuth2Client.DISCORD_REST_VERSION);

    private final String route;
    private final String formattableRoute;
    private final boolean hasQueryParams;
    private final String queryParams;

    OAuth2URL(String route, String... queryParams)
    {
        this.route = route;
        this.hasQueryParams = queryParams.length > 0;

        if(hasQueryParams)
        {
            StringBuilder b = new StringBuilder();

            for(int i = 0; i < queryParams.length; i++)
            {
                b.append(i == 0? '?' : '&');
                b.append(queryParams[i]);
            }

            this.formattableRoute = route + b.toString();
            this.queryParams = b.toString();
        }
        else
        {
            this.formattableRoute = route;
            this.queryParams = "";
        }
    }

    public String getRoute()
    {
        return route;
    }

    public boolean hasQueryParams()
    {
        return hasQueryParams;
    }

    public String compileQueryParams(Object... values) {
        return String.format(queryParams, values).replaceFirst("\\?", "");
    }

    public String getRouteWithBaseUrl()
    {
        return BASE_API_URL + route;
    }

    public String compile(Object... values)
    {
        return BASE_API_URL + (hasQueryParams? String.format(formattableRoute, values) : formattableRoute);
    }
}
