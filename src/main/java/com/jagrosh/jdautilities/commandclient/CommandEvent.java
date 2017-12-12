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

import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;
import com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;

/**
 * A wrapper class for a {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent},
 * {@link com.jagrosh.jdautilities.commandclient.CommandClient CommandClient}, and String user arguments
 * compatible with all {@link com.jagrosh.jdautilities.commandclient.Command Command}s.
 * 
 * <p>From here, developers can invoke several useful and specialized methods to assist in Command function and
 * development. There are also "extension" methods for all methods found in MessageReceivedEvent.
 * 
 * <p>Methods with "reply" in their name can be used to instantly send a {@link net.dv8tion.jda.core.entities.Message Message} 
 * response to the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} the MessageReceivedEvent was in.
 * <br>All {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by sending a response using these
 * methods automatically {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}, and no further developer
 * input is required.
 * 
 * @author John Grosh (jagrosh)
 */
public class CommandEvent
{
    public static int MAX_MESSAGES = 2;
    
    private final MessageReceivedEvent event;
    private String args;
    private final CommandClient client;
    
    /**
     * Constructor for a CommandEvent.
     * 
     * <p><b>You should not call this!</b>
     * <br>It is a generated wrapper for a {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}.
     * 
     * @param  event
     *         The initial MessageReceivedEvent
     * @param  args
     *         The String arguments after the command call
     * @param  client
     *         The {@link com.jagrosh.jdautilities.commandclient.CommandClient CommandClient}
     */
    public CommandEvent(MessageReceivedEvent event, String args, CommandClient client)
    {
        this.event = event;
        this.args = args == null ? "" : args;
        this.client = client;
    }
    
    /**
     * Returns the user's String arguments for the command.
     * <br>If no arguments have been supplied, then this will return an empty String.
     * 
     * @return Never-null arguments that a user has supplied to a command
     */
    public String getArgs()
    {
        return args;
    }
    
    void setArgs(String args)
    {
        this.args = args;
    }
    
    /**
     * Returns the underlying {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     * for this CommandEvent.
     * 
     * @return The underlying MessageReceivedEvent
     */
    public MessageReceivedEvent getEvent()
    {
        return event;
    }
    
    /**
     * Returns the {@link com.jagrosh.jdautilities.commandclient.CommandClient CommandClient}
     * that initiated this CommandEvent.
     * 
     * @return The initiating CommandClient
     */
    public CommandClient getClient()
    {
        return client;
    }

    /**
     * Links a {@link net.dv8tion.jda.core.entities.Message Message} with the calling Message
     * contained by this CommandEvent.
     *
     * <p>This method is exposed for those who wish to use linked deletion but may require usage of
     * {@link net.dv8tion.jda.core.entities.MessageChannel#sendMessage(Message) MessageChannel#sendMessage()}
     * or for other reasons cannot use the standard {@code reply()} methods.
     *
     * <p>The following conditions must be met when using this method or an {@link java.lang.IllegalArgumentException
     * IllegalArgumentException} will be thrown:
     * <ul>
     *     <li>The Message provided is from the bot (IE: {@link net.dv8tion.jda.core.entities.SelfUser SelfUser}).</li>
     *     <li>The base {@link com.jagrosh.jdautilities.commandclient.CommandClient CommandClient} must be using
     *     linked deletion (IE: {@link com.jagrosh.jdautilities.commandclient.CommandClient#usesLinkedDeletion()
     *     CommandClient#usesLinkedDeletion()} returns {@code true})</li>
     * </ul>
     *
     * @param  message
     *         The Message to add, must be from the SelfUser while linked deletion is being used.
     *
     * @throws java.lang.IllegalArgumentException
     *         One or more of the criteria to use this method are not met (see above).
     */
    public void linkId(Message message)
    {
        if(!message.getAuthor().equals(getSelfUser()))
            throw new IllegalArgumentException("Attempted to link a Message who's author was not the bot!");
        if(!client.usesLinkedDeletion())
            throw new IllegalArgumentException("Linked Deletion has been disabled for this CommandClient!");
        ((CommandClientImpl)client).linkIds(event.getMessageIdLong(), message);
    }

