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
package com.jagrosh.jdautilities.menu;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;

/**
 *
 * @author John Grosh
 */
public abstract class Menu {
    protected final EventWaiter waiter;
    protected final Set<User> users;
    protected final Set<Role> roles;
    protected final long timeout;
    protected final TimeUnit unit;
    
    protected Menu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit)
    {
        this.waiter = waiter;
        this.users = users;
        this.roles = roles;
        this.timeout = timeout;
        this.unit = unit;
    }
    
    /**
     * Displays this Menu in a {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * 
     * @param  channel
     *         The MessageChannel to display this Menu in
     */
    public abstract void display(MessageChannel channel);
    
    /**
     * Displays this Menu as a designated {@link net.dv8tion.jda.core.entities.Message Message}.
     * <br>The Message provided must be one sent by the bot! Trying to provided a Message
     * authored by another {@link net.dv8tion.jda.core.entities.User User} will prevent the
     * Menu from being displayed!
     * 
     * @param  message
     *         The Message to display this Menu as
     */
    public abstract void display(Message message);
    
    protected boolean isValidUser(MessageReactionAddEvent event)
    {
        if(users.isEmpty() && roles.isEmpty())
            return true;
        if(users.contains(event.getUser()))
            return true;
        if(!(event.getChannel() instanceof TextChannel))
            return false;
        Member m = ((TextChannel)event.getChannel()).getGuild().getMember(event.getUser());
        return m.getRoles().stream().anyMatch(r -> roles.contains(r));
    }
    
    protected boolean isValidUser(MessageReceivedEvent event)
    {
        if(users.isEmpty() && roles.isEmpty())
            return true;
        if(users.contains(event.getAuthor()))
            return true;
        if(!(event.getChannel() instanceof TextChannel))
            return false;
        return event.getMember().getRoles().stream().anyMatch(r -> roles.contains(r));
    }
}
