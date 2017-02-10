package me.jagrosh.jdautilities.commandclient.annotated;

import me.jagrosh.jdautilities.commandclient.Command;
import me.jagrosh.jdautilities.commandclient.CommandEvent;
import me.jagrosh.jdautilities.commandclient.CommandListener;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public class AnnotatedCommandListener implements CommandListener
{

    private static final Object DEFAULT_TARGET = new Object();

    private Object target = DEFAULT_TARGET;

    public void setTarget(Object object)
    {
        if (object != null)
            this.target = object;
        else this.target = DEFAULT_TARGET;
    }

    @Override
    public void onCommand(CommandEvent event, Command command)
    {
        try
        {
            for (Method m : getMethodsForType(OnCommandEvent.class))
                m.invoke(target, event, command);
        }
        catch (InvocationTargetException ex)
        {
            Throwable cause = ex.getCause();
            if (cause != null)
                throw new RuntimeException(cause);
            else throw new RuntimeException(ex);
        }
        catch (IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onCompletedCommand(CommandEvent event, Command command)
    {
        try
        {
            for (Method m : getMethodsForType(OnCommandCompleted.class))
                m.invoke(target, event, command);
        }
        catch (InvocationTargetException ex)
        {
            Throwable cause = ex.getCause();
            if (cause != null)
                throw new RuntimeException(cause);
            else throw new RuntimeException(ex);
        }
        catch (IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onTerminatedCommand(CommandEvent event, Command command)
    {
        try
        {
            for (Method m : getMethodsForType(OnCommandTerminated.class))
                m.invoke(target, event, command);
        }
        catch (InvocationTargetException ex)
        {
            Throwable cause = ex.getCause();
            if (cause != null)
                throw new RuntimeException(cause);
            else throw new RuntimeException(ex);
        }
        catch (IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onNonCommandMessage(MessageReceivedEvent event)
    {
        try
        {
            for (Method m : getMethodsForType(OnUnprocessedMessage.class))
                m.invoke(target, event);
        }
        catch (InvocationTargetException ex)
        {
            Throwable cause = ex.getCause();
            if (cause != null)
                throw new RuntimeException(cause);
            else throw new RuntimeException(ex);
        }
        catch (IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private Set<Method> getMethodsForType(final Class<? extends Annotation> type)
    {
        Set<Method> methods = new LinkedHashSet<>();
        Stream.of(target.getClass().getMethods())
              .filter(target -> target.getAnnotationsByType(type).length > 0)
              .map(method -> {method.setAccessible(true); return method;})
              .forEach(methods::add);
        return methods;
    }
}
