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

import com.jagrosh.jdautilities.commons.l10n.Localization;

/**
 *
 * @since  2.2.0
 * @author John Grosh
 */
public enum Messages implements Localization
{
    COMMAND_BOTPERM("command.botperm", "I need the {0} permission in this {1}!"),
    COMMAND_COOLDOWN("command.cooldown", "That command is on cooldown for {0} more seconds"),
    COMMAND_INVOICE("command.invoice", "You must be in a voice channel to use that!"),
    COMMAND_NEEDROLE("command.needrole", "You must have a role called `{0}` to use that!"),
    COMMAND_NOCHANNEL("command.nochannel", "That command cannot be used in this channel!"),
    COMMAND_NODM("command.nodm", "This command cannot be used in Direct messages"),
    COMMAND_USERPERM("command.userperm", "You must have the {0} permission in this {1} to use that!"),
    
    COOLDOWN_CHANNEL("cooldown.channel", "in this channel!"),
    COOLDOWN_GLOBAL("cooldown.global", "globally!"),
    COOLDOWN_GUILD("cooldown.guild", "in this server!"),
    COOLDOWN_SHARD("cooldown.shard", "on this shard!"),
    COOLDOWN_USER("cooldown.user", "!");
    
    private final String key, defaultText;
    
    Messages(String key, String defaultText)
    {
        this.key = key;
        this.defaultText = defaultText;
    }
    
    @Override
    public String getDefaultText()
    {
        return defaultText;
    }

    @Override
    public String getKey()
    {
        return key;
    }
}
