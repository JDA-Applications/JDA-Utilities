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
package com.jagrosh.jdautilities.commandclient;

import com.jagrosh.jdautilities.commandclient.Command.*;

import net.dv8tion.jda.core.Permission;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

// TODO documentation for class and methods
/**
 * @since  1.5
 * @author Kaidan Gustave
 */
public class CommandBuilder
{
    private String name = "null";
    private String help = "no help available";
    private Category category = null;
    private String arguments = null;
    private boolean guildOnly = true;
    private String requiredRole = null;
    private boolean ownerCommand = false;
    private int cooldown = 0;
    private Permission[] userPermissions = new Permission[0];
    private Permission[] botPermissions = new Permission[0];
    private String[] aliases = new String[0];
    private Command[] children = new Command[0];
    private BiConsumer<CommandEvent, Command> helpBiConsumer = null;
    private boolean usesTopicTags = true;
    private CooldownScope cooldownScope = CooldownScope.USER;

    public CommandBuilder setName(String name)
    {
        if(name == null)
            this.name = "null";
        else
            this.name = name;
        return this;
    }

    public CommandBuilder setHelp(String help)
    {
        if(help == null)
            this.help = "no help available";
        else
            this.help = help;
        return this;
    }

    public CommandBuilder setCategory(Category category)
    {
        this.category = category;
        return this;
    }

    public CommandBuilder setArguments(String arguments)
    {
        this.arguments = arguments;
        return this;
    }

    public CommandBuilder setGuildOnly(boolean guildOnly)
    {
        this.guildOnly = guildOnly;
        return this;
    }

    public CommandBuilder setRequiredRole(String requiredRole)
    {
        this.requiredRole = requiredRole;
        return this;
    }

    public CommandBuilder setOwnerCommand(boolean ownerCommand)
    {
        this.ownerCommand = ownerCommand;
        return this;
    }

    public CommandBuilder setCooldown(int cooldown)
    {
        this.cooldown = cooldown;
        return this;
    }

    public CommandBuilder setUserPermissions(Permission[] userPermissions)
    {
        if(userPermissions == null)
            this.userPermissions = new Permission[0];
        else
            this.userPermissions = userPermissions;
        return this;
    }

    public CommandBuilder setBotPermissions(Permission[] botPermissions)
    {
        if(botPermissions == null)
            this.botPermissions = new Permission[0];
        else
            this.botPermissions = botPermissions;
        return this;
    }

    public CommandBuilder setAliases(String[] aliases)
    {
        if(aliases == null)
            this.aliases = new String[0];
        else
            this.aliases = aliases;
        return this;
    }

    public CommandBuilder setChildren(Command[] children)
    {
        if(children == null)
            this.children = new Command[0];
        else
            this.children = children;
        return this;
    }

    public CommandBuilder setHelpBiConsumer(BiConsumer<CommandEvent, Command> helpBiConsumer)
    {
        this.helpBiConsumer = helpBiConsumer;
        return this;
    }

    public CommandBuilder setUsesTopicTags(boolean usesTopicTags)
    {
        this.usesTopicTags = usesTopicTags;
        return this;
    }

    public CommandBuilder setCooldownScope(CooldownScope cooldownScope)
    {
        if(cooldownScope == null)
            this.cooldownScope = CooldownScope.USER;
        else
            this.cooldownScope = cooldownScope;
        return this;
    }

    public Command build(Consumer<CommandEvent> execution)
    {
        return build((c, e) -> { execution.accept(e); });
    }

    public Command build(BiConsumer<Command,CommandEvent> execution)
    {
        return new BlankCommand(name, help, category, arguments,
                guildOnly, requiredRole, ownerCommand, cooldown,
                userPermissions, botPermissions, aliases, children,
                helpBiConsumer, usesTopicTags, cooldownScope) {

            @Override
            protected void execute(CommandEvent event)
            {
                execution.accept(this,event);
            }
        };
    }

    private abstract class BlankCommand extends Command
    {
        BlankCommand(String name, String help, Category category,
                     String arguments, boolean guildOnly, String requiredRole,
                     boolean ownerCommand, int cooldown, Permission[] userPermissions,
                     Permission[] botPermissions, String[] aliases, Command[] children,
                     BiConsumer<CommandEvent, Command> helpBiConsumer,
                     boolean usesTopicTags, CooldownScope cooldownScope)
        {
            this.name = name;
            this.help = help;
            this.category = category;
            this.arguments = arguments;
            this.guildOnly = guildOnly;
            this.requiredRole = requiredRole;
            this.ownerCommand = ownerCommand;
            this.cooldown = cooldown;
            this.userPermissions = userPermissions;
            this.botPermissions = botPermissions;
            this.aliases = aliases;
            this.children = children;
            this.helpBiConsumer = helpBiConsumer;
            this.usesTopicTags = usesTopicTags;
            this.cooldownScope = cooldownScope;
        }
    }
}
