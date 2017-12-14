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
package com.jagrosh.jdautilities.menu.orderedmenu;

import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import com.jagrosh.jdautilities.menu.Menu;
import com.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.PermissionUtil;

/**
 * Scheduled for reallocation in 2.0
 *
 * <p>Full information on these and other 2.0 deprecations and changes can be found
 * <a href="https://gist.github.com/TheMonitorLizard/4f09ac2a3c9d8019dc3cde02cc456eee">here</a>
 *
 * @author John Grosh
 */
public class OrderedMenu extends Menu {
    private final Color color;
    private final String text;
    private final String description;
    private final List<String> choices;
    private final BiConsumer<Message, Integer> action;
    private final Consumer<Message> cancel;
    private final boolean useLetters;
    private final boolean allowTypedInput;
    private final boolean useCancel;
    
    public final static String[] NUMBERS = new String[]{"1\u20E3","2\u20E3","3\u20E3",
        "4\u20E3","5\u20E3","6\u20E3","7\u20E3","8\u20E3","9\u20E3", "\uD83D\uDD1F"};
    public final static String[] LETTERS = new String[]{"\uD83C\uDDE6","\uD83C\uDDE7","\uD83C\uDDE8",
        "\uD83C\uDDE9","\uD83C\uDDEA","\uD83C\uDDEB","\uD83C\uDDEC","\uD83C\uDDED","\uD83C\uDDEE","\uD83C\uDDEF"};
    public final static String CANCEL = "\u274C";
    
    protected OrderedMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
            Color color, String text, String description, List<String> choices, BiConsumer<Message, Integer> action, Consumer<Message> cancel,
            boolean useLetters, boolean allowTypedInput, boolean useCancel)
    {
        super(waiter, users, roles, timeout, unit);
        this.color = color;
        this.text = text;
        this.description = description;
        this.choices = choices;
        this.action = action;
        this.cancel = cancel;
        this.useLetters = useLetters;
        this.allowTypedInput = allowTypedInput;
        this.useCancel = useCancel;
    }

    /**
     * Shows the OrderedMenu as a new {@link net.dv8tion.jda.core.entities.Message Message} 
     * in the provided {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * 
     * @param  channel
     *         The MessageChannel to send the new Message to
     */
    @Override
    public void display(MessageChannel channel) {
        if(channel.getType()==ChannelType.TEXT 
                && !allowTypedInput 
                && !PermissionUtil.checkPermission((TextChannel)channel, ((TextChannel)channel).getGuild().getSelfMember(), Permission.MESSAGE_ADD_REACTION))
            throw new PermissionException("Must be able to add reactions if not allowing typed input!");
        initialize(channel.sendMessage(getMessage()));
    }

    /**
     * Displays this OrderedMenu by editing the provided 
     * {@link net.dv8tion.jda.core.entities.Message Message}.
     * 
     * @param  message
     *         The Message to display the Menu in
     */
    @Override
    public void display(Message message) {
        if(message.getChannelType()==ChannelType.TEXT 
                && !allowTypedInput 
                && !PermissionUtil.checkPermission(message.getTextChannel(), message.getGuild().getSelfMember(), Permission.MESSAGE_ADD_REACTION))
            throw new PermissionException("Must be able to add reactions if not allowing typed input!");
        initialize(message.editMessage(getMessage()));
    }
    
    private void initialize(RestAction<Message> ra)
    {
        ra.queue(m -> {
            try{
                for(int i=1; i<=choices.size(); i++)
                {
                    if(i<choices.size())
                        m.addReaction(getEmoji(i)).queue();
                    else 
                    {

                            RestAction<Void> re = m.addReaction(getEmoji(i));
                            if(useCancel)
                            {
                                re.queue();
                                re = m.addReaction(CANCEL);
                            }
                            re.queue(v -> {
                                if(allowTypedInput)
                                    waitGeneric(m);
                                else
                                    waitReactionOnly(m);
                            });
                    }
                }
            }catch(PermissionException ex){
                if(allowTypedInput)
                    waitGeneric(m);
                else
                    waitReactionOnly(m);
            }
        });
    }
    
    private void waitGeneric(Message m)
    {
        waiter.waitForEvent(Event.class, e -> {
                if(e instanceof MessageReactionAddEvent)
                    return isValidReaction(m, (MessageReactionAddEvent)e);
                if(e instanceof MessageReceivedEvent)
                    return isValidMessage(m, (MessageReceivedEvent)e);
                return false;
            }, e -> {
                m.delete().queue();
                if(e instanceof MessageReactionAddEvent)
                {
                    MessageReactionAddEvent event = (MessageReactionAddEvent)e;
                    if(event.getReactionEmote().getName().equals(CANCEL))
                        cancel.accept(m);
                    else
                        action.accept(m, getNumber(event.getReactionEmote().getName()));
                }
                else if (e instanceof MessageReceivedEvent)
                {
                    MessageReceivedEvent event = (MessageReceivedEvent)e;
                    int num = getMessageNumber(event.getMessage().getContentRaw());
                    if(num<0 || num>choices.size())
                        cancel.accept(m);
                    else
                        action.accept(m, num);
                }
            }, timeout, unit, () -> cancel.accept(m));
    }
    
    private void waitReactionOnly(Message m)
    {
        waiter.waitForEvent(MessageReactionAddEvent.class, e -> {
                return isValidReaction(m, e);
            }, e -> {
                m.delete().queue();
                if(e.getReactionEmote().getName().equals(CANCEL))
                    cancel.accept(m);
                else
                    action.accept(m, getNumber(e.getReactionEmote().getName()));
            }, timeout, unit, () -> cancel.accept(m));
    }
    
    private Message getMessage()
    {
        MessageBuilder mbuilder = new MessageBuilder();
        if(text!=null)
            mbuilder.append(text);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<choices.size(); i++)
            sb.append("\n").append(getEmoji(i+1)).append(" ").append(choices.get(i));
        mbuilder.setEmbed(new EmbedBuilder().setColor(color).setDescription(description==null ? sb.toString() : description+sb.toString()).build());
        return mbuilder.build();
    }
    
    private boolean isValidReaction(Message m, MessageReactionAddEvent e)
    {
        if(!e.getMessageId().equals(m.getId()))
            return false;
        if(!isValidUser(e.getUser(), e.getGuild()))
            return false;
        if(e.getReactionEmote().getName().equals(CANCEL))
            return true;
        int num = getNumber(e.getReactionEmote().getName());
        return !(num<0 || num>choices.size());
    }
    
    private boolean isValidMessage(Message m, MessageReceivedEvent e)
    {
        if(!e.getChannel().equals(m.getChannel()))
            return false;
        return isValidUser(e.getAuthor(), e.getGuild());
    }
    
    private String getEmoji(int number)
    {
        if(useLetters)
            return LETTERS[number-1];
        else
            return NUMBERS[number-1];
    }
    
    private int getNumber(String emoji)
    {
        String[] array = useLetters ? LETTERS : NUMBERS;
        for(int i=0; i<array.length; i++)
            if(array[i].equals(emoji))
                return i+1;
        return -1;
    }
    
    private int getMessageNumber(String message)
    {
        if(useLetters)
            return message.length()==1 ? " abcdefghij".indexOf(message.toLowerCase()) : -1;
        else
        {
            if(message.length()==1)
                return " 123456789".indexOf(message);
            return message.equals("10") ? 10 : -1;
        }
    }
}
