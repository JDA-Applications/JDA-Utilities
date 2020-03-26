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
import java.time.format.DateTimeFormatter;

/**
 * Contains various data necessary for creating a {@link com.jagrosh.jdautilities.oauth2.session.Session Session}
 * using a {@link com.jagrosh.jdautilities.oauth2.session.SessionController SessionController}.
 *
 * @author Kaidan Gustave
 */
public class SessionData
{
    private final String identifier, accessToken, refreshToken, tokenType;
    private final OffsetDateTime expiration;
    private final Scope[] scopes;

    public SessionData(String identifier, String accessToken, String refreshToken, String tokenType, OffsetDateTime expiration, Scope[] scopes)
    {
        this.identifier = identifier;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiration = expiration;
        this.scopes = scopes;
    }

    /**
     * Gets the session identifier.
     *
     * @return The session identifier.
     */
    public String getIdentifier()
    {
        return identifier;
    }

    /**
     * Gets the session access token.
     *
     * @return The session access token.
     */
    public String getAccessToken()
    {
        return accessToken;
    }

    /**
     * Gets the session refresh token.
     *
     * @return The session refresh token.
     */
    public String getRefreshToken()
    {
        return refreshToken;
    }

    /**
     * Gets the session token type.
     *
     * @return The session token type.
     */
    public String getTokenType()
    {
        return tokenType;
    }

    /**
     * Gets the session expiration time.
     *
     * @return The session expiration time.
     */
    public OffsetDateTime getExpiration()
    {
        return expiration;
    }

    /**
     * Gets the session {@link com.jagrosh.jdautilities.oauth2.Scope Scopes}.
     *
     * @return The session Scopes.
     */
    public Scope[] getScopes()
    {
        return scopes;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof SessionData))
            return false;

        SessionData data = ((SessionData) obj);

        return getIdentifier().equals(data.getIdentifier()) && getTokenType().equals(data.getTokenType());
    }

    @Override
    public String toString()
    {
        return String.format("SessionData(identifier: %s, access-token: %s, refresh-token: %s, type: %s, expires: %s)",
            getIdentifier(), getAccessToken(), getRefreshToken(), getTokenType(),
            getExpiration().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }
}
