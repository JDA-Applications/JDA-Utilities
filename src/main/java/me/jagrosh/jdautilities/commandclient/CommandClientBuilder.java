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

import me.jagrosh.jdautilities.commandclient.impl.CommandClientImpl;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Function;
import net.dv8tion.jda.core.entities.Game;

/**
 *
 * @author John Grosh (jagrosh)
 */
public class CommandClientBuilder {
    private Game game = Game.of("default");
    private String ownerId;
    private String prefix;
    private String serverInvite;
    private String success;
    private String warning;
    private String error;
    private String carbonKey;
    private String botsKey;
    private final LinkedList<Command> commands = new LinkedList<>();
    private CommandListener listener;
    private Function<CommandEvent,String> helpFunction;
    private String helpWord;
    
    /**
     * Builds a CommandClientImpl with the provided settings
     * @return a CommandClientImpl
     */
    public CommandClient build()
    {
        CommandClient client = new CommandClientImpl(ownerId, prefix, game, serverInvite, success, warning, error, carbonKey, botsKey, new ArrayList<>(commands), helpFunction, helpWord);
        if(listener!=null)
            client.setListener(listener);
        return client;
    }
    
    /**
     * Sets the owner for the bot
     * @param ownerId the id of the owner
     * @return the builder
     */
    public CommandClientBuilder setOwnerId(String ownerId)
    {
        this.ownerId = ownerId;
        return this;
    }
    
    /**
     * Sets the bot's prefix. If null, the bot will use a mention as a prefix
     * @param prefix the prefix
     * @return the builder
     */
    public CommandClientBuilder setPrefix(String prefix)
    {
        this.prefix = prefix;
        return this;
    }
    
    /**
     * Sets the function to build the bot's help command. If null, it will
     * use the default help function builder
     * @param helpFunction a function to convert a commandevent to a help message
     * @return the builder
     */
    public CommandClientBuilder setHelpFunction(Function<CommandEvent,String> helpFunction)
    {
        this.helpFunction = helpFunction;
        return this;
    }
    
    /**
     * Sets the word used to trigger the command list. If null, it will use 'help'
     * @param helpWord - the word to trigger the command list
     * @return the builder
     */
    public CommandClientBuilder setHelpWord(String helpWord)
    {
        this.helpWord = helpWord;
        return this;
    }
    
    /**
     * Sets the bot's support server invite
     * @param serverInvite the support server invite
     * @return the builder
     */
    public CommandClientBuilder setServerInvite(String serverInvite)
    {
        this.serverInvite = serverInvite;
        return this;
    }
    
    /**
     * Sets the emojis for success, warning, and failure
     * @param success emoji for success
     * @param warning emoji for warning
     * @param error emoji for failure
     * @return the builder
     */
    public CommandClientBuilder setEmojis(String success, String warning, String error)
    {
        this.success = success;
        this.warning = warning;
        this.error = error;
        return this;
    }
    
    /**
     * Sets the game to use when the bot is ready. Set to null for no game
     * @param game the game to use when the bot is ready
     * @return the builder
     */
    public CommandClientBuilder setGame(Game game)
    {
        this.game = game;
        return this;
    }
    
    /**
     * Uses the default game, 'Type [prefix]help'
     * @return the builder
     */
    public CommandClientBuilder useDefaultGame()
    {
        this.game = Game.of("default");
        return this;
    }
    
    /**
     * Adds a command
     * @param command the command to add
     * @return the builder
     */
    public CommandClientBuilder addCommand(Command command)
    {
        commands.add(command);
        return this;
    }
    
    /**
     * Adds multiple commands. This is the same as calling addCommand multiple times
     * @param commands the commands to add
     * @return the builder
     */
    public CommandClientBuilder addCommands(Command... commands)
    {
        for(Command command: commands)
            this.addCommand(command);
        return this;
    }
    
    /**
     * Sets a key for Carbonitex for updating server count
     * @param key a Carbonitex key
     * @return the builder
     */
    public CommandClientBuilder setCarbonitexKey(String key)
    {
        this.carbonKey = key;
        return this;
    }
    
    /**
     * Sets a key for the Discord Bots listing for updating server count
     * @param key A bots.discord.pw API key
     * @return the builder
     */
    public CommandClientBuilder setDiscordBotsKey(String key)
    {
        this.botsKey = key;
        return this;
    }
    
    /**
     * Sets the CommandListener for the CommandClientImpl
     * @param listener the CommandListener for the CommandClientImpl
     * @return the builder
     */
    public CommandClientBuilder setListener(CommandListener listener)
    {
        this.listener = listener;
        return this;
    }
}
