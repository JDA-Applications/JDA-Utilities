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

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * A basic frame that is optionally implementable by objects returned from
 * {@link GuildSettingsManager#getSettings(net.dv8tion.jda.core.entities.Guild)
 * GuildSettingsManager#getSettings(Guild)}.
 *
 * <p>This interface allows the specification of any of the following functions:
 * <ul>
 *     <li>Guild Specific Prefixes (via {@link com.jagrosh.jdautilities.command.GuildSettingsProvider#getPrefixes()})</li>
 * </ul>
 *
 * Note that all of these functions are <b>OPTIONAL</b> to implement, and instructions
 * are available in method documentation on how to implement properly.
 * <br>Additionally, as stated before, the interface itself does not need to be
 * implemented for an object to be returned handled by a GuildSettingsManager.
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
public interface GuildSettingsProvider
{
    /**
     * Gets a {@link java.util.Collection Collection} of String prefixes available
     * for the Guild represented by this implementation.
     *
     * <p>An empty Collection or {@code null} may be returned to signify the Guild
     * doesn't have any guild specific prefixes, or that this feature is not supported
     * by this implementation.
     *
     * @return A Collection of String prefixes for the Guild represented by this
     *         implementation, or {@code null} to signify it has none or that the
     *         feature is not supported by the implementation.
     */
    @Nullable
    default Collection<String> getPrefixes()
    {
        return null;
    }
}
