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
package com.jagrosh.jdautilities.waiter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

/**
 * A simple object used primarily for entities found in {@link com.jagrosh.jdautilities.menu}.
 * 
 * <p>The EventWaiter is capable of handling specialized forms of
 * {@link net.dv8tion.jda.core.events.Event Event} that must meet criteria not normally specifiable
 * without implementation of an {@link net.dv8tion.jda.core.hooks.EventListener EventListener}.
 *
 * <p>Creating an EventWaiter requires provision and/or creation of a
 * {@link java.util.concurrent.ScheduledExecutorService Executor}, and thus a proper
 * shutdown of said executor. The default constructor for an EventWaiter sets up a
 * working, "live", EventWaiter whose shutdown is triggered via JDA firing a
 * {@link net.dv8tion.jda.core.events.ShutdownEvent ShutdownEvent}.
 * <br>A more "shutdown adaptable" constructor allows the provision of a
 * {@code ScheduledExecutorService} and a choice of how exactly shutdown will be handled
 * (see {@link EventWaiter#EventWaiter(ScheduledExecutorService, boolean)} for more details).
 * 
 * <p>As a final note, if you intend to use the EventWaiter, it is highly recommended you <b>DO NOT</b>
 * create multiple EventWaiters! Doing this will cause unnecessary increases in memory usage.
 * 
 * @author John Grosh (jagrosh)
 */
public class EventWaiter implements EventListener
{
    private final HashMap<Class<?>, List<WaitingEvent>> waitingEvents;
    private final ScheduledExecutorService threadpool;
    private final boolean shutdownAutomatically;
    
    /**
     * Constructs an empty EventWaiter.
     */
    public EventWaiter()
    {
        this(Executors.newSingleThreadScheduledExecutor(), true);
    }

    /**
     * Constructs an EventWaiter using the provided {@link java.util.concurrent.ScheduledExecutorService Executor}
     * as it's threadpool.
     *
     * <p>A developer might choose to use this constructor over the {@link EventWaiter#EventWaiter() default},
     * for using a alternate form of threadpool, as opposed to a
     * {@link java.util.concurrent.Executors#newSingleThreadExecutor() single thread executor}.
     * <br>A developer might also favor this over the default as they use the same waiter for multiple
     * shards, and thus shutdown must be handled externally if a special shutdown sequence is being used.
     *
     * <p>{@code shutdownAutomatically} is required to be manually specified by developers as a way of
     * verifying a contract that the developer will conform to the behavior of the newly generated EventWaiter:
     * <ul>
     *     <li>If {@code true}, shutdown is handled when a {@link net.dv8tion.jda.core.events.ShutdownEvent ShutdownEvent}
     *     is fired. This means that any external functions of the provided Executor is now impossible and any externally
     *     queued tasks are lost if they have yet to be run.</li>
     *     <li>If {@code false}, shutdown is now placed as a responsibility of the developer, and no attempt will be
     *     made to shutdown the provided Executor.</li>
     * </ul>
     * It's worth noting that this EventWaiter can serve as a delegate to invoke the threadpool's shutdown via
     * a call to {@link EventWaiter#shutdown()}. However, this operation is only supported for EventWaiters that
     * are not supposed to shutdown automatically, otherwise invocation of {@code EventWaiter#shutdown()} will
     * result in an {@link java.lang.UnsupportedOperationException UnsupportedOperationException}.
     *
     * @param  threadpool
     *         The ScheduledExecutorService to use for this EventWaiter's threadpool.
     * @param  shutdownAutomatically
     *         Whether or not the {@code threadpool} will shutdown automatically when a
     *         {@link net.dv8tion.jda.core.events.ShutdownEvent ShutdownEvent} is fired.
     *
     * @see    EventWaiter#shutdown()
     */
    public EventWaiter(ScheduledExecutorService threadpool, boolean shutdownAutomatically)
    {
        this.waitingEvents = new HashMap<>();
        this.threadpool = threadpool;

        // "Why is there no default constructor?"
        //
        // When a developer uses this constructor we want them to be aware that this
        // is putting the task on them to shut down the threadpool if they set this to false,
        // or to avoid errors being thrown when ShutdownEvent is fired if they set it true.
        //
        // It is YOUR fault if you have a rogue threadpool that doesn't shut down if you
        // forget to dispose of it and set this false, or that certain tasks may fail
        // if you use this executor for other things and set this true.
        //
        // NOT MINE
        this.shutdownAutomatically = shutdownAutomatically;
    }
    