    // functional calls
    
    /**
     * Replies with a String message.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}.
     * 
     * <p><b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent
     * in two split Messages.
     * 
     * @param  message
     *         A String message to reply with
     */
    public void reply(String message)
    {
        sendMessage(event.getChannel(), message);
    }
    
    /**
     * Replies with a String message and then queues a {@link java.util.function.Consumer}.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}
     * with the provided Consumer as it's success callback.
     * 
     * <p><b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent in 
     * two split Messages.
     * <br>The Consumer will be applied to the last message sent if this occurs.
     * 
     * @param  message
     *         A String message to reply with
     * @param  queue
     *         The Consumer to queue after sending the Message is sent.
     */
    public void reply(String message, Consumer<Message> queue)
    {
    	sendMessage(event.getChannel(), message, queue);
    }
    
    /**
     * Replies with a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}.
     * 
     * @param  embed
     *         The MessageEmbed to reply with
     */
    public void reply(MessageEmbed embed)
    {
        event.getChannel().sendMessage(embed).queue(m -> {
            if(event.isFromType(ChannelType.TEXT) && client.usesLinkedDeletion())
                linkId(m);
        });
    }
    
    /**
     * Replies with a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}
     * and then queues a {@link java.util.function.Consumer}.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}
     * with the provided Consumer as it's success callback.
     * 
     * @param  embed
     *         The MessageEmbed to reply with
     * @param  queue
     *         The Consumer to queue after sending the Message is sent.
     */
    public void reply(MessageEmbed embed, Consumer<Message> queue)
    {
    	event.getChannel().sendMessage(embed).queue(m -> {
    	    if(event.isFromType(ChannelType.TEXT) && client.usesLinkedDeletion())
    	        linkId(m);
    	    queue.accept(m);
        });
    }
    
    /**
     * Replies with a {@link net.dv8tion.jda.core.entities.Message Message}.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}.
     * 
     * @param  message
     *         The Message to reply with
     */
    public void reply(Message message)
    {
        event.getChannel().sendMessage(message).queue(m -> {
            if(event.isFromType(ChannelType.TEXT) && client.usesLinkedDeletion())
                linkId(m);
        });
    }
    
    /**
     * Replies with a {@link net.dv8tion.jda.core.entities.Message Message} and then
     * queues a {@link java.util.function.Consumer}.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}
     * with the provided Consumer as it's success callback.
     * 
     * @param  message
     *         The Message to reply with
     * @param  queue
     *         The Consumer to queue after sending the Message is sent.
     */
    public void reply(Message message, Consumer<Message> queue)
    {
        event.getChannel().sendMessage(message).queue(m -> {
            if(event.isFromType(ChannelType.TEXT) && client.usesLinkedDeletion())
                linkId(m);
            queue.accept(m);
        });
    }
    
    /**
     * Replies with a {@link java.io.File} with the provided name, or a default name
     * if left null.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}.
     * 
     * <p>This method uses {@link net.dv8tion.jda.core.entities.MessageChannel#sendFile(File, String, Message) MessageChannel#sendFile(File, String, Message)}
     * to send the File. For more information on what a bot may send using this, you may find the info in that method.
     * 
     * @param  file
     *         The File to reply with
     * @param  filename
     *         The filename that Discord should display (null for default).
     */
    public void reply(File file, String filename)
    {
        event.getChannel().sendFile(file, filename, null).queue();
    }
    
    /**
     * Replies with a String message and a {@link java.io.File} with the provided name, or a default 
     * name if left null.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}.
     * 
     * <p>This method uses {@link net.dv8tion.jda.core.entities.MessageChannel#sendFile(File, String, Message) MessageChannel#sendFile(File, String, Message)}
     * to send the File. For more information on what a bot may send using this, you may find the info in that method.
     * 
     * @param  message
     *         A String message to reply with
     * @param  file
     *         The File to reply with
     * @param  filename
     *         The filename that Discord should display (null for default).
     */
    public void reply(String message, File file, String filename)
    {
        Message msg = message==null ? null : new MessageBuilder().append(splitMessage(message).get(0)).build();
        event.getChannel().sendFile(file, filename, msg).queue();
    }
    
