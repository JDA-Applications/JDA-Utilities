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
package menu;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import me.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

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
}
