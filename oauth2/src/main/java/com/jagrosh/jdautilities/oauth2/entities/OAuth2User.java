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

import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.IMentionable;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.entities.User;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class OAuth2User implements ISnowflake, IMentionable
{
    private final long id;
    private final String name, discriminator, avatar, email;
    private final boolean verified, mfaEnabled;

    public OAuth2User(long id, String name, String discriminator, String avatar, String email, boolean verified, boolean mfaEnabled)
    {
        this.id = id;
        this.name = name;
        this.discriminator = discriminator;
        this.avatar = avatar;
        this.email = email;
        this.verified = verified;
        this.mfaEnabled = mfaEnabled;
    }
    
    /**
     * Gets the Users Snowflake ID as a {@code long}.
     *
     * @return The Users Snowflake ID as a {@code long}.
     */
    @Override
    public long getIdLong()
    {
        return id;
    }

    /**
     * Gets the Users account name.
     *
     * @return The Users account name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the OAuth2User's email address that is associated with their 
     * Discord account. This will be null if this user is acquired without
     * the 'email' OAuth2 scope.
     * 
     * @return The Users email, or null if the necessary scope is not provided
     */
    public String getEmail()
    {
        return email;
    }
    
    /**
     * Returns true if the user's Discord account has been verified via
     * email. This is required to send messages in servers when certain 
     * moderation levels are used.
     * 
     * @return true if the user has verified their account
     */
    public boolean isVerified()
    {
        return verified;
    }
    
    /**
     * Returns true if this user has multi-factor authentication enabled. Some
     * servers require mfa for administrative actions.
     * 
     * @return true if the user has mfa enabled
     */
    public boolean isMfaEnabled()
    {
        return mfaEnabled;
    }

    /**
     * Gets the Users discriminator.
     *
     * @return The Users discriminator.
     */
    public String getDiscriminator()
    {
        return discriminator;
    }

    /**
     * Gets the Users avatar ID.
     *
     * @return The Users avatar ID.
     */
    public String getAvatarId()
    {
        return avatar;
    }

    /**
     * Gets the Users avatar URL.
     *
     * @return The Users avatar URL.
     */
    public String getAvatarUrl()
    {
        return getAvatarId() == null ? null : "https://cdn.discordapp.com/avatars/" + getId() + "/" + getAvatarId()
            + (getAvatarId().startsWith("a_") ? ".gif" : ".png");
    }

    /**
     * Gets the Users {@link DefaultAvatar} avatar ID.
     *
     * @return The Users {@link DefaultAvatar} avatar ID.
     */
    public String getDefaultAvatarId()
    {
        return DefaultAvatar.values()[Integer.parseInt(getDiscriminator()) % DefaultAvatar.values().length].toString();
    }

    /**
     * Gets the Users {@link DefaultAvatar} avatar URL.
     *
     * @return The Users {@link DefaultAvatar} avatar URL.
     */
    public String getDefaultAvatarUrl()
    {
        return "https://discordapp.com/assets/" + getDefaultAvatarId() + ".png";
    }

    /**
     * Gets the Users avatar URL, or their {@link DefaultAvatar} avatar URL if they
     * do not have a custom avatar set on their account.
     *
     * @return The Users effective avatar URL.
     */
    public String getEffectiveAvatarUrl()
    {
        return getAvatarUrl() == null ? getDefaultAvatarUrl() : getAvatarUrl();
    }
    
    /**
     * Gets whether or not this OAuth2User is a bot.<p>
     *
     * While, at the time of writing this documentation, bots cannot
     * authenticate applications, there may be a time in the future
     * where they have such an ability.
     *
     * @return False
     */
    public boolean isBot()
    {
        return false;
    }
    
    /**
     * Gets the OAuth2User as a discord formatted mention.<p>
     *
     * {@code <@SNOWFLAKE_ID> }
     *
     * @return A discord formatted mention of this OAuth2User.
     */
    @Override
    public String getAsMention()
    {
        return "<@" + id + '>';
    }
    
    public User getJDAUser(JDA jda)
    {
        return jda.getUserById(id);
    }
    
    public User getJDAUser(ShardManager shards)
    {
        for(JDA jda: shards.getShards())
            if(jda.getUserById(id)!=null)
                return jda.getUserById(id);
        return null;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof OAuth2User))
            return false;
        OAuth2User oUser = (OAuth2User) o;
        return this == oUser || this.id == oUser.id;
    }
    
    @Override
    public int hashCode()
    {
        return Long.hashCode(id);
    }

    @Override
    public String toString()
    {
        return "U:" + getName() + '(' + id + ')';
    }
    
    /**
     * Constants representing one of five different
     * default avatars a {@link OAuth2User} can have.
     */
    public enum DefaultAvatar
    {
        BLURPLE("6debd47ed13483642cf09e832ed0bc1b"),
        GREY("322c936a8c8be1b803cd94861bdfa868"),
        GREEN("dd4dbc0016779df1378e7812eabaa04d"),
        ORANGE("0e291f67c9274a1abdddeb3fd919cbaa"),
        RED("1cbd08c76f8af6dddce02c5138971129");

        private final String text;

        DefaultAvatar(String text)
        {
            this.text = text;
        }

        @Override
        public String toString()
        {
            return text;
        }
    }
}
