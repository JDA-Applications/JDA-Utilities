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
package com.jagrosh.jdautilities.command;

import net.dv8tion.jda.core.entities.Guild;

import javax.annotation.Nullable;

/**
 * An implementable frame for classes that handle Guild-Specific
 * settings.
 *
 * <p>Standard implementations should be able to simply provide a
 * type of {@link java.lang.Object Object} provided a non-null
 * {@link net.dv8tion.jda.core.entities.Guild Guild}. Further
 * customization of the implementation is allowed on the developer
 * end.
 *
 * @param  <T>
 *         The specific type of the settings object.
 *
 * @since  2.0
 * @author Kaidan Gustave
 *
 * @implNote
 *         Unless in the event of a major breaking change to
 *         JDA, there is no chance of implementations of this
 *         interface being required to implement additional
 *         methods.
 *         <br>If in the future it is decided to add a method
 *         to this interface, the method will have a default
 *         implementation that doesn't require developer additions.
 */
public interface GuildSettingsManager<T>
{
    /**
     * Gets settings for a specified {@link net.dv8tion.jda.core.entities.Guild Guild}
     * as an object of the specified type {@code T}, or {@code null} if the guild has no
     * settings.
     *
     * @param  guild
     *         The guild to get settings for.
     *
     * @return The settings object for the guild, or {@code null} if the guild has no settings.
     */
    @Nullable
    T getSettings(Guild guild);

    /**
     * Called when JDA has fired a {@link net.dv8tion.jda.core.events.ReadyEvent ReadyEvent}.
     *
     * <p>Developers should implement this method to create or initialize resources when starting their bot.
     */
    default void init() {}

    /**
     * Called when JDA has fired a {@link net.dv8tion.jda.core.events.ShutdownEvent ShutdownEvent}.
     *
     * <p>Developers should implement this method to free up or close resources when shutting their bot.
     */
    default void shutdown() {}
}
