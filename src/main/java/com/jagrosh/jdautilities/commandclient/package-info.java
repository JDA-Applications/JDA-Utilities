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

/**
 * Items in this package pertain to the {@link com.jagrosh.jdautilities.commandclient.CommandClient CommandClient} and
 * {@link com.jagrosh.jdautilities.commandclient.Command Commands}.
 * 
 * <p>All of the contents are used heavily in the {@link com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl CommandClientImpl}, 
 * and are summarized as follows:
 * <ul>
 *     <li>{@link com.jagrosh.jdautilities.commandclient.AnnotatedModuleCompiler AnnotatedModuleCompiler}
 *     <br>An interface to create Commands from annotated objects (More info on annotated commands can be found in the
 *     {@link com.jagrosh.jdautilities.commandclient.annotation.JDACommand JDACommand} documentation).</li>
 *
 *     <li>{@link com.jagrosh.jdautilities.commandclient.CommandBuilder CommandBuilder}
 *     <br>An chain builder for Commands.</li>
 *
 *     <li>{@link com.jagrosh.jdautilities.commandclient.Command Command}
 *     <br>An abstract class that can be inherited by classes to create Commands compatible with the {@code CommandClientImpl}.</li>
 *
 *     <li>{@link com.jagrosh.jdautilities.commandclient.CommandClient CommandClient}
 *     <br>An interface used for getting info set when building a {@code CommandClientImpl}.</li>
 *
 *     <li>{@link com.jagrosh.jdautilities.commandclient.CommandClientBuilder CommandClientBuilder}
 *     <br>A builder system used to create a {@code CommandClientImpl} across several optional chained methods.</li>
 *
 *     <li>{@link com.jagrosh.jdautilities.commandclient.CommandEvent CommandEvent}
 *     <br>A wrapper for a {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}, {@code CommandClient}, and String arguments. 
 *     The main basis for carrying information to be used in Commands.</li>
 *
 *     <li>{@link com.jagrosh.jdautilities.commandclient.CommandListener CommandListener}
 *     <br>An interface to be provided to a {@code CommandClientImpl} that can provide Command operations depending on the outcome of the call.</li>
 * </ul>
 */
package com.jagrosh.jdautilities.commandclient;