    /**
     * Replies with a formatted String message using the provided arguments.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}.
     * 
     * <p><b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent
     * in two split Messages.
     * 
     * @param  format
     *         A formatted String
     * @param  args
     *         The arguments to use with the format
     */
    public void replyFormatted(String format, Object... args)
    {
        sendMessage(event.getChannel(), String.format(format, args));
    }
    
    /**
     * Replies with a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} if possible, 
     * or just a String message if it cannot send the embed.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}.
     * 
     * <p><b>NOTE:</b> This alternate String message can exceed the 2000 character cap, and will 
     * be sent in two split Messages.
     * 
     * @param  embed
     *         The MessageEmbed to reply with
     * @param  alternateMessage
     *         A String message to reply with if the provided MessageEmbed cannot be sent
     */
    public void replyOrAlternate(MessageEmbed embed, String alternateMessage)
    {
        try {
            event.getChannel().sendMessage(embed).queue();
        } catch(PermissionException e) {
            reply(alternateMessage);
        }
    }
    
    /**
     * Replies with a String message and a {@link java.io.File} with the provided name, or a default 
     * name if left null.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}.
     * 
     * <p>This method uses {@link net.dv8tion.jda.core.entities.MessageChannel#sendFile(File, String, Message) MessageChannel#sendFile(File, String, Message)}
     * to send the File. For more information on what a bot may send using this, you may find the info in that method.
     * 
     * <p><b>NOTE:</b> This alternate String message can exceed the 2000 character cap, and will 
     * be sent in two split Messages.
     * 
     * <p>It is also worth noting that unlike {@link com.jagrosh.jdautilities.commandclient.CommandEvent#reply(File,String) CommandEvent#reply(File, String)}
     * and {@link com.jagrosh.jdautilities.commandclient.CommandEvent#reply(String,File,String) CommandEvent#reply(String, File, String)},
     * this method does not throw a {@link java.io.IOException}. This is because the cause of the alternate String message being sent comes directly from a 
     * thrown {@link java.lang.Exception}, and thus a thrown IOException is grounds for the sending of the alternate message.
     * 
     * @param  message
     *         A String message to reply with
     * @param  file
     *         The File to reply with
     * @param  filename
     *         The filename that Discord should display (null for default). 
     * @param  alternateMessage
     *         A String message to reply with if the file cannot be uploaded, or an {@link java.io.IOException} is thrown
     */
    public void replyOrAlternate(String message, File file, String filename, String alternateMessage)
    {
        Message msg = message==null ? null : new MessageBuilder().append(splitMessage(message).get(0)).build();
        try {
            event.getChannel().sendFile(file, filename, msg).queue();
        } catch(Exception e) {
            reply(alternateMessage);
        }
    }
    
    /**
     * Replies with a String message sent to the calling {@link net.dv8tion.jda.core.entities.User User}'s 
     * {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}.
     * 
     * <p>If the User to be Direct Messaged does not already have a PrivateChannel
     * open to send messages to, this method will automatically open one.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}.
     * 
     * <p><b>NOTE:</b> This alternate String message can exceed the 2000 character cap, and will 
     * be sent in two split Messages.
     * 
     * @param  message
     *         A String message to reply with
     *
     * @deprecated
     *         Scheduled for removal in 2.0, replaced with {@link #replyInDm(String)}
     */
    @Deprecated
    public void replyInDM(String message)
    {
        replyInDm(message);
    }

