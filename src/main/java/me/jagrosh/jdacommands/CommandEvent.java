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
package me.jagrosh.jdacommands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.utils.SimpleLog;

/**
 *
 * @author John Grosh (jagrosh)
 */
public class CommandEvent {
    public static int MAX_MESSAGES = 2;
    
    private final MessageReceivedEvent event;
    private final String args;
    private final CommandClient client;
    
    /**
     * Constructor for a CommandEvent. You should not call this! It is a
     * generated wrapper for a MessageReceivedEvent.
     * 
     * @param event
     * @param args 
     * @param client 
     */
    public CommandEvent(MessageReceivedEvent event, String args, CommandClient client)
    {
        this.event = event;
        this.args = args == null ? "" : args;
        this.client = client;
    }
    
    /**
     * Returns the user's text input for the command
     * 
     * @return never-null arguments that a user has supplied to a command
     */
    public String getArgs()
    {
        return args;
    }
    
    /**
     * Returns the underlying MessageReceivedEvent for this CommandEvent
     * 
     * @return the underlying MessageReceivedEvent
     */
    public MessageReceivedEvent getEvent()
    {
        return event;
    }
    
    /**
     * Returns the CommandClientImpl that initiated this event
     * @return the initiating CommandClientImpl
     */
    public CommandClient getClient()
    {
        return client;
    }
    
    
    // functional calls
    
    /**
     * Replies with a String
     * @param message the message to reply
     */
    public void reply(String message)
    {
        sendMessage(event.getChannel(), message);
    }
    
    /**
     * Replies with an embed
     * @param embed the embed to reply
     */
    public void reply(MessageEmbed embed)
    {
        event.getChannel().sendMessage(embed).queue();
    }
    
    /**
     * Replies with a file
     * @param file the file to reply with
     * @param filename the filename that Discord should display (null for default)
     * @throws java.io.IOException
     */
    public void reply(File file, String filename) throws IOException
    {
        event.getChannel().sendFile(file, filename, null).queue();
    }
    
    /**
     * Replies with a message and a file
     * @param message the message
     * @param file the file
     * @param filename the filename that Discord should display (null for default)
     * @throws java.io.IOException
     */
    public void reply(String message, File file, String filename) throws IOException
    {
        Message msg = message==null ? null : new MessageBuilder().append(splitMessage(message).get(0)).build();
        event.getChannel().sendFile(file, filename, msg);
    }
    
    /**
     * Replies with an embed if possible, or just a string if it cannot embed
     * 
     * @param embed the embed to send
     * @param alternateMessage the message if the embed cannot be sent
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
     * Replies with a file if possible, or a string if it cannot upload
     * @param message the message to send with the file
     * @param file the file
     * @param filename the filename that Discord should display
     * @param alternateMessage the message if the file cannot be uploaded
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
     * Replies to a message via Direct Message
     * @param message the string to send
     */
    public void replyInDM(String message)
    {
        if(event.isFromType(ChannelType.PRIVATE))
            reply(message);
        else
        {
            if(event.getAuthor().hasPrivateChannel())
                sendMessage(event.getAuthor().getPrivateChannel(), message);
            else
                event.getAuthor().openPrivateChannel().queue(pc -> sendMessage(pc, message));
        }
    }
    
    /**
     * Replies to a message via Direct Message
     * @param embed the embed to send
     */
    public void replyInDM(MessageEmbed embed)
    {
        if(event.isFromType(ChannelType.PRIVATE))
            reply(embed);
        else
        {
            if(event.getAuthor().hasPrivateChannel())
                event.getAuthor().getPrivateChannel().sendMessage(embed).queue();
            else
                event.getAuthor().openPrivateChannel().queue(pc -> pc.sendMessage(embed).queue());
        }
    }
    
    /**
     * Replies to a message via Direct Message
     * @param message the message to send (can be null)
     * @param file the file to send
     * @param filename the file name for Discord to display
     * @throws java.io.IOException
     */
    public void replyInDm(String message, File file, String filename) throws IOException
    {
        if(event.isFromType(ChannelType.PRIVATE))
            reply(message, file, filename);
        else
        {
            Message msg = message==null ? null : new MessageBuilder().append(splitMessage(message).get(0)).build();
            if(event.getAuthor().hasPrivateChannel())
                event.getAuthor().getPrivateChannel().sendFile(file, filename, msg);
            else
                event.getAuthor().openPrivateChannel().queue(pc -> {
                try {
                    pc.sendFile(file, filename, msg);
                } catch (IOException ex) {
                    SimpleLog.getLog("Commands").warn(ex);
                }
            });
        }
    }
    
    /**
     * Adds a success reaction
     */
    public void reactSuccess()
    {
        Emote emote = parseEmote(client.getSuccess());
        if(emote==null)
            event.getMessage().addReaction(client.getSuccess()).queue();
        else
            event.getMessage().addReaction(emote).queue();
    }
    
    /**
     * Adds a warning reaction
     */
    public void reactWarning()
    {
        Emote emote = parseEmote(client.getWarning());
        if(emote==null)
            event.getMessage().addReaction(client.getWarning()).queue();
        else
            event.getMessage().addReaction(emote).queue();
    }
    
    /**
     * Adds an error reaction
     */
    public void reactEror()
    {
        Emote emote = parseEmote(client.getError());
        if(emote==null)
            event.getMessage().addReaction(client.getError()).queue();
        else
            event.getMessage().addReaction(emote).queue();
    }
    
    
    //private methods
    private Emote parseEmote(String text)
    {
        String id = text.replaceAll("<:.+:(\\d+)>", "$1");
        return event.getJDA().getEmoteById(id);
    }
    
    private static void sendMessage(MessageChannel chan, String message)
    {
        ArrayList<String> messages = splitMessage(message);
        for(int i=0; i<MAX_MESSAGES && i<messages.size(); i++)
        {
            chan.sendMessage(messages.get(i)).queue();
        }
    }
    
    private static ArrayList<String> splitMessage(String stringtoSend)
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
    public SelfUser getSelfUser()
    {
        return event.getJDA().getSelfUser();
    }
    
    public Member getSelfMember()
    {
        return event.getGuild() == null ? null : event.getGuild().getSelfMember();
    }
    
    
    // shortcuts
    public User getAuthor()
    {
        return event.getAuthor();
    }
    
    public MessageChannel getChannel()
    {
        return event.getChannel();
    }
    
    public ChannelType getChannelType()
    {
        return event.getChannelType();
    }
    
    public Group getGroup()
    {
        return event.getGroup();
    }
    
    public Guild getGuild()
    {
        return event.getGuild();
    }
    
    public JDA getJDA()
    {
        return event.getJDA();
    }
    
    public Member getMember()
    {
        return event.getMember();
    }
    
    public Message getMessage()
    {
        return event.getMessage();
    }
    
    public PrivateChannel getPrivateChannel()
    {
        return event.getPrivateChannel();
    }
    
    public long getResponseNumber()
    {
        return event.getResponseNumber();
    }
    
    public TextChannel getTextChannel()
    {
        return event.getTextChannel();
    }
    
    public boolean isFromType(ChannelType channelType)
    {
        return event.isFromType(channelType);
    }
}
