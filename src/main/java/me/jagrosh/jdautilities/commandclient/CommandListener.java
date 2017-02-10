/*
 * Copyright 2016 John Grosh (jagrosh).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.jagrosh.jdautilities.commandclient;

import me.jagrosh.jdautilities.commandclient.annotated.OnCommandCompleted;
import me.jagrosh.jdautilities.commandclient.annotated.OnCommandEvent;
import me.jagrosh.jdautilities.commandclient.annotated.OnCommandTerminated;
import me.jagrosh.jdautilities.commandclient.annotated.OnUnprocessedMessage;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * @author John Grosh (jagrosh)
 */
public interface CommandListener {

    @OnCommandEvent
    public void onCommand(CommandEvent event, Command command);

    @OnCommandCompleted
    public void onCompletedCommand(CommandEvent event, Command command);

    @OnCommandTerminated
    public void onTerminatedCommand(CommandEvent event, Command command);

    @OnUnprocessedMessage
    public void onNonCommandMessage(MessageReceivedEvent event);
}
