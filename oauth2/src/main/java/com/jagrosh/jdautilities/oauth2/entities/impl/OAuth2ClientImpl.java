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
package com.jagrosh.jdautilities.oauth2.entities.impl;

import com.jagrosh.jdautilities.oauth2.OAuth2Client;
import com.jagrosh.jdautilities.oauth2.Scope;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2User;
import com.jagrosh.jdautilities.oauth2.exceptions.MissingScopeException;
import com.jagrosh.jdautilities.oauth2.requests.OAuth2Action;
import com.jagrosh.jdautilities.oauth2.requests.OAuth2Requester;
import com.jagrosh.jdautilities.oauth2.requests.OAuth2URL;
import com.jagrosh.jdautilities.oauth2.session.DefaultSessionController;
import com.jagrosh.jdautilities.oauth2.session.Session;
import com.jagrosh.jdautilities.oauth2.session.SessionController;
import com.jagrosh.jdautilities.oauth2.session.SessionData;
import com.jagrosh.jdautilities.oauth2.state.DefaultStateController;
import com.jagrosh.jdautilities.oauth2.exceptions.InvalidStateException;
import com.jagrosh.jdautilities.oauth2.state.StateController;
import net.dv8tion.jda.core.exceptions.HttpException;
import net.dv8tion.jda.core.requests.Method;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.IOUtil;
import net.dv8tion.jda.core.utils.JDALogger;
import net.dv8tion.jda.core.utils.MiscUtil;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class OAuth2ClientImpl implements OAuth2Client
{
    private static final Logger LOG = JDALogger.getLog(OAuth2Client.class);
    
    private final long clientId;
    private final String clientSecret;
    private final SessionController sessionController;
    private final StateController stateController;
    private final OkHttpClient httpClient;
    private final OAuth2Requester requester;

    public OAuth2ClientImpl(long clientId, String clientSecret, SessionController sessionController,
                            StateController stateController, OkHttpClient httpClient)
    {
        Checks.check(clientId >= 0, "Invalid Client ID");
        Checks.notNull(clientSecret, "Client Secret");

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.sessionController = sessionController == null? new DefaultSessionController() : sessionController;
        this.stateController = stateController == null? new DefaultStateController() : stateController;
        this.httpClient = httpClient == null? new OkHttpClient.Builder().build() : httpClient;
        this.requester = new OAuth2Requester(httpClient);
    }
    
    @Override
    public String generateAuthorizationURL(String redirectUri, Scope... scopes)
    {
        Checks.notNull(redirectUri, "Redirect URI");

        return OAuth2URL.AUTHORIZE.compile(clientId, MiscUtil.encodeUTF8(redirectUri),
            Scope.join(scopes), stateController.generateNewState(redirectUri));
    }

    @Override
    public OAuth2Action<Session> startSession(String code, String state, String identifier) throws InvalidStateException
    {
        Checks.notEmpty(code, "code");
        Checks.notEmpty(state, "state");

        String redirectUri = stateController.consumeState(state);
        if(redirectUri == null)
            throw new InvalidStateException(String.format("No state '%s' exists!", state));

        return new OAuth2Action<Session>(this, Method.POST, OAuth2URL.TOKEN.compile(clientId,
            MiscUtil.encodeUTF8(redirectUri), code, clientSecret))
        {
            @Override
            protected Headers getHeaders()
            {
                return Headers.of("Content-Type", "x-www-form-urlencoded");
            }

            @Override
            protected Session handle(Response response) throws IOException
            {
                if(!response.isSuccessful())
                    throw failure(response);

                JSONObject body = new JSONObject(new JSONTokener(Requester.getBody(response)));

                String[] scopeStrings = body.getString("scope").split(" ");
                Scope[] scopes = new Scope[scopeStrings.length];
                for(int i = 0; i < scopeStrings.length; i++)
                {
                    scopes[i] = Scope.from(scopeStrings[i]);
                }

                return sessionController.createSession(new SessionData(identifier,
                    body.getString("access_token"), body.getString("refresh_token"),
                    body.getString("token_type"), OffsetDateTime.now().plusSeconds(body.getInt("expires_in")), scopes));
            }
        };
    }

    @Override
    public OAuth2Action<OAuth2User> getUser(Session session)
    {
        Checks.notNull(session, "Session");
        return new OAuth2Action<OAuth2User>(this, Method.GET, OAuth2URL.CURRENT_USER.compile()) {

            @Override
            protected Headers getHeaders()
            {
                return Headers.of("Authorization", generateAuthorizationHeader(session));
            }

            @Override
            protected OAuth2User handle(Response response) throws IOException
            {
                if(!response.isSuccessful())
                    throw failure(response);
                JSONObject body = new JSONObject(new JSONTokener(Requester.getBody(response)));
                return new OAuth2UserImpl(session, body.getLong("id"),
                    body.getString("username"), body.getString("discriminator"),
                    body.optString("avatar", null), body.optString("email", null),
                    body.optBoolean("verified", false), body.getBoolean("mfa_enabled"));
            }
        };
    }

    @Override
    public OAuth2Action<List<OAuth2Guild>> getGuilds(Session session)
    {
        Checks.notNull(session, "session");
        if(!Scope.contains(session.getScopes(), Scope.GUILDS))
            throw new MissingScopeException("get guilds for a Session", Scope.GUILDS);
        return new OAuth2Action<List<OAuth2Guild>>(this, Method.GET, OAuth2URL.CURRENT_USER_GUILDS.compile()) {
            @Override
            protected Headers getHeaders()
            {
                return Headers.of("Authorization", generateAuthorizationHeader(session));
            }

            @Override
            protected List<OAuth2Guild> handle(Response response) throws IOException
            {
                if(!response.isSuccessful())
                    throw failure(response);

                JSONArray body = new JSONArray(new JSONTokener(Requester.getBody(response)));
                List<OAuth2Guild> list = new LinkedList<>();
                JSONObject obj;
                for(int i = 0; i < body.length(); i++)
                {
                    obj = body.getJSONObject(i);
                    list.add(new OAuth2GuildImpl(obj.getLong("id"), obj.getString("name"),
                        obj.getString("icon"), obj.getBoolean("owner"), obj.getInt("permissions")));
                }
                return list;
            }
        };
    }

    @Override
    public long getId()
    {
        return clientId;
    }

    @Override
    public String getSecret()
    {
        return clientSecret;
    }

    @Override
    public StateController getStateController()
    {
        return stateController;
    }

    @Override
    public SessionController getSessionController()
    {
        return sessionController;
    }

    /**
     * Gets the internal OAuth2Requester used by this OAuth2Client.
     *
     * @return The internal OAuth2Requester used by this OAuth2Client.
     */
    public OAuth2Requester getRequester()
    {
        return requester;
    }

    protected static HttpException failure(Response response) throws IOException
    {
        final InputStream stream = Requester.getBody(response);
        final String responseBody = new String(IOUtil.readFully(stream));
        return new HttpException("Request returned failure " + response.code() + ": " + responseBody);
    }

    // Generates an authorization header 'X Y', where 'X' is the session's
    // token-type and 'Y' is the session's access token.
    private String generateAuthorizationHeader(Session session)
    {
        return String.format("%s %s", session.getTokenType(), session.getAccessToken());
    }
}
