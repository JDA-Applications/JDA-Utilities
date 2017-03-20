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
package me.jagrosh.jdautilities.menu.buttonmenu;

import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import me.jagrosh.jdautilities.menu.Menu;
import me.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.requests.RestAction;

/**
 *
 * @author John Grosh
 */
public class ButtonMenu extends Menu {
    private final Color color;
    private final String text;
    private final String description;
    private final List<String> choices;
    private final Consumer<ReactionEmote> action;
    private final Runnable cancel;
    
    protected ButtonMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
            Color color, String text, String description, List<String> choices, Consumer<ReactionEmote> action, Runnable cancel)
    {
        super(waiter, users, roles, timeout, unit);
        this.color = color;
        this.text = text;
        this.description = description;
        this.choices = choices;
        this.action = action;
        this.cancel = cancel;
    }

    /**
     * Shows the button menu as a new message in the provided channel
     * @param channel the channel to send to the message to edit
     */
    @Override
    public void display(MessageChannel channel) {
        initialize(channel.sendMessage(getMessage()));
    }

    /**
     * Shows the button menu edited into the provided message
     * @param message the message to edit the menu into
     */
    @Override
    public void display(Message message) {
        initialize(message.editMessage(getMessage()));
    }
    
    private void initialize(RestAction<Message> ra)
    {
        ra.queue(m -> {
            for(int i=0; i<choices.size(); i++)
            {
                Emote emote = m.getJDA().getEmoteById(choices.get(i));
                RestAction<Void> r = emote==null ? m.addReaction(choices.get(i)) : m.addReaction(emote);
                if(i+1<choices.size())
                    r.queue();
                else
                    r.queue(v -> {
                        waiter.waitForEvent(MessageReactionAddEvent.class, event -> {
                            if(!event.getMessageId().equals(m.getId()))
                                return false;
                            String re = event.getReaction().getEmote().isEmote() 
                                    ? event.getReaction().getEmote().getId() 
                                    : event.getReaction().getEmote().getName();
                            if(!choices.contains(re))
                                return false;
                            return isValidUser(event);
                        }, (MessageReactionAddEvent event) -> {
                            m.delete().queue();
                            action.accept(event.getReaction().getEmote());
                        }, timeout, unit, cancel);
                    });
            }
        });
    }
    
    private Message getMessage()
    {
        MessageBuilder mbuilder = new MessageBuilder();
        if(text!=null)
            mbuilder.append(text);
        if(description!=null)
            mbuilder.setEmbed(new EmbedBuilder().setColor(color).setDescription(description).build());
        return mbuilder.build();
    }
    
}
