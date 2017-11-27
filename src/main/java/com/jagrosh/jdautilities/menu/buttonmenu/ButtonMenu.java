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
package com.jagrosh.jdautilities.menu.buttonmenu;

import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import com.jagrosh.jdautilities.menu.Menu;
import com.jagrosh.jdautilities.waiter.EventWaiter;
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
 * Scheduled for reallocation in 2.0
 *
 * <p>Full information on these and other 2.0 deprecations and changes can be found
 * <a href="https://gist.github.com/TheMonitorLizard/4f09ac2a3c9d8019dc3cde02cc456eee">here</a>
 *
 * @author John Grosh
 */
public class ButtonMenu extends Menu {
    private final Color color;
    private final String text;
    private final String description;
    private final List<String> choices;
    private final Consumer<ReactionEmote> action;
    private final Consumer<Message> finalAction;
    
    protected ButtonMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
            Color color, String text, String description, List<String> choices, Consumer<ReactionEmote> action, Consumer<Message> finalAction)
    {
        super(waiter, users, roles, timeout, unit);
        this.color = color;
        this.text = text;
        this.description = description;
        this.choices = choices;
        this.action = action;
        this.finalAction = finalAction;
    }

    /**
     * Shows the ButtonMenu as a new {@link net.dv8tion.jda.core.entities.Message Message} 
     * in the provided {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * 
     * @param  channel
     *         The MessageChannel to send the new Message to
     */
    @Override
    public void display(MessageChannel channel) {
        initialize(channel.sendMessage(getMessage()));
    }

    /**
     * Displays this ButtonMenu by editing the provided {@link net.dv8tion.jda.core.entities.Message Message}.
     * 
     * @param  message
     *         The Message to display the Menu in
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
                Emote emote;
                try {
                    emote = m.getJDA().getEmoteById(choices.get(i));
                } catch(Exception e) {
                    emote = null;
                }
                RestAction<Void> r = emote==null ? m.addReaction(choices.get(i)) : m.addReaction(emote);
                if(i+1<choices.size())
                    r.queue();
                else
                    r.queue(v -> {
                        waiter.waitForEvent(MessageReactionAddEvent.class, event -> {
                            if(!event.getMessageId().equals(m.getId()))
                                return false;
                            String re = event.getReactionEmote().isEmote()
                                    ? event.getReactionEmote().getId()
                                    : event.getReactionEmote().getName();
                            if(!choices.contains(re))
                                return false;
                            return isValidUser(event.getUser(), event.getGuild());
                        }, (MessageReactionAddEvent event) -> {
                            m.delete().queue();
                            action.accept(event.getReactionEmote());
                        }, timeout, unit, () -> finalAction.accept(m));
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
