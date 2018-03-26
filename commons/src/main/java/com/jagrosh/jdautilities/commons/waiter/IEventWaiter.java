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
package com.jagrosh.jdautilities.commons.waiter;

import net.dv8tion.jda.core.events.Event;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Implementable frame for waiting on {@link net.dv8tion.jda.core.events.Event Event}s.
 *
 * <p>This interface serves to allow custom implementations for interacting with
 * menus in the {@code menu} module of the JDA-Utilities library, allowing developers
 * direct access to the precise internal function that handles event responses.
 *
 * <p>The standard implementation of this interface is
 * {@link com.jagrosh.jdautilities.commons.waiter.EventWaiter EventWaiter}.
 *
 * @see    EventWaiter
 *
 * @since  2.2
 * @author Kaidan Gustave
 *
 * @implNote
 *         Event tracking and handling is not explicitly required
 *         when implementing this interface, however anyone looking
 *         to implement this should have a plan for handling events.
 *         <br>For instance, those using JDA's default
 *         {@link net.dv8tion.jda.core.hooks.IEventManager IEventManager}
 *         should have the implementation of this interface also
 *         implement {@link net.dv8tion.jda.core.hooks.EventListener EventListener},
 *         and add it to an instance of JDA as a listener.
 */
public interface IEventWaiter
{
    /**
     * Waits an indefinite amount of time for an {@link net.dv8tion.jda.core.events.Event Event} that
     * returns {@code true} when tested with the provided {@link java.util.function.Predicate Predicate}.
     *
     * <p>When this occurs, the provided {@link java.util.function.Consumer Consumer} will accept and
     * execute using the same Event.
     *
     * @param  <T>
     *         The type of Event to wait for.
     * @param  classType
     *         The {@link java.lang.Class} of the Event to wait for. Never null.
     * @param  condition
     *         The Predicate to test when Events of the provided type are thrown. Never null.
     * @param  action
     *         The Consumer to perform an action when the condition Predicate returns {@code true}. Never null.
     *
     * @throws java.lang.IllegalArgumentException
     *         One of two reasons:
     *         <ul>
     *             <li>1) Either the {@code classType}, {@code condition}, or {@code action} was {@code null}.</li>
     *             <li>2) The internal threadpool is shut down, meaning that no more tasks can be submitted.</li>
     *         </ul>
     */
    default <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action)
    {
        // Do not override this!
        // This method is only here for backwards compatibility with the menu module!
        waitForEvent(classType, condition, action, -1, null, null);
    }

    /**
     * Waits a predetermined amount of time for an {@link net.dv8tion.jda.core.events.Event Event} that
     * returns {@code true} when tested with the provided {@link java.util.function.Predicate Predicate}.
     *
     * <p>Once started, there are two possible outcomes:
     * <ul>
     *     <li>The correct Event occurs within the time allotted, and the provided
     *     {@link java.util.function.Consumer Consumer} will accept and execute using the same Event.</li>
     *
     *     <li>The time limit is elapsed and the provided {@link java.lang.Runnable} is executed.</li>
     * </ul>
     *
     * @param  <T>
     *         The type of Event to wait for.
     * @param  classType
     *         The {@link java.lang.Class} of the Event to wait for. Never null.
     * @param  condition
     *         The Predicate to test when Events of the provided type are thrown. Never null.
     * @param  action
     *         The Consumer to perform an action when the condition Predicate returns {@code true}. Never null.
     * @param  timeout
     *         The maximum amount of time to wait for, or {@code -1} if there is no timeout.
     * @param  unit
     *         The {@link java.util.concurrent.TimeUnit TimeUnit} measurement of the timeout, or
     *         {@code null} if there is no timeout.
     * @param  timeoutAction
     *         The Runnable to run if the time runs out before a correct Event is thrown, or
     *         {@code null} if there is no action on timeout.
     *
     * @throws java.lang.IllegalArgumentException
     *         One of two reasons:
     *         <ul>
     *             <li>1) Either the {@code classType}, {@code condition}, or {@code action} was {@code null}.</li>
     *             <li>2) The internal threadpool is shut down, meaning that no more tasks can be submitted.</li>
     *         </ul>
     */
    <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action,
                                        long timeout, TimeUnit unit, Runnable timeoutAction);
}
