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

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.PermissionUtil;

/**
 * <h1><b>Commands In JDA-Utilities</b></h1>
 * 
 * <p>The internal inheritance for Commands used in JDA-Utilities is that of the Command object.
 * 
 * <p>Classes created inheriting this class gain the unique traits of commands operated using the Commands Extension.
 * <br>Using several fields, a command can define properties that make it unique and complex while maintaining 
 * a low level of development. 
 * <br>All Commands extending this class can define any number of these fields in a object constructor and then 
 * create the command action/response in the abstract 
 * {@link com.jagrosh.jdautilities.commandclient.Command#execute(CommandEvent) #execute(CommandEvent)} body:
 * 
 * <pre><code> public class ExampleCmd extends Command {
 *      
 *      public ExampleCmd() {
 *          this.name = "example";
 *          this.aliases = new String[]{"test","demo"};
 *          this.help = "gives an example of commands do";
 *      }
 *      
 *      {@literal @Override}
 *      protected void execute(CommandEvent) {
 *          event.reply("Hey look! This would be the bot's reply if this was a command!");
 *      }
 *      
 * }</code></pre>
 * 
 * Execution is with the provision of a MessageReceivedEvent-CommandClient wrapper called a
 * {@link com.jagrosh.jdautilities.commandclient.CommandEvent CommandEvent} and is performed in two steps:
 * <ul>
 *     <li>{@link com.jagrosh.jdautilities.commandclient.Command#run(CommandEvent) run} - The command runs 
 *     through a series of conditionals, automatically terminating the command instance if one is not met, 
 *     and possibly providing an error response.</li>
 *     
 *     <li>{@link com.jagrosh.jdautilities.commandclient.Command#execute(CommandEvent) execute} - The command, 
 *     now being cleared to run, executes and performs whatever lies in the abstract body method.</li>
 * </ul>
 * 
 * @author John Grosh (jagrosh)
 */
public abstract class Command
{
    /**
     * The name of the command, allows the command to be called the format: {@code [prefix]<command name>}.
     */
    protected String name = "null";
    
    /**
     * A small help String that summarizes the function of the command, used in the default help builder.
     */
    protected String help = "no help available";
    
    /**
     * The {@link com.jagrosh.jdautilities.commandclient.Command.Category Category} of the command. 
     * <br>This can perform any other checks not completed by the default conditional fields.
     */
    protected Category category = null;
    
    /**
     * An arguments format String for the command, used in the default help builder.
     */
    protected String arguments = null;
    
    /**
     * {@code true} if the command may only be used in a {@link net.dv8tion.jda.core.entities.Guild Guild}, 
     * {@code false} if it may be used in both a Guild and a DM.
     * <br>Default {@code true}.
     */
    protected boolean guildOnly = true;
    
    /**
     * A String name of a role required to use this command.
     */
    protected String requiredRole = null;
    
    /**
     * {@code true} if the command may only be used by a User with an ID matching the
     * Owners or any of the CoOwners.
     * <br>Default {@code false}.
     */
    protected boolean ownerCommand = false;
    
    /**
     * An {@code int} number of seconds users must wait before using this command again.
     */
    protected int cooldown = 0;
    
    /**
     * Any {@link net.dv8tion.jda.core.Permission Permission}s a Member must have to use this command.
     * <br>These are only checked in a {@link net.dv8tion.jda.core.entities.Guild Guild} environment.
     */
    protected Permission[] userPermissions = new Permission[0];
    
    /**
     * Any {@link net.dv8tion.jda.core.Permission Permission}s the bot must have to use a command.
     * <br>These are only checked in a {@link net.dv8tion.jda.core.entities.Guild Guild} environment.
     */
    protected Permission[] botPermissions = new Permission[0];
    
    /**
     * The aliases of the command, when calling a command these function identically to calling the
     * {@link com.jagrosh.jdautilities.commandclient.Command#name Command.name}.
     */
    protected String[] aliases = new String[0];
    
