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
import com.jagrosh.jdautilities.oauth2.entities.OAuth2User;
import com.jagrosh.jdautilities.oauth2.exceptions.MissingScopeException;
import com.jagrosh.jdautilities.oauth2.session.Session;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class OAuth2UserImpl implements OAuth2User
{
    private final OAuth2Client client;
    private final Session session;
    private final long id;
    private final String name, discriminator, avatar, email;
    private final boolean verified, mfaEnabled;

    public OAuth2UserImpl(OAuth2Client client, Session session, long id, String name, String discriminator,
                          String avatar, String email, boolean verified, boolean mfaEnabled)
    {
        this.client = client;
        this.session = session;
        this.id = id;
        this.name = name;
        this.discriminator = discriminator;
        this.avatar = avatar;
        this.email = email;
        this.verified = verified;
        this.mfaEnabled = mfaEnabled;
    }

    @Override
    public OAuth2Client getClient()
    {
        return client;
    }

    @Override
    public Session getSession()
    {
        return session;
    }

    @Override
    public String getId()
    {
        return Long.toUnsignedString(id);
    }

    @Override
    public long getIdLong()
    {
        return id;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getEmail()
    {
        if(Scope.contains(getSession().getScopes(), Scope.EMAIL))
            throw new MissingScopeException("get email for user", Scope.EMAIL);
        return email;
    }
    
    @Override
    public boolean isVerified()
    {
        return verified;
    }
    
    @Override
    public boolean isMfaEnabled()
    {
        return mfaEnabled;
    }

    @Override
    public String getDiscriminator()
    {
        return discriminator;
    }

    @Override
    public String getAvatarId()
    {
        return avatar;
    }

    @Override
    public String getAvatarUrl()
    {
        return getAvatarId() == null ? null : "https://cdn.discordapp.com/avatars/" + getId() + "/" + getAvatarId()
            + (getAvatarId().startsWith("a_") ? ".gif" : ".png");
    }

    @Override
    public String getDefaultAvatarId()
    {
        return DEFAULT_AVATARS[Integer.parseInt(getDiscriminator()) % DEFAULT_AVATARS.length];
    }

    @Override
    public String getDefaultAvatarUrl()
    {
        return "https://discordapp.com/assets/" + getDefaultAvatarId() + ".png";
    }

    @Override
    public String getEffectiveAvatarUrl()
    {
        return getAvatarUrl() == null ? getDefaultAvatarUrl() : getAvatarUrl();
    }

    @Override
    public String getAsMention()
    {
        return "<@" + id + '>';
    }
    
    @Override
    public User getJDAUser(JDA jda)
    {
        return jda.getUserById(id);
    }
    
    @Override
    public User getJDAUser(ShardManager shardManager)
    {
        return shardManager.getUserById(id);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof OAuth2UserImpl))
            return false;
        OAuth2UserImpl oUser = (OAuth2UserImpl) o;
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

    private static final String[] DEFAULT_AVATARS = new String[] {
        "6debd47ed13483642cf09e832ed0bc1b",
        "322c936a8c8be1b803cd94861bdfa868",
        "dd4dbc0016779df1378e7812eabaa04d",
        "0e291f67c9274a1abdddeb3fd919cbaa",
        "1cbd08c76f8af6dddce02c5138971129"
    };
}
