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

import java.util.Objects;
import java.util.function.Predicate;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.PermissionUtil;


/**
 *
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
    
    private final static String BOT_PERM = "%s I need the %s permission in this %s!";
    private final static String USER_PERM = "%s You must have the %s permission in this %s to use that!";
    
    protected abstract void execute(CommandEvent event);
    
    public final void run(CommandEvent event)
    {
        // owner check
        if(ownerCommand && !event.getAuthor().getId().equals(event.getClient().getOwnerId()))
        {
            terminate(event,null);
            return;
        }
        
        // category check
        if(category!=null && !category.test(event))
        {
            terminate(event,null);
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
    
    public String getName()
    {
        return name;
    }
    
    public String getHelp()
    {
        return help;
    }
    
    public Category getCategory()
    {
        return category;
    }
    
    public String getArguments()
    {
        return arguments;
    }
    
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
    
    public static class Category
    {
        private final String name;
        private final Predicate<CommandEvent> predicate;
        
        public Category(String name)
        {
            this.name = name;
            this.predicate = null;
        }
        
        public Category(String name, Predicate<CommandEvent> predicate)
        {
            this.name = name;
            this.predicate = predicate;
        }
        
        public String getName()
        {
            return name;
        }
        
        public boolean test(CommandEvent event)
        {
            return predicate==null ? true : predicate.test(event);
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof Category))
                return false;
            Category other = (Category)obj;
            return Objects.equals(name, other.name) && Objects.equals(predicate, other.predicate);
        }
        
    }
}
