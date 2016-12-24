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
package me.jagrosh.jdautilities.menu.selectiondialog;

import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import me.jagrosh.jdautilities.menu.Menu;
import me.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.requests.RestAction;

/**
 *
 * @author John Grosh
 */
public class SelectionDialog extends Menu {
    
    private final List<String> choices;
    private final String leftEnd, rightEnd;
    private final String defaultLeft, defaultRight;
    private final Function<Integer,Color> color;
    private final boolean loop;
    private final Function<Integer,String> text;
    private final Consumer<Integer> success;
    private final Runnable cancel;
    
    public static final String UP = "\uD83D\uDD3C";
    public static final String DOWN = "\uD83D\uDD3D";
    public static final String SELECT = "\u2705";
    public static final String CANCEL = "\u274E";
    
    protected SelectionDialog(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
            List<String> choices, String leftEnd, String rightEnd, String defaultLeft, String defaultRight, 
            Function<Integer,Color> color, boolean loop, Consumer<Integer> success, Runnable cancel, Function<Integer,String> text)
    {
        super(waiter, users, roles, timeout, unit);
        this.choices = choices;
        this.leftEnd = leftEnd;
        this.rightEnd = rightEnd;
        this.defaultLeft = defaultLeft;
        this.defaultRight = defaultRight;
        this.color = color;
        this.loop = loop;
        this.success = success;
        this.cancel = cancel;
        this.text = text;
    }

    /**
     * Sends the dialog to the provided channel with the first item selected
     * @param channel the channel to send to
     */
    @Override
    public void display(MessageChannel channel) {
        showDialog(channel, 1);
    }

    /**
     * Edits the dialog into the provided message with the first item selected
     * @param message the message to edit the dialog into
     */
    @Override
    public void display(Message message) {
        showDialog(message, 1);
    }
    
    /**
     * Sends a dialog to the provided channel with the provided selection
     * @param channel the channel to send the dialog to
     * @param selection the selection to start on
     */
    public void showDialog(MessageChannel channel, int selection)
    {
        if(selection<1)
            selection = 1;
        else if(selection>choices.size())
            selection = choices.size();
        Message msg = render(selection);
        initialize(channel.sendMessage(msg), selection);
    }
    
    /**
     * Edits the dialog into the provided message with the provided selection
     * @param message the message to edit the dialog into
     * @param selection the selection to start on
     */
    public void showDialog(Message message, int selection)
    {
        if(selection<1)
            selection = 1;
        else if(selection>choices.size())
            selection = choices.size();
        Message msg = render(selection);
        initialize(message.editMessage(msg), selection);
    }
    
    private void initialize(RestAction<Message> action, int selection)
    {
        action.queue(m -> {
            if(choices.size()>1)
            {
                m.addReaction(UP).queue();
                m.addReaction(SELECT).queue();
                m.addReaction(CANCEL).queue();
                m.addReaction(DOWN).queue(v -> selectionDialog(m, selection), v -> selectionDialog(m, selection));
            }
            else
            {
                m.addReaction(SELECT).queue();
                m.addReaction(CANCEL).queue(v -> selectionDialog(m, selection), v -> selectionDialog(m, selection));
            }
        });
    }
    
    private void selectionDialog(Message message, int selection)
    {
        waiter.waitForEvent(MessageReactionAddEvent.class, event -> {
            if(!event.getMessageId().equals(message.getId()))
                return false;
            if(!(UP.equals(event.getReaction().getEmote().getName()) 
                    || DOWN.equals(event.getReaction().getEmote().getName())
                    || CANCEL.equals(event.getReaction().getEmote().getName())
                    || SELECT.equals(event.getReaction().getEmote().getName())))
                return false;
            return isValidUser(event);
        }, event -> {
            int newSelection = selection;
            switch(event.getReaction().getEmote().getName())
            {
                case UP:
                    if(newSelection>1)
                        newSelection--;
                    else if(loop)
                        newSelection = choices.size();
                    break;
                case DOWN:
                    if(newSelection<choices.size())
                        newSelection++;
                    else if(loop)
                        newSelection = 1;
                    break;
                case CANCEL:
                    message.deleteMessage().queue();
                    cancel.run();
                    return;
                case SELECT:
                    message.deleteMessage().queue();
                    success.accept(selection);
                    return;
            }
            event.getReaction().removeReaction(event.getUser()).queue();
            int n = newSelection;
            message.editMessage(render(n)).queue(m -> {
                selectionDialog(m, n);
            });
        }, timeout, unit, () -> {message.deleteMessage().queue(); cancel.run();});
    }
    
    private Message render(int selection)
    {
        StringBuilder sbuilder = new StringBuilder();
        for(int i=0; i<choices.size(); i++)
            if(i+1==selection)
                sbuilder.append("\n").append(leftEnd).append(choices.get(i)).append(rightEnd);
            else
                sbuilder.append("\n").append(defaultLeft).append(choices.get(i)).append(defaultRight);
        MessageBuilder mbuilder = new MessageBuilder();
        String content = text.apply(selection);
        if(content!=null)
            mbuilder.append(content);
        return mbuilder.setEmbed(new EmbedBuilder()
                .setColor(color.apply(selection))
                .setDescription(sbuilder.toString())
                .build()).build();
    }
}
