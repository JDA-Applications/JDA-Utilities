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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;

import java.util.EnumSet;

/**
 * OAuth2 representation of a Discord Server/Guild.
 *
 * <p>Note that this is effectively a wrapper for both the Guild info, as well
 * as the info on the user in the guild represented by the session that got this Guild.
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 * @author Kaidan Gustave
 */
public interface OAuth2Guild extends ISnowflake
{
    /**
     * Gets the underlying {@link com.jagrosh.jdautilities.oauth2.OAuth2Client OAuth2Client}
     * that created this OAuth2Guild.
     *
     * @return The OAuth2Client that created this OAuth2Guild.
     */
    OAuth2Client getClient();

    /**
     * Gets the Guild's name.
     *
     * @return The Guild's name.
     */
    String getName();

    /**
     * Gets the Guild's icon ID, or {@code null} if the Guild does not have an icon.
     *
     * @return The Guild's icon ID.
     */
    String getIconId();

    /**
     * Gets the Guild's icon URL, or {@code null} if the Guild does not have an icon.
     *
     * @return The Guild's icon URL.
     */
    String getIconUrl();

    /**
     * Gets the Session User's raw permission value for the Guild.
     *
     * @return The Session User's raw permission value for the Guild.
     */
    int getPermissionsRaw();

    /**
     * Gets the Session User's {@link net.dv8tion.jda.api.Permission Permissions} for the Guild.
     *
     * @return The Session User's Permissions for the Guild.
     */
    EnumSet<Permission> getPermissions();

    /**
     * Whether or not the Session User is the owner of the Guild.
     *
     * @return {@code true} if the Session User is the owner of
     *         the Guild, {@code false} otherwise.
     */
    boolean isOwner();

    /**
     * Whether or not the Session User has all of the specified
     * {@link net.dv8tion.jda.api.Permission Permissions} in the Guild.
     *
     * @param  perms
     *         The Permissions to check for.
     *
     * @return {@code true} if and only if the Session User has all of the
     *         specified Permissions, {@code false} otherwise.
     */
    boolean hasPermission(Permission... perms);
}
