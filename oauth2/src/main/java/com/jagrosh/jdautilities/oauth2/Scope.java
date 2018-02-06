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

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public enum Scope
{
    /**
     * for oauth2 bots, this puts the bot in the user's selected guild by default
     */
    BOT("bot"),
    
    /**
     * allows /users/@me/connections to return linked third-party accounts
     */
    CONNECTIONS("connections"),
    
    /**
     * enables /users/@me to return an email
     */
    EMAIL("email"),
    
    /**
     * allows /users/@me without email
     */
    IDENTIFY("identify"),
    
    /**
     * allows /users/@me/guilds to return basic information about all of a user's guilds
     */
    GUILDS("guilds"),
    
    /**
     * allows /invites/{invite.id} to be used for joining users to a guild
     */
    GUILDS_JOIN("guilds.join"),
    
    /**
     * allows your app to join users to a group dm
     */
    GDM_JOIN("gdm.join"),
    
    /**
     * for local rpc server api access, this allows you to read messages from all client channels (otherwise restricted to channels/guilds your app creates)
     */
    MESSAGES_READ("messages.read"),
    
    /**
     * for local rpc server access, this allows you to control a user's local Discord client
     */
    RPC("rpc"),
    
    /**
     * for local rpc server api access, this allows you to access the API as the local user
     */
    RPC_API("rpc.api"),
    
    /**
     * for local rpc server api access, this allows you to receive notifications pushed out to the user
     */
    RPC_NOTIFICATIONS_READ("rpc.notifications.read"),
    
    /**
     * this generates a webhook that is returned in the oauth token response for authorization code grants
     */
    WEBHOOK_INCOMING("webhook.incoming"),
    
    /**
     * Unknown scope
     */
    UNKNOWN("");
    
    private final String text;
    
    private Scope(String text)
    {
        this.text = text;
    }
    
    public String getText()
    {
        return text;
    }
    
    public static String join(Scope... scopes)
    {
        if(scopes.length==0)
            return "";
        StringBuilder sb = new StringBuilder(scopes[0].getText());
        for(int i=1; i<scopes.length; i++)
            sb.append("%20").append(scopes[i].getText());
        return sb.toString();
    }
    
    public static Scope from(String scope)
    {
        for(Scope s: values())
            if(s.text.equalsIgnoreCase(scope))
                return s;
        return UNKNOWN;
    }
}
