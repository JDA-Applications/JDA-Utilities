/*
 * Copyright 2016 John Grosh (jagrosh).
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
package com.jagrosh.jdautilities.commandclient;

import com.jagrosh.jdautilities.commandclient.annotation.JDACommand;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.MalformedParametersException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A "compiler" for {@link java.lang.Object Object}s that uses {@link java.lang.annotation.Annotation Annotation}s
 * as helpers for creating {@link com.jagrosh.jdautilities.commandclient.Command Command}s.
 *
 * <p>Previous to version 1.6 all Commands required the Command abstract class to be extended in source.
 * The primary issue that came with this was that Commands were restricted to that method of creation, offering
 * no support for popular means such as annotated commands.
 *
 * <p>Since 1.6 the introduction of {@link com.jagrosh.jdautilities.commandclient.CommandBuilder CommandBuilder}
 * has allowed the potential to create unique {@link com.jagrosh.jdautilities.commandclient.Command Command}
 * objects after compilation.
 * <br>The primary duty of this class is to provide a "in runtime" converter for generics that are annotated with
 * the {@link JDACommand.Module JDACommand.Module}
 *
 * @since  1.7
 * @author Kaidan Gustave
 */
public class AnnotatedCommandCompiler
{
    private static final SimpleLog LOG = SimpleLog.getLog("Annotated Compiler");

    public List<Command> compileModule(Object o)
    {
        JDACommand.Module module = o.getClass().getAnnotation(JDACommand.Module.class);
        if(module == null) // TODO
            throw new IllegalArgumentException("");
        if(module.value().length<1) // TODO
            throw new IllegalArgumentException("");

        List<Method> commands = collect((Method method) -> {
            for(String name : module.value())
            {
                if(name.equalsIgnoreCase(method.getName()))
                    return true;
            }
            return false;
        }, o.getClass().getMethods());

        List<Command> list = new ArrayList<>();
        commands.forEach(method -> {
            try {
                list.add(compile(o, method));
            } catch(MalformedParametersException e) {
                LOG.fatal(e.getMessage());
            }
        });
        return list;
    }

    private Command compile(Object o, Method method) throws MalformedParametersException
    {
        JDACommand properties = method.getAnnotation(JDACommand.class);
        if(properties == null) // TODO
            throw new IllegalArgumentException("");
        CommandBuilder builder = new CommandBuilder();

        // Name
        String[] names = properties.name();
        builder.setName(names.length < 1 ? "null" : names[0]);

        // Aliases
        if(names.length>1)
            for(int i = 1; i<names.length; i++)
                builder.addAlias(names[i]);

        // Help
        builder.setHelp(properties.help());

        // Arguments
        builder.setArguments(properties.arguments().trim().isEmpty()? null : properties.arguments().trim());

        // Guild Only
        builder.setGuildOnly(properties.guildOnly());

        // Required Role
        builder.setRequiredRole(properties.requiredRole().trim().isEmpty()? null : properties.requiredRole().trim());

        // Owner Command
        builder.setOwnerCommand(properties.ownerCommand());

        // Cooldown Delay
        builder.setCooldown(properties.cooldown().value());

        // Cooldown Scope
        builder.setCooldownScope(properties.cooldown().scope());

        // Bot Permissions
        builder.setBotPermissions(properties.botPermissions());

        // User Permissions
        builder.setUserPermissions(properties.userPermissions());

        // Uses Topic Tags
        builder.setUsesTopicTags(properties.useTopicTags());

        // Child Commands
        if(properties.children().length>0)
        {
            collect((Method m) -> {
                for(String cName : properties.children())
                {
                    if(cName.equalsIgnoreCase(m.getName()))
                        return true;
                }
                return false;
            }, o.getClass().getMethods()).forEach(cm -> {
                try {
                    builder.addChild(compile(o, cm));
                } catch(MalformedParametersException e) {
                    LOG.fatal(e.getMessage());
                }
            });
        }


        Class<?>[] parameters = method.getParameterTypes();
        // Dual Parameter Command, CommandEvent
        if(parameters[0] == Command.class && parameters[1] == CommandEvent.class)
        {
            return builder.build((command, event) -> {
                try {
                    method.invoke(o, command, event);
                } catch(IllegalAccessException | InvocationTargetException e) {
                    LOG.log(e);
                }
            });
        }
        else if(parameters[0] == CommandEvent.class)
        {
            // Single parameter CommandEvent
            if(parameters.length == 1)
                return builder.build(event -> {
                    try {
                        method.invoke(o, event);
                    } catch(IllegalAccessException | InvocationTargetException e) {
                        LOG.log(e);
                    }
                });
            // Dual Parameter CommandEvent, Command
            else if(parameters[1] == Command.class)
                return builder.build((command, event) -> {
                    try {
                        method.invoke(o, event, command);
                    } catch(IllegalAccessException | InvocationTargetException e) {
                        LOG.log(e);
                    }
                });
        }

        // If we reach this point there is a malformed method and we shouldn't finish the compilation.
        throw new MalformedParametersException(
                "Method named "+method.getName()+" was not compiled due to improper parameter types!");
    }

    @SafeVarargs
    private static <T> List<T> collect(Predicate<T> filter, T... entities)
    {
        List<T> list = new ArrayList<>();
        for(T entity : entities)
        {
            if(filter.test(entity))
                list.add(entity);
        }
        return list;
    }

}
