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
package com.jagrosh.jdautilities.commandclient.annotation;

import com.jagrosh.jdautilities.commandclient.Command;
import net.dv8tion.jda.core.Permission;

import java.lang.annotation.*;

/**
 * An Annotation applicable to {@link java.lang.reflect.Method Method}s that will act as
 * {@link com.jagrosh.jdautilities.commandclient.Command Command}s when added to a Client
 * using {@link com.jagrosh.jdautilities.commandclient.CommandClientBuilder#addAnnotatedModule(Object)
 * CommandClientBuilder#addAnnotatedModule()} serving as metadata "constructors" for what
 * would be a class extending Command of the same functionality and settings.
 *
 * <p>The primary issue that command systems face when trying to implement "annotated command"
 * systems is that reflection is a powerful but also costly tool and requires much more overhead
 * than most other types systems.
 *
 * To circumvent this, classes annotated with this are put through an {@link
 * com.jagrosh.jdautilities.commandclient.AnnotatedModuleCompiler AnnotatedModuleCompiler}.
 * where they will be converted to Commands using {@link com.jagrosh.jdautilities.commandclient.CommandBuilder
 * CommandBuilder}.
 *
 * <p>Classes that wish to be contain methods to be used as commands must be annotated with
 * {@link com.jagrosh.jdautilities.commandclient.annotation.JDACommand.Module @Module}.
 * <br>Following that, any methods of said class annotated with this annotation (whose names
 * are also given as parameters of the {@code @Module} annotation) will be registered to the
 * module and "compiled" through the AnnotatedModuleCompiler provided in CommandClientBuilder.
 *
 * <pre><code>   {@link JDACommand.Module @JDACommand.Module}({@link Module#value() value} = "example")
 * public class AnnotatedModuleCmd {
 *
 *     {@literal @JDACommand(}
 *          {@link JDACommand#name() name} = {"example", "test", "demo"},
 *          {@link JDACommand#help() help} = "gives an example of what commands do"
 *      )
 *      public void example(CommandEvent) {
 *          event.reply("Hey look! This would be the bot's reply if this was a command!");
 *      }
 *
 * }</code></pre>
 *
 * @see    com.jagrosh.jdautilities.commandclient.annotation.JDACommand.Module
 *
 * @since  1.7
 * @author Kaidan Gustave
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JDACommand
{
    /**
     * The name and aliases of the command.
     *
     * <p>The first index is the name, and following indices are aliases.
     *
     * @return An array of strings, the first one being the name
     * of the command, and following ones being aliases.
     */
    String[] name() default {"null"};

    /**
     * The help string for a command.
     *
     * @return The help string for a command.
     */
    String help() default "no help available";

    /**
     * Whether or not the command is only usable in a guild.
     * <br>Default {@code true}.
     *
     * @return {@code true} if the command can only be used in a guild,
     * {@code false} otherwise.
     */
    boolean guildOnly() default true;

    /**
     * The name of a role required to use this command.
     *
     * @return The name of a role required to use this command.
     */
    String requiredRole() default "";

    /**
     * Whether or not the command is owner only.
     * <br>Default {@code true}.
     *
     * @return {@code true} if the command is owner only, {@code false} otherwise.
     */
    boolean ownerCommand() default false;

    /**
     * The arguments string for the command.
     *
     * @return The arguments string for the command.
     */
    String arguments() default "";

    /**
     * The {@link JDACommand.Cooldown JDACommand.Cooldown} for the command.
     *
     * <p>This holds both metadata for both the
     * {@link com.jagrosh.jdautilities.commandclient.Command#cooldown Command#cooldown}
     * and {@link com.jagrosh.jdautilities.commandclient.Command#cooldownScope
     * Command#cooldownScope}.
     *
     * @return The {@code @Cooldown} for the command.
     */
    Cooldown cooldown() default @Cooldown(0);

    /**
     * The {@link net.dv8tion.jda.core.Permission Permissions} the bot must have
     * on a guild to use this command.
     *
     * @return The required permissions the bot must have to use this command.
     */
    Permission[] botPermissions() default {};

    /**
     * The {@link net.dv8tion.jda.core.Permission Permissions} the user must have
     * on a guild to use this command.
     *
     * @return The required permissions a user must have to use this command.
     */
    Permission[] userPermissions() default {};

    /**
     * Whether or not this command uses topic tags.
     * <br>Default {@code true}.
     *
     * <p>For more information on topic tags, see
     * {@link com.jagrosh.jdautilities.commandclient.Command#usesTopicTags
     * Command#usesTopicTags}
     *
     * @return {@code true} if this command uses topic tags, {@code false} otherwise.
     */
    boolean useTopicTags() default true;

    /**
     * The names of any methods representing child commands for this command.
     *
     * @return The names of any methods representing child commands.
     */
    String[] children() default {};

    /**
     * Whether or not this command should remain hidden in the help builder.
     *
     * @return {@code true} if this command should remain hidden, {@code false} otherwise.
     */
    boolean isHidden() default false;

    /**
     * The {@link JDACommand.Category JDACommand.Category} for this command.
     * <br>This holds data to properly locate a <b>static field</b> representing
     * this command's {@link com.jagrosh.jdautilities.commandclient.Command.Category
     * Category}.
     *
     * @return The {@code @Category} for this command.
     */
    Category category() default @Category(name = "null", location = Category.class);

    /**
     * A helper annotation to assist in location of methods that will generate
     * into {@link com.jagrosh.jdautilities.commandclient.Command Command}s.
     *
     * <p>Method names provided to this annotation must have one or two parameters.
     * Either a single parameter {@link com.jagrosh.jdautilities.commandclient.CommandEvent
     * CommandEvent}, or a double parameter {@code CommandEvent} and {@code Command}.
     * <br>The arrangement of the double parameters is not important, so methods
     * may do it as {@code (CommandEvent, Command)} or {@code (Command, CommandEvent)}.
     *
     * @see    com.jagrosh.jdautilities.commandclient.annotation.JDACommand
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Module
    {
        /**
         * The names of any methods that will be targeted when compiling this object
         * using the {@link com.jagrosh.jdautilities.commandclient.AnnotatedModuleCompiler
         * AnnotatedModuleCompiler}.
         *
         * <p><b>This is not the same thing as the name of the commands!</b> These are
         * the names of the methods representing execution of the commands!
         *
         * @return An array of method names used when creating commands.
         */
        String[] value();
    }

    /**
     * A value wrapper for what would be {@link com.jagrosh.jdautilities.commandclient.Command#cooldown
     * Command#cooldown} and {@link com.jagrosh.jdautilities.commandclient.Command#cooldownScope
     * Command#cooldownScope}.
     *
     * The default {@link com.jagrosh.jdautilities.commandclient.Command.CooldownScope CooldownScope}
     * is {@link com.jagrosh.jdautilities.commandclient.Command.CooldownScope#USER CooldownScope.USER}.
     *
     * @see    com.jagrosh.jdautilities.commandclient.annotation.JDACommand#cooldown()
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Cooldown
    {
        /**
         * The number of seconds the annotated Command will be on cooldown.
         *
         * @return The number of seconds the annotated Command will be on cooldown.
         */
        int value();

        /**
         * The {@link com.jagrosh.jdautilities.commandclient.Command.CooldownScope CooldownScope}
         * for the annotated Command.
         *
         * <p>By default this is {@link com.jagrosh.jdautilities.commandclient.Command.CooldownScope#USER
         * CooldownScope.USER}.
         *
         * @return The CooldownScope for this annotated Command.
         */
        Command.CooldownScope scope() default Command.CooldownScope.USER;
    }

    /**
     * A helper annotation to assist in location of Category instance.
     *
     * <p>This will target a <b>static field</b> in the specified class
     * {@link Category#location() location} using reflections, with a
     * matching {@link Category#name() name}.
     *
     * <p>It is important to remember the target must be a <b>static field</b>
     * and any other attempted inputs will result in errors from the
     * {@link com.jagrosh.jdautilities.commandclient.AnnotatedModuleCompiler compiler}.
     *
     * @see    com.jagrosh.jdautilities.commandclient.annotation.JDACommand#category()
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Category
    {
        /**
         * The name of the <b>static field</b> in the {@link Category#location()
         * target class} that will be the category for the annotated command.
         *
         * @return The name of the <b>static field</b> in the target class.
         */
        String name();

        /**
         * The target class where the <b>static field</b> is located.
         *
         * @return The target class where the <b>static field</b> is located.
         */
        Class<?> location();
    }

}