    /**
     * Replies with a String message sent to the calling {@link net.dv8tion.jda.core.entities.User User}'s
     * {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}.
     *
     * <p>If the User to be Direct Messaged does not already have a PrivateChannel
     * open to send messages to, this method will automatically open one.
     *
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message}
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}.
     *
     * <p><b>NOTE:</b> This alternate String message can exceed the 2000 character cap, and will
     * be sent in two split Messages.
     *
     * @param  message
     *         A String message to reply with
     */
    public void replyInDm(String message)
    {
        if(event.isFromType(ChannelType.PRIVATE))
            reply(message);
        else
        {
            event.getAuthor().openPrivateChannel().queue(pc -> sendMessage(pc, message));
        }
    }
    
    /**
     * Replies with a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} sent to the 
     * calling {@link net.dv8tion.jda.core.entities.User User}'s {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}.
     * 
     * <p>If the User to be Direct Messaged does not already have a PrivateChannel
     * open to send messages to, this method will automatically open one.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}.
     * 
     * @param  embed
     *         The MessageEmbed to reply with
     *
     * @deprecated
     *         Scheduled for removal in 2.0, replaced with {@link #replyInDm(MessageEmbed)}
     */
    @Deprecated
    public void replyInDM(MessageEmbed embed)
    {
        replyInDm(embed);
    }

    /**
     * Replies with a {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} sent to the
     * calling {@link net.dv8tion.jda.core.entities.User User}'s {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}.
     *
     * <p>If the User to be Direct Messaged does not already have a PrivateChannel
     * open to send messages to, this method will automatically open one.
     *
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message}
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}.
     *
     * @param  embed
     *         The MessageEmbed to reply with
     */
    public void replyInDm(MessageEmbed embed)
    {
        if(event.isFromType(ChannelType.PRIVATE))
            reply(embed);
        else
        {
            event.getAuthor().openPrivateChannel().queue(pc -> pc.sendMessage(embed).queue());
        }
    }
    
    /**
     * Replies with a String message and a {@link java.io.File} with the provided name, or a default 
     * name if left null, and sent to the calling {@link net.dv8tion.jda.core.entities.User User}'s 
     * {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel}.
     * 
     * <p>If the User to be Direct Messaged does not already have a PrivateChannel
     * open to send messages to, this method will automatically open one.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}.
     * 
     * <p>This method uses {@link net.dv8tion.jda.core.entities.MessageChannel#sendFile(File, String, Message) MessageChannel#sendFile(File, String, Message)}
     * to send the File. For more information on what a bot may send using this, you may find the info in that method.
     * 
     * @param  message
     *         A String message to reply with
     * @param  file
     *         The {@code File} to reply with
     * @param  filename
     *         The filename that Discord should display (null for default).
     */
    public void replyInDm(String message, File file, String filename)
    {
        if(event.isFromType(ChannelType.PRIVATE))
            reply(message, file, filename);
        else
        {
            Message msg = message==null ? null : new MessageBuilder().append(splitMessage(message).get(0)).build();
            event.getAuthor().openPrivateChannel().queue(pc -> pc.sendFile(file, filename, msg).queue());
        }
    }
    
    /**
     * Replies with a String message, and a prefixed success emoji.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}.
     * 
     * <p><b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent
     * in two split Messages.
     * 
     * @param  message
     *         A String message to reply with
     */
    public void replySuccess(String message)
    {
        reply(client.getSuccess()+" "+message);
    }

    /**
     * Replies with a String message and a prefixed success emoji and then
     * queues a {@link java.util.function.Consumer}.
     *
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message}
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}
     * with the provided Consumer as it's success callback.
     *
     * <p><b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent
     * in two split Messages.
     *
     * @param  message
     *         A String message to reply with
     * @param  queue
     *         The Consumer to queue after sending the Message is sent.
     */
    public void replySuccess(String message, Consumer<Message> queue)
    {
        reply(client.getSuccess()+" "+message, queue);
    }

    /**
     * Replies with a String message, and a prefixed warning emoji.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}.
     * 
     * <p><b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent
     * in two split Messages.
     * 
     * @param  message
     *         A String message to reply with
     */
    public void replyWarning(String message)
    {
        reply(client.getWarning()+" "+message);
    }

