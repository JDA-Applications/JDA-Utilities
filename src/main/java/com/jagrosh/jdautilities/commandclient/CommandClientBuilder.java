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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import com.jagrosh.jdautilities.commandclient.impl.AnnotatedModuleCompilerImpl;
import com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;

/**
 * A simple builder used to create a {@link com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl CommandClientImpl}.
 * 
 * <p>Once built, add the {@link com.jagrosh.jdautilities.commandclient.CommandClient CommandClient} as an EventListener to
 * {@link net.dv8tion.jda.core.JDA JDA} and it will automatically handle commands with ease!
 * 
 * @author John Grosh (jagrosh)
 *
 * @see    com.jagrosh.jdautilities.commandclient.CommandClientBuilder
 */
public class CommandClientBuilder
{
    private Game game = Game.playing("default");
    private OnlineStatus status = OnlineStatus.ONLINE;
    private String ownerId;
    private String[] coOwnerIds;
    private String prefix;
    private String altprefix;
    private String serverInvite;
    private String success;
    private String warning;
    private String error;
    private String carbonKey;
    private String botsKey;
    private String botsOrgKey;
    private final LinkedList<Command> commands = new LinkedList<>();
    private CommandListener listener;
    private boolean useHelp = true;
    private Function<CommandEvent,String> helpFunction;
    private String helpWord;
    private ScheduledExecutorService executor;
    private int linkedCacheSize = 200;
    private AnnotatedModuleCompiler compiler = new AnnotatedModuleCompilerImpl();
    
    /**
     * Builds a {@link com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl CommandClientImpl} 
     * with the provided settings.
     * <br>Once built, only the {@link com.jagrosh.jdautilities.commandclient.CommandListener CommandListener},
     * and {@link com.jagrosh.jdautilities.commandclient.Command Command}s can be changed.
     * 
     * @return The CommandClient built.
     */
    public CommandClient build()
    {
        CommandClient client = new CommandClientImpl(ownerId, coOwnerIds, prefix, altprefix, game, status, serverInvite,
                success, warning, error, carbonKey, botsKey, botsOrgKey, new ArrayList<>(commands), useHelp,
                helpFunction, helpWord, executor, linkedCacheSize, compiler);
        if(listener!=null)
            client.setListener(listener);
        return client;
    }
    
    /**
     * Sets the owner for the bot.
     * <br>Make sure to verify that the ID provided is ISnowflake compatible when setting this.
     * If it is not, this will warn the developer.
     * 
     * @param  ownerId
     *         The ID of the owner.
     *         
     * @return This builder
     */
    public CommandClientBuilder setOwnerId(String ownerId)
    {
        this.ownerId = ownerId;
        return this;
    }
    
    /**
     * Sets the one or more CoOwners of the bot.
     * <br>Make sure to verify that all of the IDs provided are ISnowflake compatible when setting this.
     * If it is not, this will warn the developer which ones are not.
     * 
     * @param  coOwnerIds
     *         The ID(s) of the CoOwners
     * 
     * @return This builder
     */
    public CommandClientBuilder setCoOwnerIds(String... coOwnerIds)
    {
    	this.coOwnerIds = coOwnerIds;
    	return this;
    }
    
    /**
     * Sets the bot's prefix.
     * <br>If set null, empty, or not set at all, the bot will use a mention {@literal @Botname} as a prefix.
     * 
     * @param  prefix
     *         The prefix for the bot to use
     *         
     * @return This builder
     */
    public CommandClientBuilder setPrefix(String prefix)
    {
        this.prefix = prefix;
        return this;
    }
    
    /**
     * Sets the bot's alternative prefix.
     * <br>If set null, the bot will only use its primary prefix prefix.
     * 
     * @param  prefix
     *         The alternative prefix for the bot to use
     *         
     * @return This builder
     */
    public CommandClientBuilder setAlternativePrefix(String prefix)
    {
        this.altprefix = prefix;
        return this;
    }
    
    /**
     * Sets whether the {@link com.jagrosh.jdautilities.commandclient.CommandClient CommandClient} will use 
     * the builder to automatically create a help command or not.
     * 
     * @param  useHelp
     *         {@code false} to disable the help command builder, otherwise the CommandClient
     *         will use either the default or one provided via {@link #setHelpFunction(Function)}.
     *         
     * @return This builder
     */
    public CommandClientBuilder useHelpBuilder(boolean useHelp)
    {
    	this.useHelp = useHelp;
        return this;
    }
    
