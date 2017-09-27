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

import net.dv8tion.jda.core.utils.Helpers;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A incredibly lightweight interface for logging inside {@link com.jagrosh.jdautilities.commandclient.CommandClient
 * CommandClient}.
 *
 * <p>This can be implemented in full via a class, or via lambda expression as a functional interface, and applied
 * using {@link com.jagrosh.jdautilities.commandclient.CommandClientBuilder#setClientLogger(CommandClientLogger)
 * CommandClientBuilder#setClientLogger(CommandClientLogger)}, and is not restricted to any logging dependency such as
 * slf4j or Logback.
 *
 * @since  1.8
 * @author Kaidan Gustave
 */
@FunctionalInterface
public interface CommandClientLogger
{
    /**
     * Called after logging info has been processed.
     * <br>This only provides the message and whether or not the source requested it to be
     * logged as an error.
     *
     * @param  isError
     *         {@code true} if this should be logged as an error.
     * @param  msg
     *         The String message that should be logged.
     */
    void log(boolean isError, String msg);

    /**
     * Called whenever the CommandClient logs at a {@link LogLevel#INFO INFO} level.
     * <br>This should be {@link CommandClientLogger#log(boolean, String) logged} as a non-error.
     *
     * @param  msg
     *         The message that should be logged as a non-error.
     */
    default void info(Object msg)
    {
        log(LogLevel.INFO.isError(), LogLevel.simpleFormat(LogLevel.INFO, msg));
    }

    /**
     * Called whenever the CommandClient logs at a {@link LogLevel#WARN WARN} level.
     * <br>This should be {@link CommandClientLogger#log(boolean, String) logged} as a error.
     *
     * @param  msg
     *         The message that should be logged as a error.
     */
    default void warn(Object msg)
    {
        log(LogLevel.WARN.isError(), LogLevel.simpleFormat(LogLevel.WARN, msg));
    }

    /**
     * Called whenever the CommandClient logs at a {@link LogLevel#FATAL FATAL} level.
     * <br>This should be {@link CommandClientLogger#log(boolean, String) logged} as a error.
     * <br>This also may contain a stacktrace from {@link CommandClientLogger#exception(Object, Throwable)}.
     *
     * @param  msg
     *         The message that should be logged as a error.
     */
    default void error(Object msg)
    {
        log(LogLevel.FATAL.isError(), LogLevel.simpleFormat(LogLevel.FATAL, msg));
    }

    /**
     * Called whenever the CommandClient logs an {@link java.lang.Exception Exception}
     * and an extra message describing the Exception at a {@link LogLevel#FATAL FATAL} level.
     * <br>This should be {@link CommandClientLogger#log(boolean, String) logged} as a error.
     *
     * @param  msg
     *         The message that should be logged with the Exception.
     * @param  ex
     *         The Exception should be logged as a error.
     */
    default void exception(Object msg, Throwable ex)
    {
        error(String.valueOf(msg)+"\n"+Helpers.getStackTrace(ex));
    }

    /**
     * Called whenever the CommandClient logs an {@link java.lang.Exception Exception}
     * at a {@link LogLevel#FATAL FATAL} level.
     * <br>This should be {@link CommandClientLogger#log(boolean, String) logged} as a error.
     *
     * @param  ex
     *         The Exception should be logged as a error.
     */
    default void exception(Throwable ex)
    {
        error(ex);
    }

    /**
     * A set of three {@link java.lang.Enum Enum}s for describing the level at which
     * a call to log should be logged at.
     */
    enum LogLevel
    {
        /**
         * Information level: This is a non-error, indicative of proper execution
         * or other non-hazardous information.
         */
        INFO,

        /**
         * Warning level: Indicates a cause for concern, not necessarily something
         * fatal, but something that should be taken note of.
         */
        WARN,

        /**
         * Fatal level: Indicates a failure or other form of error that should be logged
         * to inform the developer that there is a possibly-serious internal issue.
         */
        FATAL;

        /**
         * Gets whether this LogLevel is indicative of an error.
         * <br>Only returns {@code true} for {@link LogLevel#WARN WARN}
         * or {@link LogLevel#FATAL FATAL} LogLevels.
         *
         * @return {@code true} if this LogLevel is indicative of an error
         */
        public boolean isError()
        {
            return this != INFO;
        }

        // Private members of this enum are internal implementation for the default methods
        // of CommandClientLogger.
        private static final String FORMAT = "[%time%][%type%][CommandClient] %msg%";
        private static final SimpleDateFormat DFORMAT = new SimpleDateFormat("HH:mm:ss");

        private static String simpleFormat(LogLevel level, Object msg)
        {
            if(msg instanceof Throwable)
                return FORMAT.replace("%time%", DFORMAT.format(new Date()))
                        .replace("%type%", level.toString())
                        .replace("%msg%",Helpers.getStackTrace((Throwable)msg));
            else
                return FORMAT.replace("%time%", DFORMAT.format(new Date()))
                        .replace("%type%", level.toString())
                        .replace("%msg%", String.valueOf(msg));
        }
    }
}