    /**
     * Waits an indefinite amount of time for an {@link net.dv8tion.jda.core.events.Event Event} that
     * returns {@code true} when tested with the provided {@link java.util.function.Predicate Predicate}.
     * 
     * <p>When this occurs, the provided {@link java.util.function.Consumer Consumer} will accept and
     * execute using the same Event.
     * 
     * @param  <T>
     *         The type of Event to wait for
     * @param  classType
     *         The {@link java.lang.Class} of the Event to wait for
     * @param  condition
     *         The Predicate to test when Events of the provided type are thrown
     * @param  action
     *         The Consumer to perform an action when the condition Predicate returns {@code true}
     */
    public <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action)
    {
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
     *         The type of Event to wait for
     * @param  classType
     *         The {@link java.lang.Class} of the Event to wait for
     * @param  condition
     *         The Predicate to test when Events of the provided type are thrown
     * @param  action
     *         The Consumer to perform an action when the condition Predicate returns {@code true}
     * @param  timeout
     *         The maximum amount of time to wait for
     * @param  unit
     *         The {@link java.util.concurrent.TimeUnit TimeUnit} measurement of the timeout
     * @param  timeoutAction
     *         The Runnable to run if the time runs out before a correct Event is thrown
     *
     * @throws IllegalArgumentException
     *         The internal threadpool is shut down, meaning that no more tasks can be submitted.
     */
    public <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action, long timeout, TimeUnit unit, Runnable timeoutAction)
    {
        if(threadpool.isShutdown())
            throw new IllegalArgumentException("Attempted to register a WaitingEvent while the EventWaiter's threadpool was already shut down!");
        List<WaitingEvent> list;
        if(waitingEvents.containsKey(classType))
            list = waitingEvents.get(classType);
        else
        {
            list = new ArrayList<>();
            waitingEvents.put(classType, list);
        }
        WaitingEvent we = new WaitingEvent<>(condition, action);
        list.add(we);

        if(timeout>0 && unit!=null)
        {
            threadpool.schedule(() -> {
                if(list.remove(we) && timeoutAction!=null)
                    timeoutAction.run();
            }, timeout, unit);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public final void onEvent(Event event)
    {
        Class c = event.getClass();
        while(c.getSuperclass()!=null) {
            if(waitingEvents.containsKey(c))
            {
                List<WaitingEvent> list = waitingEvents.get(c);
                List<WaitingEvent> ulist = new ArrayList<>(list);
                list.removeAll(ulist.stream().filter(Objects::nonNull).filter(i -> i.attempt(event)).collect(Collectors.toList()));
            }
            if(event instanceof ShutdownEvent && shutdownAutomatically)
            {
                threadpool.shutdown();
            }
            c = c.getSuperclass();
        }
    }

    /**
     * Closes this EventWaiter if it doesn't normally shutdown automatically.
     *
     * <p><b>IF YOU USED THE DEFAULT CONSTRUCTOR WITH NO ARGUMENTS DO NOT CALL THIS!</b>
     * <br>Calling this method on an EventWaiter that does shutdown automatically will result in
     * an {@link java.lang.UnsupportedOperationException UnsupportedOperationException} being thrown.
     *
     *
     *
     * @throws UnsupportedOperationException
     *         The EventWaiter is supposed to close automatically.
     */
    public void shutdown()
    {
        if(shutdownAutomatically)
            throw new UnsupportedOperationException("Shutting down EventWaiters that are set to automatically close is unsupported!");

        threadpool.shutdown();
    }
    
    private class WaitingEvent<T extends Event>
    {
        final Predicate<T> condition;
        final Consumer<T> action;
        
        WaitingEvent(Predicate<T> condition, Consumer<T> action)
        {
            this.condition = condition;
            this.action = action;
        }
        
        boolean attempt(T event)
        {
            if(condition.test(event))
            {
                action.accept(event);
                return true;
            }
            return false;
        }
    }
}