    /**
     * Sets the function to build the bot's help command.
     * <br>Setting it to {@code null} or not setting this at all will cause the bot to use 
     * the default help builder.
     * 
     * @param  helpFunction
     *         A function to convert a {@link com.jagrosh.jdautilities.commandclient.CommandEvent CommandEvent} 
     *         to a String for a help DM.
     *
     * @return This builder
     *
     * @deprecated
     *         Scheduled for removal in 2.0, will be replaced with a Consumer instead.
     */
    @SuppressWarnings("DeprecatedIsStillUsed") // Suppress the link in docs
    @Deprecated
    public CommandClientBuilder setHelpFunction(Function<CommandEvent,String> helpFunction)
    {
        this.helpFunction = helpFunction;
        return this;
    }
    
    /**
     * Sets the word used to trigger the command list.
     * <br>Setting this to {@code null} or not setting this at all will set the help word
     * to {@code "help"}.
     * 
     * @param  helpWord
     *         The word to trigger the help command
     *         
     * @return This builder
     */
    public CommandClientBuilder setHelpWord(String helpWord)
    {
        this.helpWord = helpWord;
        return this;
    }
    
    /**
     * Sets the bot's support server invite.
     * 
     * @param  serverInvite
     *         The support server invite
     *         
     * @return This builder
     */
    public CommandClientBuilder setServerInvite(String serverInvite)
    {
        this.serverInvite = serverInvite;
        return this;
    }
    
