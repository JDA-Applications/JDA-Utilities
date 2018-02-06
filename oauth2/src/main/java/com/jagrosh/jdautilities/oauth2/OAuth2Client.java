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
package com.jagrosh.jdautilities.oauth2;

import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2User;
import com.jagrosh.jdautilities.oauth2.session.DefaultSessionController;
import com.jagrosh.jdautilities.oauth2.session.Session;
import com.jagrosh.jdautilities.oauth2.session.SessionController;
import com.jagrosh.jdautilities.oauth2.state.DefaultStateController;
import com.jagrosh.jdautilities.oauth2.state.InvalidStateException;
import com.jagrosh.jdautilities.oauth2.state.StateController;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import net.dv8tion.jda.core.exceptions.HttpException;
import net.dv8tion.jda.core.requests.Requester;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.IOUtil;
import net.dv8tion.jda.core.utils.JDALogger;
import net.dv8tion.jda.core.utils.MiscUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class OAuth2Client
{
    public static final String BASE_API_URL = "https://discordapp.com/api/";
    public static final String AUTHORIZE_URL = BASE_API_URL + "oauth2/authorize"
            + "?client_id=%d"
            + "&redirect_uri=%s"
            + "&response_type=code"
            + "&scope=%s"
            + "&state=%s";
    public static final String TOKEN_URL = BASE_API_URL + "oauth2/token"
            + "?client_id=%d"
            + "&redirect_uri=%s"
            + "&grant_type=authorization_code"
            + "&code=%s"
            + "&client_secret=%s";
    public static final String CURRENT_USER_URL = BASE_API_URL + "users/@me";
    public static final String CURRENT_USER_GUILDS_URL = BASE_API_URL + "users/@me/guilds";
    public static final String USER_AGENT = "JDA-Utils Oauth2("+JDAUtilitiesInfo.GITHUB + " | " + JDAUtilitiesInfo.VERSION + ")";
    public static final Logger LOG = JDALogger.getLog(OAuth2Client.class);
    public static final RequestBody EMPTY_BODY = RequestBody.create(null, new byte[0]);
    
    private final long clientId;
    private final String clientSecret;
    private final SessionController sessionController;
    private final StateController stateController;
    private final OkHttpClient client;

    protected OAuth2Client(long clientId, String clientSecret, SessionController sessionController, 
            StateController stateController, OkHttpClient client)
    {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.sessionController = sessionController;
        this.stateController = stateController;
        this.client = client;
    }
    
    public String generateAuthorizationURL(String redirectUri, Scope... scopes)
    {
        return String.format(AUTHORIZE_URL, clientId, MiscUtil.encodeUTF8(redirectUri), Scope.join(scopes), stateController.generateNewState(redirectUri));
    }
    
    public Session startSession(String code, String state, String identifier) throws InvalidStateException, IOException
    {
        Checks.notEmpty(state, "state");
        Checks.notEmpty(code, "code");
        String redirectUri = stateController.consumeState(state);
        if(redirectUri==null)
            throw new InvalidStateException(String.format("No state '%s' exists!", state));
        String requestUrl = String.format(TOKEN_URL, clientId, MiscUtil.encodeUTF8(redirectUri), code, clientSecret);
        try(Response response = client.newCall(new Request.Builder()
                .header("Content-Type", "x-www-form-urlencoded").url(requestUrl)
                .post(EMPTY_BODY).build()).execute())
        {
            if(!response.isSuccessful())
                throw failure(response);
            JSONObject body = new JSONObject(new JSONTokener(Requester.getBody(response)));
            String[] scopeStrings = body.getString("scope").split(" ");
            Scope[] scopes = new Scope[scopeStrings.length];
            for(int i=0; i<scopeStrings.length; i++)
                scopes[i] = Scope.from(scopeStrings[i]);
            return sessionController.createSession(identifier, body.getString("access_token"), body.getString("refresh_token"), 
                    body.getString("token_type"), OffsetDateTime.now().plusSeconds(body.getInt("expires_in")), scopes);
        }
    }
    
    public OAuth2User getUser(Session session) throws IOException
    {
        Checks.notNull(session, "session");
        try(Response response = client.newCall(new Request.Builder()
                .header("Authorization", session.getTokenType()+" "+session.getAccessToken())
                .get().url(CURRENT_USER_URL)
                .build()).execute())
        {
            if(!response.isSuccessful())
                throw failure(response);
            JSONObject body = new JSONObject(new JSONTokener(Requester.getBody(response)));
            return new OAuth2User(body.getLong("id"), body.getString("username"), body.getString("discriminator"), 
                    body.optString("avatar", null), body.optString("email", null), body.getBoolean("verified"), body.getBoolean("mfa_enabled"));
        }
    }
    
    public List<OAuth2Guild> getGuilds(Session session) throws IOException
    {
        Checks.notNull(session, "session");
        try(Response response = client.newCall(new Request.Builder()
                .header("Authorization", session.getTokenType()+" "+session.getAccessToken())
                .get().url(CURRENT_USER_GUILDS_URL)
                .build()).execute())
        {
            if(!response.isSuccessful())
                throw failure(response);
            JSONArray body = new JSONArray(new JSONTokener(Requester.getBody(response)));
            List<OAuth2Guild> list = new LinkedList<>();
            JSONObject obj;
            for(int i=0; i<body.length(); i++)
            {
                obj = body.getJSONObject(i);
                list.add(new OAuth2Guild(obj.getLong("id"), obj.getString("name"), obj.getString("icon"), obj.getBoolean("owner"), obj.getInt("permissions")));
            }
            return list;
        }
    }
    
    public long getIdLong()
    {
        return clientId;
    }
    
    public String getId()
    {
        return Long.toUnsignedString(clientId);
    }
    
    protected static HttpException failure(Response response) throws IOException
    {
        final InputStream stream = Requester.getBody(response);
        final String responseBody = new String(IOUtil.readFully(stream));
        return new HttpException("Request returned failure " + response.code() + ": " + responseBody);
    }
    
    public static class Builder
    {
        private long clientId = -1;
        private String clientSecret;
        private SessionController sessionController;
        private StateController stateController;
        private OkHttpClient client;
        
        public OAuth2Client build()
        {
            Checks.positive(clientId, "client id");
            Checks.notEmpty(clientSecret, "client secret");
            return new OAuth2Client(clientId, clientSecret, sessionController==null ? new DefaultSessionController() : sessionController, 
                    stateController==null ? new DefaultStateController() : stateController, client==null ? new OkHttpClient.Builder().build() : client);
        }
        
        public Builder setClientId(long clientId)
        {
            this.clientId = clientId;
            return this;
        }
        
        public Builder setClientSecret(String clientSecret)
        {
            this.clientSecret = clientSecret;
            return this;
        }
        
        public Builder setSessionController(SessionController sessionController)
        {
            this.sessionController = sessionController;
            return this;
        }
        
        public Builder setStateController(StateController stateController)
        {
            this.stateController = stateController;
            return this;
        }
        
        public Builder setOkHttpClient(OkHttpClient client)
        {
            this.client = client;
            return this;
        }
    }
}
