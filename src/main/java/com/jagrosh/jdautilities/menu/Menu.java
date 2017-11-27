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
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;

import javax.annotation.Nullable;

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

    /**
     * This method was not officially documented before 1.9 when it was marked
     * as deprecated.<p>
     *
     * Please see {@link Menu#isValidUser(User, Guild)} for the officially
     * supported overload.
     *
     * @param  event
     *         The event
     *
     * @return {@code true} if the User is valid.
     *
     * @deprecated
     *         Replace with {@link Menu#isValidUser(User, Guild)}
     */
    @Deprecated
    protected boolean isValidUser(MessageReactionAddEvent event)
    {
        return isValidUser(event.getUser(), event.getGuild());
    }

    /**
     * This method was not officially documented before 1.9 when it was marked
     * as deprecated.<p>
     *
     * Please see {@link Menu#isValidUser(User, Guild)} for the officially
     * supported overload.
     *
     * @param  event
     *         The event
     *
     * @return {@code true} if the User is valid.
     *
     * @deprecated
     *         Replace with {@link Menu#isValidUser(User, Guild)}
     */
    @Deprecated
    protected boolean isValidUser(MessageReceivedEvent event)
    {
        return isValidUser(event.getAuthor(), event.getGuild());
    }

    /**
     * Checks to see if the provided {@link net.dv8tion.jda.core.entities.User User}
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
     * Checks to see if the provided {@link net.dv8tion.jda.core.entities.User User}
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

        return guild.getMember(user).getRoles().stream().anyMatch(r -> roles.contains(r));
    }
}
