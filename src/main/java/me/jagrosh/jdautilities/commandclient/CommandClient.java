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

import java.time.OffsetDateTime;
import java.util.List;

/**
 *
 * @author John Grosh (jagrosh)
 */
public interface CommandClient {
    
    /**
     * Gets the Client's prefix
     * @return possibly-null prefix
     */
    public String getPrefix();
    
    /**
     * Returns the visual representation of the bot's prefix. This is the same
     * as getPrefix unless the prefix is the default, in which case it appears
     * as @Botname
     * @return never-null prefix
     */
    public String getTextualPrefix();
    
    /**
     * Sets the Command Listener to listen for command-related events
     * @param listener the command listener
     */
    public void setListener(CommandListener listener);

    public void setAnnotatedListener(Object listener);

    /**
     * Returns the current CommandListener
     * @return possibly-null CommandListener
     */
    public CommandListener getListener();
    
    /**
     * Returns the list of registered commands
     * @return never-null list
     */
    public List<Command> getCommands();

    public List<Object> getCommandConsumers();
    
    /**
     * Gets the time this CommandClientImpl was instantiated
     * @return the start time of this CommandClientImpl
     */
    public OffsetDateTime getStartTime();
    
    /**
     * Gets the time that the specified cooldown expires
     * @param name the cooldown name
     * @return the expiration time, or null if the cooldown does not exist
     */
    public OffsetDateTime getCooldown(String name);
    
    /**
     * Gets the remaining time on the specified cooldown
     * @param name the cooldown name
     * @return the time remaining
     */
    public int getRemainingCooldown(String name);
    
    /**
     * Applies the specified cooldown
     * @param name the cooldown name
     * @param seconds the time to make the cooldown last
     */
    public void applyCooldown(String name, int seconds);
    
    /**
     * Cleans up expired cooldowns to reduce memory
     */
    public void cleanCooldowns();
    
    /**
     * Gets the ID of the owner of this bot
     * @return the ID of the owner of the bot
     */
    public String getOwnerId();
    
    /**
     * Gets the success emoji
     * @return the success emoji
     */
    public String getSuccess();
    
    /**
     * Gets the warning emoji
     * @return the warning emoji
     */
    public String getWarning();
    
    /**
     * Gets the error emoji
     * @return the error emoji
     */
    public String getError();
    
    /**
     * Gets the invite to the bot's support server
     * @return possibly-null server invite
     */
    public String getServerInvite();
}