    /**
     * The child commands of the command. These are used in the format {@code [prefix]<parent name>
     * <child name>}.
     */
    protected Command[] children = new Command[0];
    
    /**
     * The {@link java.util.function.BiConsumer BiConsumer} for creating a help response to the format 
     * {@code [prefix]<command name> help}.
     */
    protected BiConsumer<CommandEvent, Command> helpBiConsumer = null;

    /**
     * {@code true} if this command checks a channel topic for topic-tags.
     * <br>This means that putting {@code {-commandname}}, {@code {-command category}}, {@code {-all}} in a channel topic
     * will cause this command to terminate.
     * <br>Default {@code true}.
     */
    protected boolean usesTopicTags = true;
    
    /**
     * {@code true} if this command should be hidden from the help.
     * <br>Default {@code false}
     */
    protected boolean hidden = false;

    /**
     * The {@link com.jagrosh.jdautilities.commandclient.Command.CooldownScope CooldownScope}
     * of the command. This defines how far of a scope cooldowns have.
     * <br>Default {@link com.jagrosh.jdautilities.commandclient.Command.CooldownScope#USER CooldownScope.USER}.
     */
    protected CooldownScope cooldownScope = CooldownScope.USER;
    
    private final static String BOT_PERM = "%s I need the %s permission in this %s!";
    private final static String USER_PERM = "%s You must have the %s permission in this %s to use that!";
    
    /**
     * The main body method of a {@link com.jagrosh.jdautilities.commandclient.Command Command}. 
     * <br>This is the "response" for a successful 
     * {@link com.jagrosh.jdautilities.commandclient.Command#run(CommandEvent) #run(CommandEvent)}.
     * 
     * @param  event
     *         The {@link com.jagrosh.jdautilities.commandclient.CommandEvent CommandEvent} that 
     *         triggered this Command
     */
    protected abstract void execute(CommandEvent event);
    
    /**
     * Runs checks for the {@link com.jagrosh.jdautilities.commandclient.Command Command} with the 
     * given {@link com.jagrosh.jdautilities.commandclient.CommandEvent CommandEvent} that called it.
     * <br>Will terminate, and possibly respond with a failure message, if any checks fail.
     * 
     * @param  event
     *         The CommandEvent that triggered this Command
     */
    public final void run(CommandEvent event)
    {
        // child check
        if(!event.getArgs().isEmpty())
        {
            String[] parts = Arrays.copyOf(event.getArgs().split("\\s+",2), 2);
            if(helpBiConsumer!=null && parts[0].equalsIgnoreCase(event.getClient().getHelpWord()))
            {
                helpBiConsumer.accept(event, this);
                return;
            }
            for(Command cmd: children)
            {
                if(cmd.isCommandFor(parts[0]))
                {
                    event.setArgs(parts[1]==null ? "" : parts[1]);
                    cmd.run(event);
                    return;
                }
            }
        }
        
        // owner check
        if(ownerCommand && !(event.isOwner()))
        {
            terminate(event,null);
            return;
        }
        
        // category check
        if(category!=null && !category.test(event))
        {
            terminate(event, category.getFailureResponse());
            return;
        }

        // is allowed check
        if(event.isFromType(ChannelType.TEXT) && !isAllowed(event.getTextChannel()))
        {
            terminate(event, "That command cannot be used in this channel!");
            return;
        }
        
        // required role check
        if(requiredRole!=null)
            if(!event.isFromType(ChannelType.TEXT) || !event.getMember().getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(requiredRole)))
            {
                terminate(event, event.getClient().getError()+" You must have a role called `"+requiredRole+"` to use that!");
                return;
            }
        
