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
package com.jagrosh.jdautilities.oauth2.session;

import com.jagrosh.jdautilities.oauth2.Scope;
import java.time.OffsetDateTime;

/**
 * Implementable data type used to allow access to data regarding OAuth2 sessions.
 *
 * <p>This can be used with a proper {@link com.jagrosh.jdautilities.oauth2.OAuth2Client OAuth2Client}
 * to get information on the logged in {@link com.jagrosh.jdautilities.oauth2.entities.OAuth2User User},
 * as well as {@link com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild Guilds} they are on.
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 * @author Kaidan Gustave
 */
public interface Session
{
    /**
     * Gets the session's access token.
     *
     * @return The session's access token.
     */
    String getAccessToken();

    /**
     * Gets the session's refresh token.
     *
     * @return The session's refresh token.
     */
    String getRefreshToken();

    /**
     * Gets the session's {@link com.jagrosh.jdautilities.oauth2.Scope Scopes}.
     *
     * @return The session's Scopes.
     */
    Scope[] getScopes();

    /**
     * Gets the session's token type.
     *
     * @return The session's token type.
     */
    String getTokenType();

    /**
     * Gets the session's expiration time.
     *
     * @return The session's expiration time.
     */
    OffsetDateTime getExpiration();
}
