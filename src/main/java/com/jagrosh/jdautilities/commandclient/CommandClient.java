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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.core.requests.RestAction;

/**
 * A Bot Client interface implemented on objects used to hold bot data.
 * 
 * <p>This is implemented in {@link com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl CommandClientImpl}
 * alongside inheritance of {@link net.dv8tion.jda.core.hooks.ListenerAdapter ListenerAdapter} to create a
 * compounded "ClientListener" which catches specific kinds of events thrown by JDA and processes them
 * automatically to handle and execute {@link com.jagrosh.jdautilities.commandclient.Command Command}s
 * 
 * @author John Grosh (jagrosh)
 */
public interface CommandClient {
    
    /**
     * Gets the Client's prefix.
     * 
     * @return A possibly-null prefix
     */
    String getPrefix();
    
    /**
     * Returns the visual representation of the bot's prefix. 
     * 
     * <p>This is the same as {@link CommandClient#getPrefix()} unless the prefix is the default,
     * in which case it appears as {@literal @Botname}.
     * 
     * @return A never-null prefix
     */
    String getTextualPrefix();

    /**
     * Adds a single {@link com.jagrosh.jdautilities.commandclient.Command Command} to this CommandClient's
     * registered Commands.
     *
     * <p>For CommandClient's containing 20 commands or less, command calls by users will have the bot iterate
     * through the entire {@link java.util.ArrayList ArrayList} to find the command called. As expected, this
     * can get fairly hefty if a bot has a lot of Commands registered to it.
     *
     * <p>To prevent delay a CommandClient that has more that 20 Commands registered to it will begin to use
     * <b>indexed calls</b>.
     * <br>Indexed calls use a {@link java.util.HashMap HashMap} which links their
     * {@link com.jagrosh.jdautilities.commandclient.Command#name name} and their
     * {@link com.jagrosh.jdautilities.commandclient.Command#aliases aliases} to the index that which they
     * are located at in the ArrayList they are stored.
     *
     * <p>This means that all insertion and removal of Commands must reorganize the index maintained by the HashMap.
     * <br>For this particular insertion, the Command provided is inserted at the end of the index, meaning it will
     * become the "rightmost" Command in the ArrayList.
     *
     * @param  command
     *         The Command to add
     *
     * @throws java.lang.IllegalArgumentException
     *         If the Command provided has a name or alias that has already been registered
     */
    void addCommand(Command command);

    /**
     * Adds a single {@link com.jagrosh.jdautilities.commandclient.Command Command} to this CommandClient's
     * registered Commands at the specified index.
     *
     * <p>For CommandClient's containing 20 commands or less, command calls by users will have the bot iterate
     * through the entire {@link java.util.ArrayList ArrayList} to find the command called. As expected, this
     * can get fairly hefty if a bot has a lot of Commands registered to it.
     *
     * <p>To prevent delay a CommandClient that has more that 20 Commands registered to it will begin to use
     * <b>indexed calls</b>.
     * <br>Indexed calls use a {@link java.util.HashMap HashMap} which links their
     * {@link com.jagrosh.jdautilities.commandclient.Command#name name} and their
     * {@link com.jagrosh.jdautilities.commandclient.Command#aliases aliases} to the index that which they
     * are located at in the ArrayList they are stored.
     *
     * <p>This means that all insertion and removal of Commands must reorganize the index maintained by the HashMap.
     * <br>For this particular insertion, the Command provided is inserted at the index specified, meaning it will
     * become the Command located at that index in the ArrayList. This will shift the Command previously located at
     * that index as well as any located at greater indices, right one index ({@code size()+1}).
     *
     * @param  command
     *         The Command to add
     * @param  index
     *         The index to add the Command at (must follow the specifications {@code 0<=index<=size()})
     *
     * @throws java.lang.ArrayIndexOutOfBoundsException
     *         If {@code index < 0} or {@code index > size()}
     * @throws java.lang.IllegalArgumentException
     *         If the Command provided has a name or alias that has already been registered to an index
     */
    void addCommand(Command command, int index);

