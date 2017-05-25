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
public abstract class Command {
    
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
     * {@code true} if the command may only be used in a {@link net.dv8tion.jda.core.Guild Guild}, 
     * {@code false} if it may be used in both a Guild and a DM.
     */
    protected boolean guildOnly = true;
    
    /**
     * A String name of a role required to use this command.
     */
    protected String requiredRole = null;
    
    /**
     * {@code true} if the command may only be used by a User with an ID matching the
     * Owners or any of the CoOwners.
     */
    protected boolean ownerCommand = false;
    
    /**
     * An {@code int} number of seconds users must wait before using this command again.
     */
    protected int cooldown = 0;
    
    /**
     * Any {@link net.dv8tion.jda.core.Permission Permission}s a Member must have to use this command.
     * <br>These are only checked in a {@link net.dv8tion.jda.core.Guild Guild} environment.
     */
    protected Permission[] userPermissions = new Permission[0];
    
    /**
     * Any {@link net.dv8tion.jda.core.Permission Permission}s the bot must have to use a command.
     * <br>These are only checked in a {@link net.dv8tion.jda.core.Guild Guild} environment.
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
        if(ownerCommand && !(event.isOwner() || event.isCoOwner()))
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
        
        // required role check
        if(requiredRole!=null)
            if(event.getChannelType()!=ChannelType.TEXT || !event.getMember().getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(requiredRole)))
            {
                terminate(event, event.getClient().getError()+" You must have a role called `"+requiredRole+"` to use that!");
                return;
            }
        
        // sub-help check
        if(helpBiConsumer!=null && !event.getArgs().isEmpty() && event.getArgs().split("\\s+")[0].equalsIgnoreCase(event.getClient().getHelpWord()))
            helpBiConsumer.accept(event, this);
        
        // availabilty check
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
            event.reply(event.getClient().getError()+" This command cannot be used in Direct messages");
            return;
        }
        
        //cooldown check
        if(cooldown>0)
        {
            int remaining = event.getClient().getRemainingCooldown(name+"|"+event.getAuthor().getId());
            if(remaining>0)
            {
                event.reply(event.getClient().getWarning()+" That command is on cooldown for "+remaining+" more seconds!");
                return;
            }
            else event.getClient().applyCooldown(name+"|"+event.getAuthor().getId(), cooldown);
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
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#name Command.name} for the Command.
     *
     * @return The name for the Command
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#help Command.help} for the Command.
     *
     * @return The help for the Command
     */
    public String getHelp() {
        return help;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#category Command.category} for the Command.
     *
     * @return The category for the Command
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#arguments Command.arguments} for the Command.
     *
     * @return The arguments for the Command
     */
    public String getArguments() {
        return arguments;
    }

    /**
     * Checks if this Command can only be used in a {@link net.dv8tion.jda.core.entities.Guild Guild}.
     *
     * @return {@code true} if this Command can only be used in a Guild, else {@code false} if it can
     *         be used outside of one
     */
    public boolean isGuildOnly() {
        return guildOnly;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#requiredRole Command.requiredRole} for the Command.
     *
     * @return The requiredRole for the Command
     */
    public String getRequiredRole() {
        return requiredRole;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#cooldown Command.cooldown} for the Command.
     *
     * @return The cooldown for the Command
     */
    public int getCooldown() {
        return cooldown;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#userPermissions Command.userPermissions} for the Command.
     *
     * @return The userPermissions for the Command
     */
    public Permission[] getUserPermissions() {
        return userPermissions;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#botPermissions Command.botPermissions} for the Command.
     *
     * @return The botPermissions for the Command
     */
    public Permission[] getBotPermissions() {
        return botPermissions;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#aliases Command.aliases} for the Command.
     *
     * @return The aliases for the Command
     */
    public String[] getAliases() {
        return aliases;
    }

    /**
     * Gets the {@link com.jagrosh.jdautilities.commandclient.Command#children Command.children} for the Command.
     *
     * @return The children for the Command
     */
    public Command[] getChildren() {
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
    
    private void terminate(CommandEvent event, String message)
    {
        if(message!=null)
            event.reply(message);
        if(event.getClient().getListener()!=null)
            event.getClient().getListener().onTerminatedCommand(event, this);
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
            return predicate==null ? true : predicate.test(event);
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
}
