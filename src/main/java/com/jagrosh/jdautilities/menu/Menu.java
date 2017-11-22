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

import java.util.Arrays;
import java.util.HashSet;
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
public abstract class Menu
{
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
        if(event.getUser().isBot())
            return false;
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
        if(event.getAuthor().isBot())
            return false;
        if(users.isEmpty() && roles.isEmpty())
            return true;
        if(users.contains(event.getAuthor()))
            return true;
        if(!(event.getChannel() instanceof TextChannel))
            return false;
        return event.getMember().getRoles().stream().anyMatch(r -> roles.contains(r));
    }

    /**
     *
     * @author John Grosh
     */
    public abstract static class Builder<T extends Builder<T, V>, V extends Menu> {
        protected EventWaiter waiter;
        protected Set<User> users = new HashSet<>();
        protected Set<Role> roles = new HashSet<>();
        protected long timeout = 1;
        protected TimeUnit unit = TimeUnit.MINUTES;

        /**
         * Builds the {@link com.jagrosh.jdautilities.menu.Menu Menu} corresponding to
         * this {@link com.jagrosh.jdautilities.menu.Menu.Builder MenuBuilder}.
         * <br>After doing this, no modifications of the displayed Menu can be made.
         *
         * @return The built Menu of corresponding type to this MenuBuilder.
         */
        public abstract V build();

        /**
         * Sets the {@link com.jagrosh.jdautilities.waiter.EventWaiter EventWaiter}
         * that will do {@link com.jagrosh.jdautilities.menu.Menu Menu} operations.
         *
         * <p><b>NOTE:</b> All Menus will only work with an EventWaiter set!
         * <br>Not setting an EventWaiter means the Menu will not work.
         *
         * @param  waiter
         *         The EventWaiter
         *
         * @return This builder
         */
        public final T setEventWaiter(EventWaiter waiter)
        {
            this.waiter = waiter;
            return (T) this;
        }

        /**
         * Adds {@link net.dv8tion.jda.core.entities.User User}s that are allowed to use the
         * {@link com.jagrosh.jdautilities.menu.Menu Menu} that will be built.
         *
         * @param  users
         *         The Users allowed to use the Menu
         *
         * @return This builder
         */
        public final T addUsers(User... users)
        {
            this.users.addAll(Arrays.asList(users));
            return (T)this;
        }

        /**
         * Sets {@link net.dv8tion.jda.core.entities.User User}s that are allowed to use the
         * {@link com.jagrosh.jdautilities.menu.Menu Menu} that will be built.
         * <br>This clears any Users already registered before adding the ones specified.
         *
         * @param  users
         *         The Users allowed to use the Menu
         *
         * @return This builder
         */
        public final T setUsers(User... users)
        {
            this.users.clear();
            this.users.addAll(Arrays.asList(users));
            return (T)this;
        }

        /**
         * Adds {@link net.dv8tion.jda.core.entities.Role Role}s that are allowed to use the
         * {@link com.jagrosh.jdautilities.menu.Menu Menu} that will be built.
         *
         * @param  roles
         *         The Roles allowed to use the Menu
         *
         * @return This builder
         */
        public final T addRoles(Role... roles)
        {
            this.roles.addAll(Arrays.asList(roles));
            return (T)this;
        }

        /**
         * Sets {@link net.dv8tion.jda.core.entities.Role Role}s that are allowed to use the
         * {@link com.jagrosh.jdautilities.menu.Menu Menu} that will be built.
         * <br>This clears any Roles already registered before adding the ones specified.
         *
         * @param  roles
         *         The Roles allowed to use the Menu
         *
         * @return This builder
         */
        public final T setRoles(Role... roles)
        {
            this.roles.clear();
            this.roles.addAll(Arrays.asList(roles));
            return (T)this;
        }

        /**
         * Sets the timeout that the {@link com.jagrosh.jdautilities.menu.Menu Menu} should
         * stay available.
         *
         * <p>After this has expired, the a final action in the form of a
         * {@link java.lang.Runnable Runnable} may execute.
         *
         * @param  timeout
         *         The amount of time for the Menu to stay available
         * @param  unit
         *         The {@link java.util.concurrent.TimeUnit TimeUnit} for the timeout
         *
         * @return This builder
         */
        public final T setTimeout(long timeout, TimeUnit unit)
        {
            this.timeout = timeout;
            this.unit = unit;
            return (T) this;
        }
    }
}