    /**
     * Removes a single {@link com.jagrosh.jdautilities.commandclient.Command Command} from this CommandClient's
     * registered Commands at the index linked to the provided name/alias.
     *
     * <p>For CommandClient's containing 20 commands or less, command calls by users will have the bot iterate
     * through the entire {@link java.util.ArrayList ArrayList} to find the command called. As expected, this
     * can get fairly hefty if a bot has a lot of Commands registered to it.
     *
     * <p>To prevent delay a CommandClient that has more that 20 Commands registered to it will begin to use
     * <b>indexed calls</b>.
     * <br>Indexed calls use a {@link java.util.HashMap HashMap} which links their
     * {@link com.jagrosh.jdautilities.commandclient.Command#name name} and their
     * {@link com.jagrosh.jdautilities.commandclient.Command#aliases aliases} to the index that which they
     * are located at in the ArrayList they are stored.
     *
     * <p>This means that all insertion and removal of Commands must reorganize the index maintained by the HashMap.
     * <br>For this particular removal, the Command removed is that of the corresponding index retrieved by the name
     * provided. This will shift any Commands located at greater indices, left one index ({@code size()-1}).
     *
     * @param  name
     *         The name or an alias of the Command to remove
     *
     * @throws java.lang.IllegalArgumentException
     *         If the name provided was not registered to an index
     */
    void removeCommand(String name);

    /**
     * Sets the {@link com.jagrosh.jdautilities.commandclient.CommandListener CommandListener} to catch 
     * command-related events thrown by this {@link com.jagrosh.jdautilities.commandclient.CommandClient CommandClient}.
     * 
     * @param  listener
     *         The CommandListener
     */
    void setListener(CommandListener listener);
    
    /**
     * Returns the current {@link com.jagrosh.jdautilities.commandclient.CommandListener CommandListener}.
     * 
     * @return A possibly-null CommandListener
     */
    CommandListener getListener();
    
    /**
     * Returns the list of registered {@link com.jagrosh.jdautilities.commandclient.Command Command}s 
     * during this session.
     * 
     * @return A never-null List of Commands registered during this session
     */
    List<Command> getCommands();

    /**
     * Gets the time this {@link com.jagrosh.jdautilities.commandclient.CommandClient CommandClient} 
     * implementation was created.
     * 
     * @return The start time of this CommandClient implementation
     */
    OffsetDateTime getStartTime();
    
    /**
     * Gets the {@link java.time.OffsetDateTime OffsetDateTime} that the specified cooldown expires.
     * 
     * @param  name
     *         The cooldown name
     *         
     * @return The expiration time, or null if the cooldown does not exist
     */
    OffsetDateTime getCooldown(String name);
    
    /**
     * Gets the remaining number of seconds on the specified cooldown.
     * 
     * @param  name
     *         The cooldown name
     *         
     * @return The number of seconds remaining
     */
    int getRemainingCooldown(String name);
    
    /**
     * Applies the specified cooldown with the provided name.
     * 
     * @param  name
     *         The cooldown name
     * @param  seconds
     *         The time to make the cooldown last
     */
    void applyCooldown(String name, int seconds);
    
    /**
     * Cleans up expired cooldowns to reduce memory.
     */
    void cleanCooldowns();
    
    /**
     * Gets the number of uses for the provide {@link com.jagrosh.jdautilities.commandclient.Command Command}
     * during this session, or {@code 0} if the command is not registered to this CommandClient.
     * 
     * @param  command 
     *         The Command
     *         
     * @return The number of uses for the Command
     */
    int getCommandUses(Command command);
    
    /**
     * Gets the number of uses for a {@link com.jagrosh.jdautilities.commandclient.Command Command} 
     * during this session matching the provided String name, or {@code 0} if there is no Command 
     * with the name.
     *
     * <p><b>NOTE:</b> this method <b>WILL NOT</b> get uses for a command if an
     * {@link com.jagrosh.jdautilities.commandclient.Command#aliases alias} is provided! Also note that
     * {@link com.jagrosh.jdautilities.commandclient.Command#children child commands} <b>ARE NOT</b>
     * tracked and providing names or effective names of child commands will return {@code 0}.
     * 
     * @param  name
     *         The name of the Command
     *         
     * @return The number of uses for the Command, or {@code 0} if the name does not match with a 
     *         Command
     */
    int getCommandUses(String name);
    
    /**
     * Gets the ID of the owner of this bot as a String.
     * 
     * @return The String ID of the owner of the bot
     */
    String getOwnerId();
    
    /**
     * Gets the ID of the owner of this bot as a {@code long}.
     * 
     * @return The {@code long} ID of the owner of the bot
     */
    long getOwnerIdLong();
    
    /**
     * Gets the ID(s) of any CoOwners of this bot as a String Array.
     * 
     * @return The String ID(s) of any CoOwners of this bot
     */
    String[] getCoOwnerIds();
    
