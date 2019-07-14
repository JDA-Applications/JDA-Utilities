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
package com.jagrosh.jdautilities.menu;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.entities.*;

import javax.annotation.Nullable;

/**
 * A frame for wrapping an {@link com.jagrosh.jdautilities.commons.waiter.EventWaiter EventWaiter}
 * into a "action, reaction" menu that waits on forms of user input such as reactions,
 * or key-phrases.
 *
 * <p>Classes extending this are able to take a provided {@link net.dv8tion.jda.api.entities.Message Message}
 * or {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel} and display a visualized "Menu"
 * as or in it.
 *
 * <p>The JDA-Utilities default implementations of this superclass typically handle input through
 * the assistance of things such as {@link net.dv8tion.jda.api.entities.MessageReaction reactions},
 * but the actual implementation is only limited to the events provided by Discord and handled through JDA.
 *
 * <p>For custom implementations, readability of creating and integrating may be improved
 * by the implementation of a companion builder may be helpful (see the documentation on
 * {@link Menu.Builder Menu.Builder} for more info).
 *
 * @see    com.jagrosh.jdautilities.commons.waiter.EventWaiter
 * @see    Menu.Builder
 *
 * @author John Grosh
 *
 * @implNote
 *         While the standard JDA-Utilities implementations of this and Menu are
 *         all handled as {@link net.dv8tion.jda.api.entities.MessageEmbed embeds},
 *         there is no bias or advantage of implementing a custom Menu as a message
 *         without an embed.
 */
public abstract class Menu
{
    protected final EventWaiter waiter;
    protected Set<User> users;
    protected Set<Role> roles;
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
     * Displays this Menu in a {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.
     * 
     * @param  channel
     *         The MessageChannel to display this Menu in
     */
    public abstract void display(MessageChannel channel);
    
    /**
     * Displays this Menu as a designated {@link net.dv8tion.jda.api.entities.Message Message}.
     * <br>The Message provided must be one sent by the bot! Trying to provided a Message
     * authored by another {@link net.dv8tion.jda.api.entities.User User} will prevent the
     * Menu from being displayed!
     * 
     * @param  message
     *         The Message to display this Menu as
     */
    public abstract void display(Message message);

    /**
     * Checks to see if the provided {@link net.dv8tion.jda.api.entities.User User}
     * is valid to interact with this Menu.<p>
     *
     * This is a shortcut for {@link Menu#isValidUser(User, Guild)} where the Guild
     * is {@code null}.
     *
     * @param  user
     *         The User to validate.
     *
     * @return {@code true} if the User is valid, {@code false} otherwise.
     *
     * @see    Menu#isValidUser(User, Guild)
     */
    protected boolean isValidUser(User user)
    {
        return isValidUser(user, null);
    }

    /**
     *
     * Checks to see if the provided {@link net.dv8tion.jda.api.entities.User User}
     * is valid to interact with this Menu.<p>
     *
     * For a User to be considered "valid" to use a Menu, the following logic (in order) is applied:
     * <ul>
     *     <li>The User must not be a bot. If it is, this returns {@code false} immediately.</li>
     *
     *     <li>If no users and no roles were specified in the builder for this Menu, then this
     *         will return {@code true}.</li>
     *
     *     <li>If the User is among the users specified in the builder for this Menu, this will
     *         return {@code true}.</li>
     *
     *     <li>If the Guild is {@code null}, or if the User is not a member on the Guild, this
     *         will return {@code false}.</li>
     *
     *     <li>Finally, the determination will be if the User on the provided Guild has any
     *         of the builder-specified Roles.</li>
     * </ul>
     *
     * Custom-implementation-wise, it's highly recommended developers who might override this
     * attempt to follow a similar logic for their Menus, as this provides a full-proof guard
     * against exceptions when validating a User of a Menu.
     *
     * @param  user
     *         The User to validate.
     * @param  guild
     *         The Guild to validate the User on.<br>
     *         Can be provided {@code} null safely.
     *
     * @return {@code true} if the User is valid, {@code false} otherwise.
     */
    protected boolean isValidUser(User user, @Nullable Guild guild)
    {
        if(user.isBot())
            return false;
        if(users.isEmpty() && roles.isEmpty())
            return true;
        if(users.contains(user))
            return true;
        if(guild == null || !guild.isMember(user))
            return false;

        return guild.getMember(user).getRoles().stream().anyMatch(roles::contains);
    }

    /**
     * An extendable frame for a chain-method builder that constructs a specified type of
     * {@link com.jagrosh.jdautilities.menu.Menu Menu}.<p>
     *
     * Conventionally, implementations of Menu should have a static nested class called
     * {@code Builder}, which extends this superclass:
     * <pre><code>
     * public class MyMenu extends Menu
     * {
     *     // Menu Code
     *
     *    {@literal public static class Builder extends Menu.Builder<Builder, MyMenu>}
     *     {
     *         // Builder Code
     *     }
     * }
     * </code></pre>
     *
     * @author John Grosh
     *
     * @implNote
     *         Before 2.0 this were a separate class known as {@code MenuBuilder}.<br>
     *         Note that while the standard JDA-Utilities implementations of this and Menu are
     *         all handled as {@link net.dv8tion.jda.api.entities.MessageEmbed embeds}, there
     *         is no bias or advantage of implementing a custom Menu as a message without an embed.
     */
    @SuppressWarnings("unchecked")
    public abstract static class Builder<T extends Builder<T, V>, V extends Menu>
    {
        protected EventWaiter waiter;
        protected Set<User> users = new HashSet<>();
        protected Set<Role> roles = new HashSet<>();
        protected long timeout = 1;
        protected TimeUnit unit = TimeUnit.MINUTES;

        /**
         * Builds the {@link com.jagrosh.jdautilities.menu.Menu Menu} corresponding to
         * this {@link com.jagrosh.jdautilities.menu.Menu.Builder Menu.Builder}.
         * <br>After doing this, no modifications of the displayed Menu can be made.
         *
         * @return The built Menu of corresponding type to this {@link com.jagrosh.jdautilities.menu.Menu.Builder}.
         */
        public abstract V build();

        /**
         * Sets the {@link com.jagrosh.jdautilities.commons.waiter.EventWaiter EventWaiter}
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
         * Adds {@link net.dv8tion.jda.api.entities.User User}s that are allowed to use the
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
         * Sets {@link net.dv8tion.jda.api.entities.User User}s that are allowed to use the
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
         * Adds {@link net.dv8tion.jda.api.entities.Role Role}s that are allowed to use the
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
         * Sets {@link net.dv8tion.jda.api.entities.Role Role}s that are allowed to use the
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
