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

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * An implementable "Listener" that can be added to a {@link com.jagrosh.jdautilities.command.CommandClient CommandClient}
 * and used to handle events relating to {@link com.jagrosh.jdautilities.command.Command Command}s.
 * 
 * @author John Grosh (jagrosh)
 */
public interface CommandListener
{
    /**
     * Called when a {@link com.jagrosh.jdautilities.command.Command Command} is triggered
     * by a {@link com.jagrosh.jdautilities.command.CommandEvent CommandEvent}.
     * 
     * @param  event
     *         The CommandEvent that triggered the Command
     * @param  command
     *         The Command that was triggered
     */
    default void onCommand(CommandEvent event, Command command) {}
    
    /**
     * Called when a {@link com.jagrosh.jdautilities.command.Command Command} is triggered
     * by a {@link com.jagrosh.jdautilities.command.CommandEvent CommandEvent} after it's
     * completed successfully.
     *
     * <p>Note that a <i>successfully</i> completed command is one that has not encountered
     * an error or exception. Calls that do face errors should be handled by
     * {@link CommandListener#onCommandException(CommandEvent, Command, Throwable) CommandListener#onCommandException}
     * 
     * @param  event
     *         The CommandEvent that triggered the Command
     * @param  command
     *         The Command that was triggered
     */
    default void onCompletedCommand(CommandEvent event, Command command) {}
    
    /**
     * Called when a {@link com.jagrosh.jdautilities.command.Command Command} is triggered
     * by a {@link com.jagrosh.jdautilities.command.CommandEvent CommandEvent} but is
     * terminated before completion.
     * 
     * @param  event
     *         The CommandEvent that triggered the Command
     * @param  command
     *         The Command that was triggered
     */
    default void onTerminatedCommand(CommandEvent event, Command command) {}
    
    /**
     * Called when a {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     * is caught by the Client Listener's but doesn't correspond to a
     * {@link com.jagrosh.jdautilities.command.Command Command}.
     * 
     * <p>In other words, this catches all <b>non-command</b> MessageReceivedEvents allowing
     * you to handle them without implementation of another listener.
     * 
     * @param  event
     *         A MessageReceivedEvent that wasn't used to call a Command
     */
    default void onNonCommandMessage(MessageReceivedEvent event) {}

    /**
     * Called when a {@link com.jagrosh.jdautilities.command.Command Command}
     * catches a {@link java.lang.Throwable Throwable} <b>during execution</b>.
     *
     * <p>This doesn't account for exceptions thrown during other pre-checks,
     * and should not be treated as such!
     *
     * <p>An example of this misconception is via a
     * {@link com.jagrosh.jdautilities.command.Command.Category Category} test:
     *
     * <pre><code> public class BadCommand extends Command {
     *
     *      public BadCommand() {
     *          this.name = "bad";
     *          this.category = new Category("bad category", event {@literal ->} {
     *              // This will throw a NullPointerException if it's not from a Guild!
     *              return event.getGuild().getIdLong() == 12345678910111213;
     *          });
     *      }
     *
     *      {@literal @Override}
     *      protected void execute(CommandEvent) {
     *          event.reply("This is a bad command!");
     *      }
     *
     * }</code></pre>
     *
     * The {@link java.lang.NullPointerException NullPointerException} thrown will not be caught by this method!
     *
     * @param  event
     *         The CommandEvent that triggered the Command
     * @param  command
     *         The Command that was triggered
     * @param  throwable
     *         The Throwable thrown during Command execution
     */
    default void onCommandException(CommandEvent event, Command command, Throwable throwable) {
        // Default rethrow as a runtime exception.
        throw throwable instanceof RuntimeException? (RuntimeException)throwable : new RuntimeException(throwable);
    }
}