    /**
     * Gets the ID(s) of any CoOwners of this bot as a {@code long} Array.
     * 
     * @return The {@code long} ID(s) of any CoOwners of this bot
     */
    long[] getCoOwnerIdsLong();
    
    /**
     * Gets the success emoji.
     * 
     * @return The success emoji
     */
    String getSuccess();
    
    /**
     * Gets the warning emoji.
     * 
     * @return The warning emoji
     */
    String getWarning();
    
    /**
     * Gets the error emoji.
     * 
     * @return The error emoji
     */
    String getError();
    
    /**
     * Gets the invite to the bot's support server.
     * 
     * @return A possibly-null server invite
     */
    String getServerInvite();
    
    /**
     * Gets an a recently updated count of all the {@link net.dv8tion.jda.core.entities.Guild Guild}s 
     * the bot is connected to on all shards.
     * 
     * <p><b>NOTE:</b> This may not always or should not be assumed accurate! Any time
     * a shard joins or leaves a guild it will update the number retrieved by this method
     * but will not update when other shards join or leave guilds. This means that shards
     * will not always retrieve the same value. For instance:
     * <ul>
     *     <li>1) Shard A joins 10 Guilds</li>
     *     <li>2) Shard B invokes this method</li>
     *     <li>3) Shard A invokes this method</li>
     * </ul>
     * The number retrieved by Shard B will be that of the number retrieved by Shard A,
     * minus 10 guilds because Shard B hasn't updated and accounted for those 10 guilds
     * on Shard A.
     * 
     * <p><b>This feature requires a Discord Bots API Key to be set!</b>
     * <br>To set your Discord Bots API Key, you'll have to retrieve it from the
     * <a href="http://bots.discord.pw/">Discord Bots</a> website.
     * 
     * @return A recently updated count of all the Guilds the bot is connected to on
     *         all shards.
     */
    int getTotalGuilds();
    
    /**
     * Gets the word used to invoke a help DM.
     * 
     * @return The help word
     */
    String getHelpWord();
    
    /**
     * Schedules a {@link net.dv8tion.jda.core.requests.RestAction RestAction} to occur in a provided delay
     * of <b>seconds</b>.
     * 
     * <p>This is more useful than {@link CommandClient#schedule(String, int, TimeUnit, RestAction)} when dealing with 
     * short delays as it is simple, easy, and would not require the operating class to {@code import}
     * {@link java.util.concurrent.TimeUnit}.
     * 
     * <p>This allows it to be cancelled later using {@link com.jagrosh.jdautilities.commandclient.CommandClient#cancel(String) CommandClient#cancel(String)}.
     * 
     * @param  name
     *         The name of the scheduled RestAction (can be used to cancel it later if needed)
     * @param  delay
     *         The amount of seconds to delay for
     * @param  toQueue
     *         The RestAction to queue after the delay
     */
    <T> void schedule(String name, int delay, RestAction<T> toQueue);
    
    /**
     * Schedules a {@link java.lang.Runnable Runnable} to run in a provided delay of <b>seconds</b>.
     * 
     * <p>This is more useful than {@link CommandClient#schedule(String, int, TimeUnit, Runnable)} when dealing with
     * short delays as it is simple, easy, and would not require the operating class to {@code import}
     * {@link java.util.concurrent.TimeUnit TimeUnit}.
     * 
     * <p>This allows it to be cancelled later using {@link com.jagrosh.jdautilities.commandclient.CommandClient#cancel(String) CommandClient#cancel(String)}.
     * 
     * @param  name 
     *         The name of the scheduled Runnable (can be used to cancel it later if needed)
     * @param  delay 
     *         The the amount of seconds to delay for
     * @param  runnable 
     *         The Runnable to run after the delay
     */
    void schedule(String name, int delay, Runnable runnable);
    
    /**
     * Schedules a {@link net.dv8tion.jda.core.requests.RestAction RestAction} to occur in the provided delay of 
     * {@link java.util.concurrent.TimeUnit TimeUnit}.
     * 
     * <p>This allows it to be cancelled later using {@link com.jagrosh.jdautilities.commandclient.CommandClient#cancel(String) CommandClient#cancel(String)}.
     * 
     * @param  name
     *         The name of the scheduled RestAction (can be used to cancel it later if needed)
     * @param  delay
     *         The amount to delay for
     * @param  unit
     *         The unit to measure the delay with
     * @param  toQueue
     *         The RestAction to queue after the delay
     */
    <T> void schedule(String name, int delay, TimeUnit unit, RestAction<T> toQueue);
    
