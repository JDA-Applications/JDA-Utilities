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
import java.util.function.Predicate;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.PermissionUtil;


/**
 * The internal inheritance for Commands used in JDA-Utilities.<p>
 * Classes created inheriting this class gain the unique traits of commands operated using the
 * Commands Extension. Using several fields, a command can define properties that make it 
 * unique and complex while maintaining a low level of development. All Commands extending this
 * class can define any number of these fields and then a main command body:<p>
 * <code>protected void execute(CommandEvent event) {
 * event.reply("Hey look! This would be the bot's reply if this was a command!");
 * }</code><p>
 * @author John Grosh (jagrosh)
 */
public abstract class Command {
    protected String name = "null";
    protected String help = "no help available";
    protected Category category = null;
    protected String arguments = null;
    protected boolean guildOnly = true;
    protected String requiredRole = null;
    protected boolean ownerCommand = false;
    protected int cooldown = 0;
    protected Permission[] userPermissions = new Permission[0];
    protected Permission[] botPermissions = new Permission[0];
    protected String[] aliases = new String[0];
    protected Command[] children = new Command[0];
    
    private final static String BOT_PERM = "%s I need the %s permission in this %s!";
    private final static String USER_PERM = "%s You must have the %s permission in this %s to use that!";
    
    /**
     * The main body method of a {@linkplain Command}. This is the "response" for a successful
     * {@linkplain #run}.
     * @param event the CommandEvent that triggered this Command
     */
    protected abstract void execute(CommandEvent event);
    
    /**
     * Runs checks for the {@linkplain Command} with the given {@linkplain CommandEvent} that called it.
     * Will terminate, and possibly respond with a failure message, if any checks fail.
     * @param event the CommandEvent that triggered this Command
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
     * Checks if the given input represents this command
     * @param input the input to check
     * @return true if the input is the name or an alias of the command
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
     * Gets the name of the command
     * @return the name of the command
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Gets the help string for the command
     * @return the help string for the command
     */
    public String getHelp()
    {
        return help;
    }
    
    /**
     * Gets the {@linkplain Category} for the command
     * @return the category for the command
     */
    public Category getCategory()
    {
        return category;
    }
    
    /**
     * Gets the argument format for the command
     * @return the argument format for the command
     */
    public String getArguments()
    {
        return arguments;
    }
    
    /**
     * Gets the required user permissions to run the command on a guild
     * @return the required user permissions to run the command on a guild
     */
    public Permission[] getUserPermissions()
    {
        return userPermissions;
    }
    
    /**
     * Gets the required bot permissions to run the command on a guild
     * @return the required bot permissions to run the command on a guild
     */
    public Permission[] getBotPermissions()
    {
        return botPermissions;
    }
    
    /**
     * Gets the command's child commands
     * @return the child commands of the command
     */
    public Command[] getChildren()
    {
        return children;
    }
    
    /**
     * Gets the aliases for the command
     * @return the aliases for the command
     */
    public String[] getAliases()
    {
        return aliases;
    }
    
    /**
     * Checks whether or not this command is an owner only command
     * @return true if the command is an owner command, otherwise false if it is not
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
     * To be used in {@linkplain Command}s as a means of organizing commands into "Categories" as well 
     * as terminate command usage when the calling CommandEvent doesn't meet certain requirements
     * 
     * @author John Grosh (jagrosh)
     */
    public static class Category
    {
        private final String name;
        private final String failResponse;
        private final Predicate<CommandEvent> predicate;
        
        /**
         * A Command Category containing a name
         * @param name The name of the Category
         */
        public Category(String name)
        {
            this.name = name;
            this.failResponse = null;
            this.predicate = null;
        }
        
        /**
         * A Command Category containing a name and a predicate.<p>
         * The command will be terminated if the {@linkplain #test(CommandEvent)} returns false.
         * @param name The name of the Category
         * @param predicate the Category test
         */
        public Category(String name, Predicate<CommandEvent> predicate)
        {
            this.name = name;
            this.failResponse = null;
            this.predicate = predicate;
        }
        
        /**
         * A Command Category containing a name, a predicate, and a failure response.<p>
         * The command will be terminated if the {@linkplain #test(CommandEvent)} returns false, 
         * and the failure response will be sent.
         * @param name the name of the Category
         * @param failResponse the response if the test fails
         * @param predicate the Category test
         */
        public Category(String name, String failResponse, Predicate<CommandEvent> predicate)
        {
            this.name = name;
            this.failResponse = failResponse;
            this.predicate = predicate;
        }
        
        /**
         * Gets the name of the Category
         * @return the name of the Category
         */
        public String getName()
        {
            return name;
        }
        
        /**
         * Gets the failure response of the Category
         * @return the failure response of the Category
         */
        public String getFailureResponse()
        {
            return failResponse;
        }
        
        /**
         * Runs a test of the provided predicate 
         * @param event the CommandEvent that was called when this method is tested
         * @return true if the predicate was not set, was set as null, or was tested and returned true, otherwise
         * returns false
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