        // availability check
        if(event.getChannelType()==ChannelType.TEXT)
        {
            // bot perms
            for(Permission p: botPermissions)
            {
                if(p.isChannel())
                {
                    if(p.name().startsWith("VOICE"))
                    {
                        VoiceChannel vc = event.getMember().getVoiceState().getChannel();
                        if(vc==null)
                        {
                            terminate(event, event.getClient().getError()+" You must be in a voice channel to use that!");
                            return;
                        }
                        else if(!PermissionUtil.checkPermission(vc, event.getSelfMember(), p))
                        {
                            terminate(event, String.format(BOT_PERM, event.getClient().getError(), p.name(), "Voice Channel"));
                            return;
                        }
                    }
                    else
                    {
                        if(!PermissionUtil.checkPermission(event.getTextChannel(), event.getSelfMember(), p))
                        {
                            terminate(event, String.format(BOT_PERM, event.getClient().getError(), p.name(), "Channel"));
                            return;
                        }
                    }
                }
                else
                {
                    if(!PermissionUtil.checkPermission(event.getTextChannel(), event.getSelfMember(), p))
                    {
                        terminate(event, String.format(BOT_PERM, event.getClient().getError(), p.name(), "Guild"));
                        return;
                    }
                }
            }
            
            //user perms
            for(Permission p: userPermissions)
            {
                if(p.isChannel())
                {
                    if(!PermissionUtil.checkPermission(event.getTextChannel(), event.getMember(), p))
                    {
                        terminate(event, String.format(USER_PERM, event.getClient().getError(), p.name(), "Channel"));
                        return;
                    }
                }
                else
                {
                    if(!PermissionUtil.checkPermission(event.getTextChannel(), event.getMember(), p))
                    {
                        terminate(event, String.format(USER_PERM, event.getClient().getError(), p.name(), "Guild"));
                        return;
                    }
                }
            }
        }
        else if(guildOnly)
        {
            terminate(event, event.getClient().getError()+" This command cannot be used in Direct messages");
            return;
        }
        
        //cooldown check
        if(cooldown>0)
        {
            String key = getCooldownKey(event);
            int remaining = event.getClient().getRemainingCooldown(key);
            if(remaining>0)
            {
                String error = getCooldownError(event, remaining);
                if(error!=null)
                {
                    terminate(event, error);
                    return;
                }
            }
            else event.getClient().applyCooldown(key, cooldown);
        }
        