    /**
     * Schedules a {@link java.lang.Runnable Runnable} to run in a provided delay of
     * {@link java.util.concurrent.TimeUnit TimeUnit}.
     * 
     * <p>This allows it to be cancelled later using {@link com.jagrosh.jdautilities.commandclient.CommandClient#cancel(String) CommandClient#cancel(String)}.
     * 
     * @param  name
     *         The name of the scheduled Runnable (can be used to cancel it later if needed)
     * @param  delay
     *         The amount to delay for
     * @param  unit
     *         The unit to measure the delay with
     * @param  runnable
     *         The Runnable to run after the delay
     */
    void schedule(String name, int delay, TimeUnit unit, Runnable runnable);
    
    /**
     * Saves a {@link java.util.concurrent.ScheduledFuture ScheduledFuture} to a provided name.
     * 
     * <p>This allows it to be cancelled later using {@link com.jagrosh.jdautilities.commandclient.CommandClient#cancel(String) CommandClient#cancel(String)}.
     * 
     * @param  name
     *         The name of the ScheduledFuture (can be used to cancel it later if needed)
     * @param  future
     *         The ScheduledFuture to save
     */
    void saveFuture(String name, ScheduledFuture<?> future);
    
    /**
     * Checks if a {@link java.util.concurrent.ScheduledFuture ScheduledFuture} exists
     * corresponding to the provided name.
     * 
     * <p><b>NOTE:</b> This method will <b>NOT</b> take into account whether or not the provided name finds 
     * a ScheduledFuture that has already occurred or has been cancelled. To detect if the schedule only 
     * contains a "live" ScheduledFuture going by the name provided, invoking 
     * {@link com.jagrosh.jdautilities.commandclient.CommandClient#cleanSchedule() CommandClient#cleanSchedule()} 
     * beforehand may provide more accurate results.
     * 
     * @param  name
     *         The name of the ScheduledFuture
     *         
     * @return {@code true} if there exists a ScheduledFuture corresponding to the provided name 
     *         (regardless of it's possible cancellation or expiration), otherwise {@code false}.
     */
    boolean scheduleContains(String name);
    
    /**
     * Cancels a {@link java.util.concurrent.ScheduledFuture ScheduledFuture} corresponding to the provided name.
     * 
     * <p>This will not cancel the ScheduledFuture if it is running or has already occurred. To perform a
     * cancellation even in mid-operation use {@link com.jagrosh.jdautilities.commandclient.CommandClient#cancelImmediately(String) CommandClient#cancelImmediately(String)}.
     * 
     * @param  name 
     *         The name of the ScheduledFuture
     */
    void cancel(String name);
    
    /**
     * Cancels a {@link java.util.concurrent.ScheduledFuture ScheduledFuture} corresponding to the provided name,
     * possibly in the midst of it running.
     * 
     * <p>This will cancel a ScheduledFuture, even if it is currently running, but will not
     * if the ScheduledFuture has already occurred.
     * 
     * @param  name
     *         The name of the ScheduledFuture to be cancelled immediately
     */
    void cancelImmediately(String name);
    
    /**
     * Gets a {@link java.util.concurrent.ScheduledFuture ScheduledFuture} corresponding to the provided name.
     * 
     * @param  name 
     *         The name of the ScheduledFuture to get
     *         
     * @return The ScheduledFuture corresponding to the provided name
     */
    ScheduledFuture<?> getScheduledFuture(String name);
    
    /**
     * Cleans up cancelled and expired {@link java.util.concurrent.ScheduledFuture ScheduledFuture}s to reduce memory.
     */
    void cleanSchedule();

    /**
     * Gets whether this CommandClient uses linked deletion.
     *
     * <p>Linking calls is the basic principle of pairing bot responses with their calling
     * {@link net.dv8tion.jda.core.entities.Message Message}s.
     * <br>Using this with a basic function such as deletion, this causes bots to delete their
     * Messages as a response to the calling Message being deleted.
     *
     * @return {@code true} if the bot uses linked deletion, {@code false} otherwise.
     *
     * @see    com.jagrosh.jdautilities.commandclient.CommandClientBuilder#setLinkedCacheSize(int)
     *         For how to disable or enable linked deletion.
     */
    boolean usesLinkedDeletion();
}
