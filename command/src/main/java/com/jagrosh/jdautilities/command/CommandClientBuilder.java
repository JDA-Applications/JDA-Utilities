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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Consumer;

import com.jagrosh.jdautilities.command.impl.AnnotatedModuleCompilerImpl;
import com.jagrosh.jdautilities.command.impl.CommandClientImpl;
import java.util.concurrent.ScheduledExecutorService;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;

/**
 * A simple builder used to create a {@link com.jagrosh.jdautilities.command.impl.CommandClientImpl CommandClientImpl}.
 * 
 * <p>Once built, add the {@link com.jagrosh.jdautilities.command.CommandClient CommandClient} as an EventListener to
 * {@link net.dv8tion.jda.core.JDA JDA} and it will automatically handle commands with ease!
 * 
 * @author John Grosh (jagrosh)
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
    private Consumer<CommandEvent> helpConsumer;
    private String helpWord;
    private ScheduledExecutorService executor;
    private int linkedCacheSize = 0;
    private AnnotatedModuleCompiler compiler = new AnnotatedModuleCompilerImpl();
    private GuildSettingsManager manager = null;

    /**
     * Builds a {@link com.jagrosh.jdautilities.command.impl.CommandClientImpl CommandClientImpl}
     * with the provided settings.
     * <br>Once built, only the {@link com.jagrosh.jdautilities.command.CommandListener CommandListener},
     * and {@link com.jagrosh.jdautilities.command.Command Command}s can be changed.
     * 
     * @return The CommandClient built.
     */
    public CommandClient build()
    {
        CommandClient client = new CommandClientImpl(ownerId, coOwnerIds, prefix, altprefix, game, status, serverInvite,
                                                     success, warning, error, carbonKey, botsKey, botsOrgKey, new ArrayList<>(commands), useHelp,
                                                     helpConsumer, helpWord, executor, linkedCacheSize, compiler, manager);
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
     * Sets whether the {@link com.jagrosh.jdautilities.command.CommandClient CommandClient} will use
     * the builder to automatically create a help command or not.
     * 
     * @param  useHelp
     *         {@code false} to disable the help command builder, otherwise the CommandClient
     *         will use either the default or one provided via {@link com.jagrosh.jdautilities.command.CommandClientBuilder#setHelpConsumer(Consumer)}}.
     *         
     * @return This builder
     */
    public CommandClientBuilder useHelpBuilder(boolean useHelp)
    {
    	this.useHelp = useHelp;
        return this;
    }
    
    /**
     * Sets the consumer to run as the bot's help command.
     * <br>Setting it to {@code null} or not setting this at all will cause the bot to use 
     * the default help builder.
     * 
     * @param  helpConsumer
     *         A consumer to accept a {@link com.jagrosh.jdautilities.command.CommandEvent CommandEvent}
     *         when a help command is called.
     *         
     * @return This builder
     */
    public CommandClientBuilder setHelpConsumer(Consumer<CommandEvent> helpConsumer)
    {
        this.helpConsumer = helpConsumer;
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
     */
    public CommandClientBuilder setGame(Game game)
    {
        this.game = game;
        return this;
    }
    
    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Game Game} the bot will use as the default: 
     * 'Playing <b>Type [prefix]help</b>'
     * 
     * @return This builder
     */
    public CommandClientBuilder useDefaultGame()
    {
        this.game = Game.playing("default");
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
     */
    public CommandClientBuilder setStatus(OnlineStatus status)
    {
        this.status = status;
        return this;
    }
    
    /**
     * Adds a {@link com.jagrosh.jdautilities.command.Command Command} and registers it to the
     * {@link com.jagrosh.jdautilities.command.impl.CommandClientImpl CommandClientImpl} for this session.
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
     * Adds and registers multiple {@link com.jagrosh.jdautilities.command.Command Command}s to the
     * {@link com.jagrosh.jdautilities.command.impl.CommandClientImpl CommandClientImpl} for this session.
     * <br>This is the same as calling {@link com.jagrosh.jdautilities.command.CommandClientBuilder#addCommand(Command)} multiple times.
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
     * {@link com.jagrosh.jdautilities.command.impl.CommandClientImpl CommandClientImpl} for this session.
     *
     * <p>For more information on annotated command modules, see
     * {@link com.jagrosh.jdautilities.command.annotation the annotation package} documentation.
     *
     * @param  module
     *         The annotated command module to add
     *
     * @return This builder
     *
     * @see    AnnotatedModuleCompiler
     * @see    com.jagrosh.jdautilities.command.annotation.JDACommand
     */
    public CommandClientBuilder addAnnotatedModule(Object module)
    {
        this.commands.addAll(compiler.compile(module));
        return this;
    }

    /**
     * Adds multiple annotated command modules to the
     * {@link com.jagrosh.jdautilities.command.impl.CommandClientImpl CommandClientImpl} for this session.
     * <br>This is the same as calling {@link com.jagrosh.jdautilities.command.CommandClientBuilder#addAnnotatedModule(Object)} multiple times.
     *
     * <p>For more information on annotated command modules, see
     * {@link com.jagrosh.jdautilities.command.annotation the annotation package} documentation.
     *
     * @param  modules
     *         The annotated command modules to add
     *
     * @return This builder
     *
     * @see    AnnotatedModuleCompiler
     * @see    com.jagrosh.jdautilities.command.annotation.JDACommand
     */
    public CommandClientBuilder addAnnotatedModules(Object... modules)
    {
        for(Object command : modules)
            addAnnotatedModule(command);
        return this;
    }

    /**
     * Sets the {@link com.jagrosh.jdautilities.command.AnnotatedModuleCompiler AnnotatedModuleCompiler}
     * for this CommandClientBuilder.
     *
     * <p>If not set this will be the default implementation found {@link
     * com.jagrosh.jdautilities.command.impl.AnnotatedModuleCompilerImpl here}.
     *
     * @param  compiler
     *         The AnnotatedModuleCompiler to use
     *
     * @return This builder
     *
     * @see    AnnotatedModuleCompiler
     * @see    com.jagrosh.jdautilities.command.annotation.JDACommand
     */
    public CommandClientBuilder setAnnotatedCompiler(AnnotatedModuleCompiler compiler)
    {
        this.compiler = compiler;
        return this;
    }

    /**
     * Sets the <a href="https://www.carbonitex.net/discord/bots">Carbonitex</a> key for this bot's listing.
     * 
     * <p>When set, the {@link com.jagrosh.jdautilities.command.impl.CommandClientImpl CommandClientImpl}
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
     * <p>When set, the {@link com.jagrosh.jdautilities.command.impl.CommandClientImpl CommandClientImpl}
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
     * <p>When set, the {@link com.jagrosh.jdautilities.command.impl.CommandClientImpl CommandClientImpl}
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
     * Sets the {@link com.jagrosh.jdautilities.command.CommandListener CommandListener} for the
     * {@link com.jagrosh.jdautilities.command.impl.CommandClientImpl CommandClientImpl}.
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
     * Sets the {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} for the
     * {@link com.jagrosh.jdautilities.command.impl.CommandClientImpl CommandClientImpl}.
     * 
     * @param  executor
     *         The ScheduledExecutorService for the CommandClientImpl
     *         
     * @return This builder
     */
    public CommandClientBuilder setScheduleExecutor(ScheduledExecutorService executor)
    {
        this.executor = executor;
        return this;
    }
    
    /**
     * Sets the internal size of the client's {@link com.jagrosh.jdautilities.commons.utils.FixedSizeCache FixedSizeCache}
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
     *         built {@link com.jagrosh.jdautilities.command.CommandClient CommandClient}
     *         will not use linked caching.
     *
     * @return This builder
     */
    public CommandClientBuilder setLinkedCacheSize(int linkedCacheSize)
    {
        this.linkedCacheSize = linkedCacheSize;
        return this;
    }

    /**
     * Sets the {@link com.jagrosh.jdautilities.command.GuildSettingsManager GuildSettingsManager}
     * for the CommandClientImpl built using this builder.
     *
     * @param  manager
     *         The GuildSettingsManager to set.
     *
     * @return This builder
     */
    public CommandClientBuilder setGuildSettingsManager(GuildSettingsManager manager)
    {
        this.manager = manager;
        return this;
    }
}
