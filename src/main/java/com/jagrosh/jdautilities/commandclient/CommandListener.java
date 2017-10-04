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
package com.jagrosh.jdautilities.commandclient;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * An implementable "Listener" that can be added to a {@link com.jagrosh.jdautilities.commandclient.CommandClient CommandClient}
 * and used to handle events relating to {@link com.jagrosh.jdautilities.commandclient.Command Command}s.
 * 
 * @author John Grosh (jagrosh)
 */
public interface CommandListener
{
    /**
     * A method that is called when a {@link com.jagrosh.jdautilities.commandclient.Command Command}
     * is triggered by a {@link com.jagrosh.jdautilities.commandclient.CommandEvent CommandEvent}.
     * 
     * @param  event
     *         The CommandEvent that triggered the Command
     * @param  command
     *         The Command that was triggered
     */
    void onCommand(CommandEvent event, Command command);
    
    /**
     * A method that is called when a {@link com.jagrosh.jdautilities.commandclient.Command Command}
     * is triggered by a {@link com.jagrosh.jdautilities.commandclient.CommandEvent CommandEvent}
     * after it's completed successfully.
     * 
     * @param  event
     *         The CommandEvent that triggered the Command
     * @param  command
     *         The Command that was triggered
     */
    void onCompletedCommand(CommandEvent event, Command command);
    
    /**
     * A method that is called when a {@link com.jagrosh.jdautilities.commandclient.Command Command}
     * is triggered by a {@link com.jagrosh.jdautilities.commandclient.CommandEvent CommandEvent} but
     * is terminated before completion.
     * 
     * @param  event
     *         The CommandEvent that triggered the Command
     * @param  command
     *         The Command that was triggered
     */
    void onTerminatedCommand(CommandEvent event, Command command);
    
    /**
     * A method that is called whenever a 
     * {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent} is caught by the Client Listener's
     * {@link net.dv8tion.jda.core.hooks.ListenerAdapter#onMessageReceived(MessageReceivedEvent) ListenerAdapter#onMessageReceived(MessageReceivedEvent)}
     * but doesn't correspond to a {@link com.jagrosh.jdautilities.commandclient.Command Command}.
     * 
     * <p>In other words, this catches all <b>non-command</b> MessageReceivedEvents allowing you to handle them without
     * implementation of another listener.
     * 
     * @param  event
     *         A MessageReceivedEvent that wasn't used to call a Command
     */
    void onNonCommandMessage(MessageReceivedEvent event);
}