    /**
     * Replies with a String message and a prefixed warning emoji and then
     * queues a {@link java.util.function.Consumer}.
     *
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message}
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}
     * with the provided Consumer as it's success callback.
     *
     * <p><b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent
     * in two split Messages.
     *
     * @param  message
     *         A String message to reply with
     * @param  queue
     *         The Consumer to queue after sending the Message is sent.
     */
    public void replyWarning(String message, Consumer<Message> queue)
    {
        reply(client.getWarning()+" "+message, queue);
    }

    /**
     * Replies with a String message and a prefixed error emoji.
     * 
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message} 
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}.
     * 
     * <p><b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent
     * in two split Messages.
     * 
     * @param  message
     *         A String message to reply with
     */
    public void replyError(String message)
    {
        reply(client.getError()+" "+message);
    }

    /**
     * Replies with a String message and a prefixed error emoji and then
     * queues a {@link java.util.function.Consumer}.
     *
     * <p>The {@link net.dv8tion.jda.core.requests.RestAction RestAction} returned by
     * sending the response as a {@link net.dv8tion.jda.core.entities.Message Message}
     * automatically does {@link net.dv8tion.jda.core.requests.RestAction#queue() RestAction#queue()}
     * with the provided Consumer as it's success callback.
     *
     * <p><b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent
     * in two split Messages.
     *
     * @param  message
     *         A String message to reply with
     * @param  queue
     *         The Consumer to queue after sending the Message is sent.
     */
    public void replyError(String message, Consumer<Message> queue)
    {
        reply(client.getError()+" "+message, queue);
    }

    /**
     * Adds a success reaction to the calling {@link net.dv8tion.jda.core.entities.Message Message}.
     */
    public void reactSuccess()
    {
        react(client.getSuccess());
    }
    
    /**
     * Adds a warning reaction to the calling {@link net.dv8tion.jda.core.entities.Message Message}.
     */
    public void reactWarning()
    {
        react(client.getWarning());
    }
    
    /**
     * Adds an error reaction to the calling {@link net.dv8tion.jda.core.entities.Message Message}.
     */
    public void reactError()
    {
        react(client.getError());
    }
    
    
    //private methods

    private void react(String reaction)
    {
        if(reaction.isEmpty())
            return;
        try{
            Emote emote = parseEmote(reaction);
            if(emote==null)
                event.getMessage().addReaction(reaction).queue();
            else
                event.getMessage().addReaction(emote).queue();
        }catch(PermissionException ex){}
    }
    
    private Emote parseEmote(String text)
    {
        String id = text.replaceAll("<:.+:(\\d+)>", "$1");
        try {
            return event.getJDA().getEmoteById(id);
        } catch(Exception e) {
            return null;
        }
    }
    
    private void sendMessage(MessageChannel chan, String message)
    {
        ArrayList<String> messages = splitMessage(message);
        for(int i=0; i<MAX_MESSAGES && i<messages.size(); i++)
        {
            chan.sendMessage(messages.get(i)).queue(m -> {
                if(event.isFromType(ChannelType.TEXT) && client.usesLinkedDeletion())
                    linkId(m);
            });
        }
    }
    
    private void sendMessage(MessageChannel chan, String message, Consumer<Message> queue)
    {
        ArrayList<String> messages = splitMessage(message);
        for(int i=0; i<MAX_MESSAGES && i<messages.size(); i++)
        {
            if(i+1==MAX_MESSAGES || i+1==messages.size())
                chan.sendMessage(messages.get(i)).queue(m -> {
                    if(event.isFromType(ChannelType.TEXT) && client.usesLinkedDeletion())
                        linkId(m);
                    queue.accept(m);
                });
            else
                chan.sendMessage(messages.get(i)).queue(m -> {
                    if(event.isFromType(ChannelType.TEXT) && client.usesLinkedDeletion())
                        linkId(m);
                });
        }
    }


