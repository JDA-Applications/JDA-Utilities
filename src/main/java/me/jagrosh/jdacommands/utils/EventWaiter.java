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
package me.jagrosh.jdacommands.utils;

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

/**
 *
 * @author John Grosh (jagrosh)
 */
public class EventWaiter implements EventListener {
    
    private final HashMap<Class<?>, List<WaitingEvent>> waitingEvents;
    private final ScheduledExecutorService threadpool;
    
    public EventWaiter()
    {
        waitingEvents = new HashMap<>();
        threadpool = Executors.newSingleThreadScheduledExecutor();
    }
    
    /**
     * Waits an indefinite amount of time for an event
     * @param <T> the type of event
     * @param classType
     * @param condition the condition that the waiter will do that action
     * @param action the action to perform when the condition is met
     */
    public <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action)
    {
        waitForEvent(classType, condition, action, -1, null, null);
    }
    
    public <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action, long timeout, TimeUnit unit, Runnable timeoutAction)
    {
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
            threadpool.schedule(() -> {
                if(list.remove(we) && timeoutAction!=null)
                    timeoutAction.run();
            }, timeout, unit);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public final void onEvent(Event event)
    {
        if(waitingEvents.containsKey(event.getClass()))
        {
            List<WaitingEvent> list = waitingEvents.get(event.getClass());
            List<WaitingEvent> ulist = new ArrayList<>(list);
            list.removeAll(ulist.stream().filter(i -> i.attempt(event)).collect(Collectors.toList()));
        }
        else if(event instanceof ShutdownEvent)
        {
            threadpool.shutdown();
        }
    }
}
