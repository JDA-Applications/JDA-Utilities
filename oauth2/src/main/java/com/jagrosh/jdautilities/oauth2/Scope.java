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
 * Constants used to specify the scope of OAuth2 sessions.
 *
 * <p>All OAuth2 sessions can act within' their available scope upon creation,
 * and as such, these are specified in when the session's are created.
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public enum Scope
{
    /**
     * For oauth2 bots, this puts the bot in the user's selected guild by default
     */
    BOT("bot"),
    
    /**
     * Allows /users/@me/connections to return linked third-party accounts
     */
    CONNECTIONS("connections"),
    
    /**
     * Enables /users/@me to return an email
     */
    EMAIL("email"),
    
    /**
     * Allows /users/@me without email
     */
    IDENTIFY("identify"),
    
    /**
     * Allows /users/@me/guilds to return basic information about all of a user's guilds
     */
    GUILDS("guilds"),
    
    /**
     * Allows /invites/{invite.id} to be used for joining users to a guild
     */
    GUILDS_JOIN("guilds.join"),
    
    /**
     * Allows your app to join users to a group dm
     */
    GDM_JOIN("gdm.join"),
    
    /**
     * For local rpc server api access, this allows you to read messages from all
     * client channels (otherwise restricted to channels/guilds your app creates)
     */
    MESSAGES_READ("messages.read"),
    
    /**
     * For local rpc server access, this allows you to control a user's local Discord client
     */
    RPC("rpc"),
    
    /**
     * For local rpc server api access, this allows you to access the API as the local user
     */
    RPC_API("rpc.api"),
    
    /**
     * For local rpc server api access, this allows you to receive notifications pushed out to the user
     */
    RPC_NOTIFICATIONS_READ("rpc.notifications.read"),
    
    /**
     * This generates a webhook that is returned in the oauth token response for authorization code grants
     */
    WEBHOOK_INCOMING("webhook.incoming"),
    
    /**
     * Unknown scope
     */
    UNKNOWN("");
    
    private final String text;
    
    Scope(String text)
    {
        this.text = text;
    }

    /**
     * The text key associated with this scope.
     *
     * @return The text key associated with this scope.
     */
    public String getText()
    {
        return text;
    }

    public static boolean contains(Scope[] scopes, Scope scope)
    {
        if(scopes == null || scopes.length == 0 || scope == null || scope == UNKNOWN)
            return false;
        for(Scope s : scopes)
            if(s == scope)
                return true;
        return false;
    }

    /**
     * Joins the specified scopes properly as they should
     * be represented as part of an authorization URL.
     *
     * @param  scopes
     *         The scopes to join.
     *
     * @return A String representing how the scopes should be
     *         represented as part of an authorization URL.
     */
    public static String join(Scope... scopes)
    {
        return join(false, scopes);
    }

    /**
     * Joins the specified scopes properly as they should
     * be represented as part of an authorization URL.
     *
     * @param  scopes
     *         The scopes to join.
     * @param  bySpace
     *         If the scopes should be joined by " " or "%20" (default: "%20")
     *
     * @return A String representing how the scopes should be
     *         represented as part of an authorization URL.
     */
    public static String join(boolean bySpace, Scope... scopes)
    {
        if(scopes.length == 0)
            return "";
        StringBuilder sb = new StringBuilder(scopes[0].getText());
        for(int i = 1; i < scopes.length; i++)
        {
            if (bySpace)
            {
                sb.append(" ");
            }
            else
            {
                sb.append("%20");
            }
            sb.append(scopes[i].getText());
        }
        return sb.toString();
    }

    /**
     * Gets a scope based on the specified text key.
     *
     * @param  scope
     *         A text key to get a scope by.
     *
     * @return The scope matching the provided text key
     *         ({@link Scope#UNKNOWN UNKNOWN} by default)
     */
    public static Scope from(String scope)
    {
        for(Scope s : values())
            if(s.text.equalsIgnoreCase(scope))
                return s;
        return UNKNOWN;
    }
}
