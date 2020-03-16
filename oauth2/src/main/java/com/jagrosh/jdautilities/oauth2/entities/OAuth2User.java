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
package com.jagrosh.jdautilities.oauth2.entities;

import com.jagrosh.jdautilities.oauth2.OAuth2Client;
import com.jagrosh.jdautilities.oauth2.session.Session;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.User;

/**
 * OAuth2 representation of a Discord User.
 * <br>More specifically, this is the User that the session is currently managing when retrieved using
 * {@link com.jagrosh.jdautilities.oauth2.OAuth2Client#getUser(Session) OAuth2Client#getUser}.
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 * @author Kaidan Gustave
 */
public interface OAuth2User extends ISnowflake, IMentionable
{
    /**
     * Gets the underlying {@link com.jagrosh.jdautilities.oauth2.OAuth2Client OAuth2Client}
     * that created this OAuth2User.
     *
     * @return The OAuth2Client that created this OAuth2User.
     */
    OAuth2Client getClient();

    /**
     * Gets the originating {@link com.jagrosh.jdautilities.oauth2.session.Session}
     * that is responsible for this OAuth2User.
     *
     * @return The Session responsible for this OAuth2User.
     */
    Session getSession();

    /**
     * Gets the user's Snowflake ID as a String.
     *
     * @return The user's Snowflake ID as a String.
     */
    String getId();

    /**
     * Gets the user's Snowflake ID as a {@code long}.
     *
     * @return The user's Snowflake ID as a {@code long}.
     */
    long getIdLong();

    /**
     * Gets the user's account name.
     *
     * @return The user's account name.
     */
    String getName();

    /**
     * Gets the user's email address that is associated with their Discord account.
     *
     * <p>Note that if this user is acquired without the '{@link com.jagrosh.jdautilities.oauth2.Scope#EMAIL email}'
     * OAuth {@link com.jagrosh.jdautilities.oauth2.Scope Scope}, this will throw a
     * {@link com.jagrosh.jdautilities.oauth2.exceptions.MissingScopeException MissingScopeException}.
     *
     * @return The user's email.
     *
     * @throws com.jagrosh.jdautilities.oauth2.exceptions.MissingScopeException
     *         If the corresponding {@link OAuth2User#getSession() session} does not have the
     *         proper 'email' OAuth2 scope
     */
    String getEmail();

    /**
     * Returns {@code true} if the user's Discord account has been verified via email.
     *
     * <p>This is required to send messages in guilds where certain moderation levels are used.
     *
     * @return {@code true} if the user has verified their account, {@code false} otherwise.
     */
    boolean isVerified();

    /**
     * Returns {@code true} if this user has multi-factor authentication enabled.
     *
     * <p>Some guilds require mfa for administrative actions.
     *
     * @return {@code true} if the user has mfa enabled, {@code false} otherwise.
     */
    boolean isMfaEnabled();

    /**
     * Gets the user's discriminator.
     *
     * @return The user's discriminator.
     */
    String getDiscriminator();

    /**
     * Gets the user's avatar ID, or {@code null} if they have not set one.
     *
     * @return The user's avatar ID, or {@code null} if they have not set one.
     */
    String getAvatarId();

    /**
     * Gets the user's avatar URL, or {@code null} if they have not set one.
     *
     * @return The user's avatar URL, or {@code null} if they have not set one.
     */
    String getAvatarUrl();

    /**
     * Gets the user's avatar URL.
     *
     * @return The user's avatar URL.
     */
    String getDefaultAvatarId();

    /**
     * Gets the user's default avatar ID.
     *
     * @return The user's default avatar ID.
     */
    String getDefaultAvatarUrl();

    /**
     * Gets the user's avatar URL, or their {@link #getDefaultAvatarUrl() default avatar URL}
     * if they do not have a custom avatar set on their account.
     *
     * @return The user's effective avatar URL.
     */
    String getEffectiveAvatarUrl();

    /**
     * Gets whether or not this user is a bot.<p>
     *
     * <p>While, at the time of writing this documentation, bots cannot
     * authenticate applications, there may be a time in the future
     * where they have such an ability.
     *
     * @return {@code false}
     *
     * @deprecated
     *         Due to the nature of OAuth2 at this moment, bots are not
     *         allowed to use the various urls provided.
     *         <br>This method is scheduled for removal upon merging it
     *         with <code>master</code> in JDA-Utilities 2.2
     */
    @Deprecated
    default boolean isBot()
    {
        // Note: the code here has not changed from it's implementation.
        return false;
    }

    /**
     * Gets the user as a discord formatted mention:
     * <br>{@code <@SNOWFLAKE_ID> }
     *
     * @return A discord formatted mention of this user.
     */
    String getAsMention();

    /**
     * Gets the corresponding {@link net.dv8tion.jda.api.entities.User JDA User}
     * from the provided instance of {@link net.dv8tion.jda.api.JDA JDA}.
     *
     * <p>Note that there is no guarantee that this will not return {@code null}
     * as the instance of JDA may not have access to the User.
     *
     * <p>For sharded bots, use {@link OAuth2User#getJDAUser(ShardManager)}.
     *
     * @param  jda
     *         The instance of JDA to get from.
     *
     * @return A JDA User, possibly {@code null}.
     */
    User getJDAUser(JDA jda);

    /**
     * Gets the corresponding {@link net.dv8tion.jda.api.entities.User JDA User}
     * from the provided {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager}.
     *
     * <p>Note that there is no guarantee that this will not return {@code null}
     * as the ShardManager may not have access to the User.
     *
     * <p>For un-sharded bots, use {@link OAuth2User#getJDAUser(JDA)}.
     *
     * @param  shardManager
     *         The ShardManager to get from.
     *
     * @return A JDA User, possibly {@code null}.
     */
    User getJDAUser(ShardManager shardManager);
}
