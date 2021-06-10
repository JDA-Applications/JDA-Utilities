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
import com.jagrosh.jdautilities.oauth2.session.DefaultSessionController.DefaultSession;
import java.time.OffsetDateTime;
import java.util.HashMap;

/**
 * The default {@link com.jagrosh.jdautilities.oauth2.session.SessionController SessionController} implementation.
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class DefaultSessionController implements SessionController<DefaultSession>
{
    private final HashMap<String, DefaultSession> sessions = new HashMap<>();
    
    @Override
    public DefaultSession getSession(String identifier)
    {
        return sessions.get(identifier);
    }

    @Override
    public DefaultSession createSession(SessionData data)
    {
        DefaultSession created = new DefaultSession(data);
        sessions.put(data.getIdentifier(), created);
        return created;
    }

    public static class DefaultSession implements Session
    {
        private final String accessToken, refreshToken, tokenType;
        private final OffsetDateTime expiration;
        private final Scope[] scopes;
        
        private DefaultSession(String accessToken, String refreshToken, String tokenType, OffsetDateTime expiration, Scope[] scopes)
        {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.tokenType = tokenType;
            this.expiration = expiration;
            this.scopes = scopes;
        }

        private DefaultSession(SessionData data)
        {
            this(data.getAccessToken(), data.getRefreshToken(), data.getTokenType(), data.getExpiration(), data.getScopes());
        }
        
        @Override
        public String getAccessToken()
        {
            return accessToken;
        }

        @Override
        public String getRefreshToken()
        {
            return refreshToken;
        }

        @Override
        public Scope[] getScopes()
        {
            return scopes;
        }

        @Override
        public String getTokenType()
        {
            return tokenType;
        }

        @Override
        public OffsetDateTime getExpiration()
        {
            return expiration;
        }
    }
}