    /**
     * Sets the emojis for success, warning, and failure.
     * 
     * @param  success
     *         Emoji for success
     * @param  warning
     *         Emoji for warning
     * @param  error
     *         Emoji for failure
     *         
     * @return This builder
     */
    public CommandClientBuilder setEmojis(String success, String warning, String error)
    {
        this.success = success;
        this.warning = warning;
        this.error = error;
        return this;
    }
    
    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Game Game} to use when the bot is ready.
     * <br>Can be set to {@code null} for no game.
     * 
     * @param  game
     *         The Game to use when the bot is ready
     *         
     * @return This builder
     *
     * @deprecated
     *         This features will be removed in 2.0 due to it's availability
     *         through {@link net.dv8tion.jda.core.JDABuilder JDABuilder}
     *         not being more or less efficient than through the CommandClient.
     *         <br>If you want to set your bot's game or status, you may do so through
     *         {@link net.dv8tion.jda.core.JDABuilder#setGame(Game) JDABuilder#setGame(Game)}
     *         or {@link net.dv8tion.jda.core.JDABuilder#setStatus(OnlineStatus)
     *         JDABuilder#setStatus(OnlineStatus)} respectively.
     */
    @Deprecated
    public CommandClientBuilder setGame(Game game)
    {
        this.game = game;
        return this;
    }
    
    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Game Game} being played to display when the bot is ready.
     * 
     * @param  name
     *         Non-null/non-empty name of the game
     *         
     * @return This builder
     *
     * @deprecated
     *         This features will be removed in 2.0 due to it's availability
     *         through {@link net.dv8tion.jda.core.JDABuilder JDABuilder}
     *         not being more or less efficient than through the CommandClient.
     *         <br>If you want to set your bot's game or status, you may do so through
     *         {@link net.dv8tion.jda.core.JDABuilder#setGame(Game) JDABuilder#setGame(Game)}
     *         or {@link net.dv8tion.jda.core.JDABuilder#setStatus(OnlineStatus)
     *         JDABuilder#setStatus(OnlineStatus)} respectively.
     */
    @Deprecated
    public CommandClientBuilder setPlaying(String name)
    {
        if(name!=null && !name.isEmpty())
            this.game = Game.of(name);
        return this;
    }
    
    /**
     * Sets the streaming {@link net.dv8tion.jda.core.entities.Game Game} to display when the bot is ready.
     * <br>This game will be displayed as a stream with the url provided as it's link.
     * 
     * <p><b>NOTE:</b> The url must be a valid streaming url from <a href="https://twitch.tv/">Twitch</a>.
     * <br>If the url is not valid, then it will be set as a normal game with the provided name: 'Playing 
     * <b>{@literal <name>}</b>'
     * 
     * @param  name
     *         Non-null/non-empty name of the stream
     * @param  url
     *         The url of the stream (must be valid for streaming)
     *         
     * @return This builder
     *
     * @deprecated
     *         This features will be removed in 2.0 due to it's availability
     *         through {@link net.dv8tion.jda.core.JDABuilder JDABuilder}
     *         not being more or less efficient than through the CommandClient.
     *         <br>If you want to set your bot's game or status, you may do so through
     *         {@link net.dv8tion.jda.core.JDABuilder#setGame(Game) JDABuilder#setGame(Game)}
     *         or {@link net.dv8tion.jda.core.JDABuilder#setStatus(OnlineStatus)
     *         JDABuilder#setStatus(OnlineStatus)} respectively.
     */
    @Deprecated
    public CommandClientBuilder setStreaming(String name, String url)
    {
        if(name!=null && !name.isEmpty())
        {
            if(Game.isValidStreamingUrl(url))
                this.setGame(Game.of(name, url));
            else
                this.setGame(Game.of(name));
        }
        return this;
    }
    
    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Game Game} the bot will use as the default: 
     * 'Playing <b>Type [prefix]help</b>'
     * 
     * @return This builder
     *
     * @deprecated
     *         This features will be removed in 2.0 due to it's availability
     *         through {@link net.dv8tion.jda.core.JDABuilder JDABuilder}
     *         not being more or less efficient than through the CommandClient.
     *         <br>If you want to set your bot's game or status, you may do so through
     *         {@link net.dv8tion.jda.core.JDABuilder#setGame(Game) JDABuilder#setGame(Game)}
     *         or {@link net.dv8tion.jda.core.JDABuilder#setStatus(OnlineStatus)
     *         JDABuilder#setStatus(OnlineStatus)} respectively.
     */
    @Deprecated
    public CommandClientBuilder useDefaultGame()
    {
        this.game = Game.of("default");
        return this;
    }
    
    /**
     * Sets the {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} the bot will use once Ready
     * This defaults to ONLINE
     *
     * @param  status
     *         The status to set
     *
     * @return This builder
     *
     * @deprecated
     *         This features will be removed in 2.0 due to it's availability
     *         through {@link net.dv8tion.jda.core.JDABuilder JDABuilder}
     *         not being more or less efficient than through the CommandClient.
     *         <br>If you want to set your bot's game or status, you may do so through
     *         {@link net.dv8tion.jda.core.JDABuilder#setGame(Game) JDABuilder#setGame(Game)}
     *         or {@link net.dv8tion.jda.core.JDABuilder#setStatus(OnlineStatus)
     *         JDABuilder#setStatus(OnlineStatus)} respectively.
     */
    @Deprecated
    public CommandClientBuilder setStatus(OnlineStatus status)
    {
        this.status = status;
        return this;
    }
    
    /**
     * Adds a {@link com.jagrosh.jdautilities.commandclient.Command Command} and registers it to the 
     * {@link com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl CommandClientImpl} for this session.
     * 
     * @param  command
     *         The command to add
     *         
     * @return This builder
     */
    public CommandClientBuilder addCommand(Command command)
    {
        commands.add(command);
        return this;
    }
    
    /**
     * Adds and registers multiple {@link com.jagrosh.jdautilities.commandclient.Command Command}s to the 
     * {@link com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl CommandClientImpl} for this session.
     * <br>This is the same as calling {@link CommandClientBuilder#addCommand(Command)} multiple times.
     * 
     * @param  commands
     *         The Commands to add
     *         
     * @return This builder
     */
    public CommandClientBuilder addCommands(Command... commands)
    {
        for(Command command: commands)
            this.addCommand(command);
        return this;
    }

    /**
     * Adds an annotated command module to the
     * {@link com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl CommandClientImpl} for this session.
     *
     * <p>For more information on annotated command modules, see
     * {@link com.jagrosh.jdautilities.commandclient.annotation the annotation package} documentation.
     *
     * @param  module
     *         The annotated command module to add
     *
     * @return This builder
     *
     * @see    com.jagrosh.jdautilities.commandclient.AnnotatedModuleCompiler
     * @see    com.jagrosh.jdautilities.commandclient.annotation.JDACommand
     */
    public CommandClientBuilder addAnnotatedModule(Object module)
    {
        this.commands.addAll(compiler.compile(module));

        return this;
    }

    /**
     * Adds multiple annotated command modules to the
     * {@link com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl CommandClientImpl} for this session.
     * <br>This is the same as calling {@link CommandClientBuilder#addAnnotatedModule(Object)} multiple times.
     *
     * <p>For more information on annotated command modules, see
     * {@link com.jagrosh.jdautilities.commandclient.annotation the annotation package} documentation.
     *
     * @param  modules
     *         The annotated command modules to add
     *
     * @return This builder
     *
     * @see    com.jagrosh.jdautilities.commandclient.AnnotatedModuleCompiler
     * @see    com.jagrosh.jdautilities.commandclient.annotation.JDACommand
     */
    public CommandClientBuilder addAnnotatedModules(Object... modules)
    {
        for(Object command : modules)
            addAnnotatedModule(command);
        return this;
    }

    /**
     * Sets the {@link com.jagrosh.jdautilities.commandclient.AnnotatedModuleCompiler AnnotatedModuleCompiler}
     * for this CommandClientBuilder.
     *
     * <p>If not set this will be the default implementation found {@link
     * com.jagrosh.jdautilities.commandclient.impl.AnnotatedModuleCompilerImpl here}.
     *
     * @param  compiler
     *         The AnnotatedModuleCompiler to use
     *
     * @return This builder
     *
     * @see    com.jagrosh.jdautilities.commandclient.AnnotatedModuleCompiler
     * @see    com.jagrosh.jdautilities.commandclient.annotation.JDACommand
     */
    public CommandClientBuilder setAnnotatedCompiler(AnnotatedModuleCompiler compiler)
    {
        this.compiler = compiler;
        return this;
    }

    /**
     * Sets the <a href="https://www.carbonitex.net/discord/bots">Carbonitex</a> key for this bot's listing.
     * 
     * <p>When set, the {@link com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl CommandClientImpl}
     * will automatically update it's Carbonitex listing with relevant information such as server count.
     * 
     * @param  key
     *         A Carbonitex key
     *         
     * @return This builder
     */
    public CommandClientBuilder setCarbonitexKey(String key)
    {
        this.carbonKey = key;
        return this;
    }
    
    /**
     * Sets the <a href="http://bots.discord.pw/">Discord Bots</a> API key for this bot's listing.
     * 
     * <p>When set, the {@link com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl CommandClientImpl} 
     * will automatically update it's Discord Bots listing with relevant information such as server count.
     * 
     * @param  key
     *         A Discord Bots API key
     *         
     * @return This builder
     */
    public CommandClientBuilder setDiscordBotsKey(String key)
    {
        this.botsKey = key;
        return this;
    }
    
    /**
     * Sets the <a href="https://discordbots.org/">Discord Bot List</a> API key for this bot's listing.
     * 
     * <p>When set, the {@link com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl CommandClientImpl} 
     * will automatically update it's Discord Bot List listing with relevant information such as server count.
     * 
     * @param  key
     *         A Discord Bot List API key
     *         
     * @return This builder
     */
    public CommandClientBuilder setDiscordBotListKey(String key)
    {
        this.botsOrgKey = key;
        return this;
    }
    
    /**
     * Sets the {@link com.jagrosh.jdautilities.commandclient.CommandListener CommandListener} for the 
     * {@link com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl CommandClientImpl}.
     * 
     * @param  listener
     *         The CommandListener for the CommandClientImpl
     *         
     * @return This builder
     */
    public CommandClientBuilder setListener(CommandListener listener)
    {
        this.listener = listener;
        return this;
    }
    
    /**
     * Sets the {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} for this 
     * {@link com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl CommandClientImpl}.
     * 
     * <p><b>NOTE:</b> It <b>MUST</b> be a 
     * {@link java.util.concurrent.Executors#newSingleThreadScheduledExecutor SingleThreadScheduledExecutor}. 
     * Providing any other kinds of {@link java.util.concurrent.Executors Executors} will cause unpredictable results.
     * 
     * <p>Also note that unless you wish to use the SingleThreadScheduledExecutor provided here in other areas of
     * your code, this is most likely useless to set.
     * 
     * @param  executor
     *         The ScheduledExecutorService for the CommandClientImpl (must be a SingleThreadScheduledExecutor)
     *         
     * @return This builder
     *
     * @deprecated
     *         Scheduled for removal in 2.0
     */
    @Deprecated
    public CommandClientBuilder setScheduleExecutor(ScheduledExecutorService executor)
    {
        this.executor = executor;
        return this;
    }

    /**
     * Sets the internal size of the client's {@link com.jagrosh.jdautilities.entities.FixedSizeCache FixedSizeCache}
     * used for caching and pairing the bot's response {@link net.dv8tion.jda.core.entities.Message Message}s with
     * the calling Message's ID.
     *
     * <p>Higher cache size means that decay of cache contents will most likely occur later, allowing the deletion of
     * responses when the call is deleted to last for a longer duration. However this also means larger memory usage.
     *
     * <p>Setting {@code 0} or negative will cause the client to not use linked caching <b>at all</b>.
     *
     * @param  linkedCacheSize
     *         The maximum number of paired responses that can be cached, or {@code <1} if the
     *         built {@link com.jagrosh.jdautilities.commandclient.CommandClient CommandClient}
     *         will not use linked caching.
     *
     * @return This builder
     */
    public CommandClientBuilder setLinkedCacheSize(int linkedCacheSize)
    {
        this.linkedCacheSize = linkedCacheSize;
        return this;
    }
}
