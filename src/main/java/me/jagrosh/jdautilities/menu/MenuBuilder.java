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
package me.jagrosh.jdautilities.menu;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import me.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

/**
 *
 * @author John Grosh
 */
public abstract class MenuBuilder {
    protected EventWaiter waiter;
    protected Set<User> users = new HashSet<>();
    protected Set<Role> roles = new HashSet<>();
    protected long timeout = 1;
    protected TimeUnit unit = TimeUnit.MINUTES;
    
    public abstract <V extends Menu> V build();
    
    public abstract <T extends MenuBuilder> T setColor(Color color);
        
    /**
     * Sets the EventWaiter to use to do menu operations
     * @param waiter the EventWaiter
     * @return the builder after the waiter has been set
     */
    public final <T extends MenuBuilder> T setEventWaiter(EventWaiter waiter)
    {
        this.waiter = waiter;
        return (T)this;
    }
    
    /**
     * Adds users that are allowed to use the menu
     * @param users the users allowed to use the menu
     * @return the builder when the users have been added
     */
    public final <T extends MenuBuilder> T addUsers(User... users)
    {
        this.users.addAll(Arrays.asList(users));
        return (T)this;
    }
    
    /**
     * Sets users that are allowed to use the menu
     * @param users the users allowed to use the menu
     * @return the builder when the users have been set
     */
    public final <T extends MenuBuilder> T setUsers(User... users)
    {
        this.users.clear();
        this.users.addAll(Arrays.asList(users));
        return (T)this;
    }
    
    /**
     * Adds roles that are allowed to use the menu
     * @param roles the roles allowed to use the menu
     * @return  the builder when the roles have been added
     */
    public final <T extends MenuBuilder> T addRoles(Role... roles)
    {
        this.roles.addAll(Arrays.asList(roles));
        return (T)this;
    }
    
    /**
     * Sets roles that are allowed to use the menu
     * @param roles the roles allowed to use the menu
     * @return  the builder when the roles have been set
     */
    public final <T extends MenuBuilder> T setRoles(Role... roles)
    {
        this.roles.clear();
        this.roles.addAll(Arrays.asList(roles));
        return (T)this;
    }
    
    /**
     * Sets the timeout that the menu should stay available
     * 
     * @param timeout the amount of time
     * @param unit the time units
     * @return the menu builder when the timeout has been set
     */
    public final <T extends MenuBuilder> T setTimeout(long timeout, TimeUnit unit)
    {
        this.timeout = timeout;
        this.unit = unit;
        return (T)this;
    }
}