        // run
        execute(event);
        if(event.getClient().getListener()!=null)
            event.getClient().getListener().onCompletedCommand(event, this);
    }
    
    /**
     * Checks if the given input represents this Command
     * 
     * @param  input
     *         The input to check
     * 
     * @return {@code true} if the input is the name or an alias of the Command
     */
    public boolean isCommandFor(String input)
    {
        if(name.equalsIgnoreCase(input))
            return true;
        for(String alias: aliases)
            if(alias.equalsIgnoreCase(input))
                return true;
        return false;
    }

    /**
     * Checks whether a command is allowed in a {@link net.dv8tion.jda.core.entities.TextChannel TextChannel}
     * by searching the channel topic for topic tags relating to the command.
     *
     * <p>{-{@link com.jagrosh.jdautilities.commandclient.Command#name name}},
     * {-{@link com.jagrosh.jdautilities.commandclient.Command.Category category name}}, or {-{@code all}}
     * are valid examples of ways that this method would return {@code false} if placed in a channel topic.
     *
     * <p><b>NOTE:</b>Topic tags are <b>case sensitive</b> and proper usage must be in lower case!
     * <br>Also note that setting {@link com.jagrosh.jdautilities.commandclient.Command#usesTopicTags usesTopicTags}
     * to {@code false} will cause this method to always return {@code true}, as the feature would not be applicable
     * in the first place.
     *
     * @param  channel
     *         The TextChannel to test.
     *
     * @return {@code true} if the channel topic doesn't specify any topic-tags that would cause this command
     *         to be cancelled, or if {@code usesTopicTags} has been set to {@code false}.
     */
    public boolean isAllowed(TextChannel channel)
    {
        if(!usesTopicTags)
            return true;
        if(channel==null)
            return true;
        String topic = channel.getTopic();
        if(topic==null || topic.isEmpty())
            return true;
        topic = topic.toLowerCase();
        String lowerName = name.toLowerCase();
        if(topic.contains("{"+lowerName+"}"))
            return true;
        if(topic.contains("{-"+lowerName+"}"))
            return false;
        String lowerCat = category==null ? null : category.getName().toLowerCase();
        if(lowerCat!=null)
        {
            if(topic.contains("{"+lowerCat+"}"))
                return true;
            if(topic.contains("{-"+lowerCat+"}"))
                return false;
        }
        return !topic.contains("{-all}");
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#name Command.name} for the Command.
     *
     * @return The name for the Command
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#help Command.help} for the Command.
     *
     * @return The help for the Command
     */
    public String getHelp()
    {
        return help;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#category Command.category} for the Command.
     *
     * @return The category for the Command
     */
    public Category getCategory()
    {
        return category;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#arguments Command.arguments} for the Command.
     *
     * @return The arguments for the Command
     */
    public String getArguments()
    {
        return arguments;
    }

    /**
     * Checks if this Command can only be used in a {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @return {@code true} if this Command can only be used in a Guild, else {@code false} if it can
     *         be used outside of one
     */
    public boolean isGuildOnly()
    {
        return guildOnly;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#requiredRole Command.requiredRole} for the Command.
     *
     * @return The requiredRole for the Command
     */
    public String getRequiredRole()
    {
        return requiredRole;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#cooldown Command.cooldown} for the Command.
     *
     * @return The cooldown for the Command
     */
    public int getCooldown()
    {
        return cooldown;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#userPermissions Command.userPermissions} for the Command.
     *
     * @return The userPermissions for the Command
     */
    public Permission[] getUserPermissions()
    {
        return userPermissions;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#botPermissions Command.botPermissions} for the Command.
     *
     * @return The botPermissions for the Command
     */
    public Permission[] getBotPermissions()
    {
        return botPermissions;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#aliases Command.aliases} for the Command.
     *
     * @return The aliases for the Command
     */
    public String[] getAliases()
    {
        return aliases;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#children Command.children} for the Command.
     *
     * @return The children for the Command
     */
    public Command[] getChildren()
    {
        return children;
    }

    /**
     * Checks whether or not this command is an owner only Command.
     * 
     * @return {@code true} if the command is an owner command, otherwise {@code false} if it is not
     */
    public boolean isOwnerCommand()
    {
        return ownerCommand;
    }
    
    /**
     * Checks whether or not this command should be hidden from the help
     * 
     * @return {@code true} if the command should be hidden, otherwise {@code false}
     */
    public boolean isHidden()
    {
        return hidden;
    }
    
    private void terminate(CommandEvent event, String message)
    {
        if(message!=null)
            event.reply(message);
        if(event.getClient().getListener()!=null)
            event.getClient().getListener().onTerminatedCommand(event, this);
    }

    /**
     * Gets the proper cooldown key for this Command under the provided
     * {@link com.jagrosh.jdautilities.commandclient.CommandEvent CommanEvent}.
     *
     * @param  event
     *         The CommandEvent to generate the cooldown for.
     *
     * @return A String key to use when applying a cooldown.
     */
    public String getCooldownKey(CommandEvent event)
    {
        switch (cooldownScope)
        {
            case USER:         return cooldownScope.genKey(name,event.getAuthor().getIdLong());
            case USER_GUILD:   return event.getGuild()!=null ? cooldownScope.genKey(name,event.getAuthor().getIdLong(),event.getGuild().getIdLong()) :
                    CooldownScope.USER_CHANNEL.genKey(name,event.getAuthor().getIdLong(), event.getChannel().getIdLong());
            case USER_CHANNEL: return cooldownScope.genKey(name,event.getAuthor().getIdLong(),event.getChannel().getIdLong());
            case GUILD:        return event.getGuild()!=null ? cooldownScope.genKey(name,event.getGuild().getIdLong()) :
                    CooldownScope.CHANNEL.genKey(name,event.getChannel().getIdLong());
            case CHANNEL:      return cooldownScope.genKey(name,event.getChannel().getIdLong());
            case SHARD:        return event.getJDA().getShardInfo()!=null ? cooldownScope.genKey(name, event.getJDA().getShardInfo().getShardId()) :
                    CooldownScope.GLOBAL.genKey(name, 0);
            case USER_SHARD:   return event.getJDA().getShardInfo()!=null ? cooldownScope.genKey(name,event.getAuthor().getIdLong(),event.getJDA().getShardInfo().getShardId()) :
                    CooldownScope.USER.genKey(name, event.getAuthor().getIdLong());
            case GLOBAL:       return cooldownScope.genKey(name, 0);
            default:           return "";
        }
    }

    /**
     * Gets an error message for this Command under the provided
     * {@link com.jagrosh.jdautilities.commandclient.CommandEvent CommanEvent}.
     *
     * @param  event
     *         The CommandEvent to generate the error message for.
     * @param  remaining
     *         The remaining number of seconds a command is on cooldown for.
     *
     * @return A String error message for this command if {@code remaining > 0},
     *         else {@code null}.
     */
    public String getCooldownError(CommandEvent event, int remaining)
    {
        if(remaining<=0)
            return null;
        String front = event.getClient().getWarning()+" That command is on cooldown for "+remaining+" more seconds";
        if(cooldownScope.equals(CooldownScope.USER))
            return front+"!";
        else if(cooldownScope.equals(CooldownScope.USER_GUILD) && event.getGuild()==null)
            return front+" "+CooldownScope.USER_CHANNEL.errorSpecification+"!";
        else if(cooldownScope.equals(CooldownScope.GUILD) && event.getGuild()==null)
            return front+" "+CooldownScope.CHANNEL.errorSpecification+"!";
        else
            return front+" "+cooldownScope.errorSpecification+"!";
    }

    /**
     * To be used in {@link com.jagrosh.jdautilities.commandclient.Command Command}s as a means of 
     * organizing commands into "Categories" as well as terminate command usage when the calling 
     * {@link com.jagrosh.jdautilities.commandclient.CommandEvent CommandEvent} doesn't meet 
     * certain requirements.
     * 
     * @author John Grosh (jagrosh)
     */
    public static class Category
    {
        private final String name;
        private final String failResponse;
        private final Predicate<CommandEvent> predicate;
        
        /**
         * A Command Category containing a name.
         * 
         * @param  name
         *         The name of the Category
         */
        public Category(String name)
        {
            this.name = name;
            this.failResponse = null;
            this.predicate = null;
        }
        
        /**
         * A Command Category containing a name and a {@link java.util.function.Predicate}.
         * 
         * <p>The command will be terminated if the {@link #test(CommandEvent)} 
         * returns {@code false}.
         * 
         * @param  name 
         *         The name of the Category
         * @param  predicate
         *         The Category predicate to test
         */
        public Category(String name, Predicate<CommandEvent> predicate)
        {
            this.name = name;
            this.failResponse = null;
            this.predicate = predicate;
        }
        
        /**
         * A Command Category containing a name, a {@link java.util.function.Predicate},
         * and a failure response.
         * 
         * <p>The command will be terminated if the {@link #test(CommandEvent)} 
         * returns {@code false}, and the failure response will be sent.
         * 
         * @param  name 
         *         The name of the Category
         * @param  failResponse
         *         The response if the test fails
         * @param  predicate
         *         The Category predicate to test
         */
        public Category(String name, String failResponse, Predicate<CommandEvent> predicate)
        {
            this.name = name;
            this.failResponse = failResponse;
            this.predicate = predicate;
        }
        
        /**
         * Gets the name of the Category.
         * 
         * @return The name of the Category
         */
        public String getName()
        {
            return name;
        }
        
        /**
         * Gets the failure response of the Category.
         * 
         * @return The failure response of the Category
         */
        public String getFailureResponse()
        {
            return failResponse;
        }
        
        /**
         * Runs a test of the provided {@link java.util.function.Predicate}.
         * 
         * @param  event
         *         The {@link com.jagrosh.jdautilities.commandclient.CommandEvent CommandEvent}
         *         that was called when this method is invoked
         *         
         * @return {@code true} if the Predicate was not set, was set as null, or was 
         *         tested and returned true, otherwise returns {@code false}
         */
        public boolean test(CommandEvent event)
        {
            return predicate==null || predicate.test(event);
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof Category))
                return false;
            Category other = (Category)obj;
            return Objects.equals(name, other.name) && Objects.equals(predicate, other.predicate) && Objects.equals(failResponse, other.failResponse);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + Objects.hashCode(this.name);
            hash = 17 * hash + Objects.hashCode(this.failResponse);
            hash = 17 * hash + Objects.hashCode(this.predicate);
            return hash;
        }
    }

    /**
     * A series of {@link java.lang.Enum Enum}s used for defining the scope size for a
     * {@link com.jagrosh.jdautilities.commandclient.Command Command}'s cooldown.
     *
     * <p>The purpose for these values is to allow easy, refined, and generally convenient keys
     * for cooldown scopes, allowing a command to remain on cooldown for more than just the user
     * calling it, with no unnecessary abstraction or developer input.
     *
     * Cooldown keys are generated via {@link Command#getCooldownKey(CommandEvent)
     * Command#getCooldownKey(CommandEvent)} using 1-2 Snowflake ID's corresponding to the name
     * (IE: {@code USER_CHANNEL} uses the ID's of the User and the Channel from the CommandEvent).
     *
     * <p>However, the issue with generalizing and generating like this is that the command may
     * be called in a non-guild environment, causing errors internally.
     * <br>To prevent this, all of the values that contain "{@code GUILD}" in their name default
     * to their "{@code CHANNEL}" counterparts when commands using them are called outside of a
     * {@link net.dv8tion.jda.core.entities.Guild Guild} environment.
     * <ul>
     *     <li>{@link #GUILD GUILD} defaults to {@link #CHANNEL CHANNEL}.</li>
     *     <li>{@link #USER_GUILD USER_GUILD} defaults to {@link #USER_CHANNEL USER_CHANNEL}.</li>
     * </ul>
     *
     * @since  1.3
     * @author Kaidan Gustave
     *
     * @see    com.jagrosh.jdautilities.commandclient.Command#cooldownScope Command.cooldownScope
     *
     * @apiNote
     *         These are effective across a single instance of JDA, and not multiple
     *         ones, save when multiple shards run on a single JVM and under a
     *         {@link net.dv8tion.jda.bot.sharding.ShardManager ShardManager}.
     *         <br>There is no shard magic, and no guarantees for a 100% "global"
     *         cooldown, unless all shards of the bot run under the same ShardManager,
     *         and/or via some external system unrelated to JDA-Utilities.
     */
    public enum CooldownScope
    {
        /**
         * Applies the cooldown to the calling {@link net.dv8tion.jda.core.entities.User User} across all
         * locations on this instance (IE: TextChannels, PrivateChannels, etc).
         *
         * <p>The key for this is generated in the format
         * <ul>
         *     {@code <command-name>|U:<userID>}
         * </ul>
         */
        USER("U:%d",""),

        /**
         * Applies the cooldown to the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} the
         * command is called in.
         *
         * <p>The key for this is generated in the format
         * <ul>
         *     {@code <command-name>|C:<channelID>}
         * </ul>
         */
        CHANNEL("C:%d","in this channel"),

        /**
         * Applies the cooldown to the calling {@link net.dv8tion.jda.core.entities.User User} local to the
         * {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} the command is called in.
         *
         * <p>The key for this is generated in the format
         * <ul>
         *     {@code <command-name>|U:<userID>|C:<channelID>}
         * </ul>
         */
        USER_CHANNEL("U:%d|C:%d", "in this channel"),

        /**
         * Applies the cooldown to the {@link net.dv8tion.jda.core.entities.Guild Guild} the command is called in.
         *
         * <p>The key for this is generated in the format
         * <ul>
         *     {@code <command-name>|G:<guildID>}
         * </ul>
         *
         * <p><b>NOTE:</b> This will automatically default back to {@link CooldownScope#CHANNEL CooldownScope.CHANNEL}
         * when called in a private channel.  This is done in order to prevent internal
         * {@link java.lang.NullPointerException NullPointerException}s from being thrown while generating cooldown keys!
         */
        GUILD("G:%d", "in this server"),

        /**
         * Applies the cooldown to the calling {@link net.dv8tion.jda.core.entities.User User} local to the
         * {@link net.dv8tion.jda.core.entities.Guild Guild} the command is called in.
         *
         * <p>The key for this is generated in the format
         * <ul>
         *     {@code <command-name>|U:<userID>|G:<guildID>}
         * </ul>
         *
         * <p><b>NOTE:</b> This will automatically default back to {@link CooldownScope#CHANNEL CooldownScope.CHANNEL}
         * when called in a private channel. This is done in order to prevent internal
         * {@link java.lang.NullPointerException NullPointerException}s from being thrown while generating cooldown keys!
         */
        USER_GUILD("U:%d|G:%d", "in this server"),

        /**
         * Applies the cooldown to the calling Shard the command is called on.
         *
         * <p>The key for this is generated in the format
         * <ul>
         *     {@code <command-name>|S:<shardID>}
         * </ul>
         *
         * <p><b>NOTE:</b> This will automatically default back to {@link CooldownScope#GLOBAL CooldownScope.GLOBAL}
         * when {@link net.dv8tion.jda.core.JDA#getShardInfo() JDA#getShardInfo()} returns {@code null}.
         * This is done in order to prevent internal {@link java.lang.NullPointerException NullPointerException}s
         * from being thrown while generating cooldown keys!
         */
        SHARD("S:%d", "on this shard"),

        /**
         * Applies the cooldown to the calling {@link net.dv8tion.jda.core.entities.User User} on the Shard
         * the command is called on.
         *
         * <p>The key for this is generated in the format
         * <ul>
         *     {@code <command-name>|U:<userID>|S:<shardID>}
         * </ul>
         *
         * <p><b>NOTE:</b> This will automatically default back to {@link CooldownScope#USER CooldownScope.USER}
         * when {@link net.dv8tion.jda.core.JDA#getShardInfo() JDA#getShardInfo()} returns {@code null}.
         * This is done in order to prevent internal {@link java.lang.NullPointerException NullPointerException}s
         * from being thrown while generating cooldown keys!
         */
        USER_SHARD("U:%d|S:%d", "on this shard"),

        /**
         * Applies this cooldown globally.
         *
         * <p>As this implies: the command will be unusable on the instance of JDA in all types of
         * {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}s until the cooldown has ended.
         *
         * <p>The key for this is {@code <command-name>|globally}
         */
        GLOBAL("Global", "globally");

        private final String format;
        final String errorSpecification;

        CooldownScope(String format, String errorSpecification)
        {
            this.format = format;
            this.errorSpecification = errorSpecification;
        }

        String genKey(String name, long id)
        {
            return genKey(name, id, -1);
        }

        String genKey(String name, long idOne, long idTwo)
        {
            if(this.equals(GLOBAL))
                return name+"|"+format;
            else if(idTwo==-1)
                return name+"|"+String.format(format,idOne);
            else return name+"|"+String.format(format,idOne,idTwo);
        }
    }
}