    /**
     * Splits a String into one or more Strings who's length does not exceed 2000 characters.
     * <br>Also nullifies usages of {@code @here} and {@code @everyone} so that they do not mention anyone.
     * <br>Useful for splitting long messages so that they can be sent in more than one 
     * {@link net.dv8tion.jda.core.entities.Message Message} at maximum potential length.
     * 
     * @param  stringtoSend
     *         The String to split and send
     *         
     * @return An {@link java.util.ArrayList ArrayList} containing one or more Strings, with nullified
     *         occurrences of {@code @here} and {@code @everyone}, and that do not exceed 2000 characters
     *         in length
     */
    public static ArrayList<String> splitMessage(String stringtoSend)
    {
        ArrayList<String> msgs =  new ArrayList<>();
        if(stringtoSend!=null)
        {
            stringtoSend = stringtoSend.replace("@everyone", "@\u0435veryone").replace("@here", "@h\u0435re").trim();
            while(stringtoSend.length()>2000)
            {
                int leeway = 2000 - (stringtoSend.length()%2000);
                int index = stringtoSend.lastIndexOf("\n", 2000);
                if(index<leeway)
                    index = stringtoSend.lastIndexOf(" ", 2000);
                if(index<leeway)
                    index=2000;
                String temp = stringtoSend.substring(0,index).trim();
                if(!temp.equals(""))
                    msgs.add(temp);
                stringtoSend = stringtoSend.substring(index).trim();
            }
            if(!stringtoSend.equals(""))
                msgs.add(stringtoSend);
        }
        return msgs;
    }


    // custom shortcuts
    
    /**
     * Gets a {@link net.dv8tion.jda.core.entities.SelfUser SelfUser} representing the bot.
     * <br>This is the same as invoking {@code event.getJDA().getSelfUser()}.
     * 
     * @return A User representing the bot
     */
    public SelfUser getSelfUser()
    {
        return event.getJDA().getSelfUser();
    }
    
    /**
     * Gets a {@link net.dv8tion.jda.core.entities.Member Member} representing the bot, or null
     * if the event does not take place on a {@link net.dv8tion.jda.core.entities.Guild Guild}.
     * <br>This is the same as invoking {@code event.getGuild().getSelfMember()}.
     * 
     * @return A possibly-null Member representing the bot
     */
    public Member getSelfMember()
    {
        return event.getGuild() == null ? null : event.getGuild().getSelfMember();
    }

    /**
     * Tests whether or not the {@link net.dv8tion.jda.core.entities.User User} who triggered this
     * event is an owner of the bot.
     * 
     * @return {@code true} if the User is the Owner, else {@code false}
     */
    public boolean isOwner()
    {
    	if(event.getAuthor().getId().equals(this.getClient().getOwnerId()))
    	    return true;
        if(this.getClient().getCoOwnerIds()==null)
            return false;
        for(String id : this.getClient().getCoOwnerIds())
            if(id.equals(event.getAuthor().getId()))
                return true;
        return false;
    }
    
    /**
     * Tests whether or not the {@link net.dv8tion.jda.core.entities.User User} who triggered this
     * event is a CoOwner of the bot.
     * 
     * @return {@code true} if the User is the CoOwner, else {@code false}
     *
     * @deprecated
     *         Set for removal in 2.0.
     *         <br>The idea of "co-owner" has undergone a revision.
     *         It is a principle that trying to discriminate between an owner
     *         and co-owner is a hindrance that idea.
     *         <br>You should optimally try to implement your own system,
     *         either through {@link com.jagrosh.jdautilities.commandclient.Command.Category
     *         Categories} or through some other means.
     *         <br>This function is now supported in one call to {@link #isOwner()}.
     *
     *         <p>Full information on these and other 2.0 deprecations and changes can be found
     *         <a href="https://gist.github.com/TheMonitorLizard/4f09ac2a3c9d8019dc3cde02cc456eee">here</a>
     */
    @Deprecated
    public boolean isCoOwner()
    {
    	if(this.getClient().getCoOwnerIds()==null)
    		return false;
    	for(String id : this.getClient().getCoOwnerIds())
    		if(id.equals(event.getAuthor().getId()))
    			return true;
    	return false;
    }
    
    
    // shortcuts
    
    /**
     * Gets the {@link net.dv8tion.jda.core.entities.User User} who triggered this CommandEvent.
     * 
     * @return The User who triggered this CommandEvent
     */
    public User getAuthor()
    {
        return event.getAuthor();
    }
    
    /**
     * Gets the {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} that the CommandEvent
     * was triggered on.
     * 
     * @return The MessageChannel that the CommandEvent was triggered on
     */
    public MessageChannel getChannel()
    {
        return event.getChannel();
    }
    
    /**
     * Gets the {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} of the 
     * {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel} that the CommandEvent was triggered on.
     * 
     * @return The ChannelType of the MessageChannel that this CommandEvent was triggered on
     */
    public ChannelType getChannelType()
    {
        return event.getChannelType();
    }
    
    /**
     * Gets the {@link net.dv8tion.jda.client.entities.Group Group} that this CommandEvent
     * was triggered in.
     * 
     * @return The Group that this CommandEvent was triggered in
     */
    public Group getGroup()
    {
        return event.getGroup();
    }
    
    /**
     * Gets the {@link net.dv8tion.jda.core.entities.Guild Guild} that this CommandEvent
     * was triggered on.
     * 
     * @return The Guild that this CommandEvent was triggered on
     */
    public Guild getGuild()
    {
        return event.getGuild();
    }
    
    /**
     * Gets the instance of {@link net.dv8tion.jda.core.JDA JDA} that this CommandEvent 
     * was caught by.
     * 
     * @return The instance of JDA that this CommandEvent was caught by
     */
    public JDA getJDA()
    {
        return event.getJDA();
    }
    
    /**
     * Gets the {@link net.dv8tion.jda.core.entities.Member Member} that triggered this CommandEvent.
     * 
     * @return The Member that triggered this CommandEvent
     */
    public Member getMember()
    {
        return event.getMember();
    }
    
    /**
     * Gets the {@link net.dv8tion.jda.core.entities.Message Message} responsible for triggering
     * this CommandEvent.
     * 
     * @return The Message responsible for the CommandEvent
     */
    public Message getMessage()
    {
        return event.getMessage();
    }
    
    /**
     * Gets the {@link net.dv8tion.jda.core.entities.PrivateChannel PrivateChannel} that this CommandEvent 
     * may have taken place on, or {@code null} if it didn't happen on a PrivateChannel.
     * 
     * @return The PrivateChannel that this CommandEvent may have taken place on, or null
     *         if it did not happen on a PrivateChannel.
     */
    public PrivateChannel getPrivateChannel()
    {
        return event.getPrivateChannel();
    }
    
    /**
     * Gets the response number for the {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}.
     * 
     * @return The response number for the MessageReceivedEvent
     */
    public long getResponseNumber()
    {
        return event.getResponseNumber();
    }
    
    /**
     * Gets the {@link net.dv8tion.jda.core.entities.TextChannel TextChannel} that this CommandEvent 
     * may have taken place on, or {@code null} if it didn't happen on a TextChannel.
     * 
     * @return The TextChannel this CommandEvent may have taken place on, or null
     *         if it did not happen on a TextChannel.
     */
    public TextChannel getTextChannel()
    {
        return event.getTextChannel();
    }
    
    /**
     * Compares a provided {@link net.dv8tion.jda.core.entities.ChannelType ChannelType} with the one this
     * CommandEvent occurred on, returning {@code true} if they are the same ChannelType.
     * 
     * @param  channelType
     *         The ChannelType to compare
     *         
     * @return {@code true} if the CommandEvent originated from a {@link net.dv8tion.jda.core.entities.MessageChannel}
     *         of the provided ChannelType, otherwise {@code false}.
     */
    public boolean isFromType(ChannelType channelType)
    {
        return event.isFromType(channelType);
    }
}
