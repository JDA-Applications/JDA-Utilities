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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import com.jagrosh.jdautilities.menu.MenuBuilder;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction.ReactionEmote;

/**
 * Scheduled for reallocation in 2.0
 *
 * <p>Full information on these and other 2.0 deprecations and changes can be found
 * <a href="https://gist.github.com/TheMonitorLizard/4f09ac2a3c9d8019dc3cde02cc456eee">here</a>
 *
 * @author John Grosh
 */
public class ButtonMenuBuilder extends MenuBuilder<ButtonMenuBuilder, ButtonMenu> {

    private Color color;
    private String text;
    private String description;
    private final List<String> choices = new LinkedList<>();
    private Consumer<ReactionEmote> action;
    private Consumer<Message> finalAction = (m) -> {};
    
    @Override
    public ButtonMenu build() {
        if(waiter==null)
            throw new IllegalArgumentException("Must set an EventWaiter");
        if(choices.isEmpty())
            throw new IllegalArgumentException("Must have at least one choice");
        if(action==null)
            throw new IllegalArgumentException("Must provide an action consumer");
        if(text==null && description==null)
            throw new IllegalArgumentException("Either text or description must be set");
        return new ButtonMenu(waiter, users, roles, timeout, unit, color, text, description, choices, action, finalAction);
    }

    /**
     * Sets the {@link java.awt.Color Color} of the {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}, 
     * if description of the MessageEmbed is set.
     * 
     * @param  color
     *         The Color of the MessageEmbed
     *         
     * @return This builder
     */
    @Override
    public ButtonMenuBuilder setColor(Color color) {
        this.color = color;
        return this;
    }
    
    /**
     * Sets the text of the {@link net.dv8tion.jda.core.entities.Message Message} to be displayed
     * when the {@link com.jagrosh.jdautilities.menu.buttonmenu.ButtonMenu ButtonMenu} is built.
     * 
     * <p>This is displayed directly above the embed.
     * 
     * @param  text
     *         The Message content to be displayed above the embed when the ButtonMenu is built
     *         
     * @return This builder
     */
    public ButtonMenuBuilder setText(String text) {
        this.text = text;
        return this;
    }
    
    /**
     * Sets the description to be placed in an {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}. 
     * <br>If this is {@code null}, no MessageEmbed will be displayed
     * 
     * @param  description
     *         The content of the MessageEmbed's description
     *     
     * @return This builder
     */
    public ButtonMenuBuilder setDescription(String description) {
        this.description = description;
        return this;
    }
    
    /**
     * Sets the {@link java.util.function.Consumer Consumer} action to perform upon selecting a button.
     * 
     * @param  action
     *         The Consumer action to perform upon selecting a button
     * 
     * @return This builder
     */
    public ButtonMenuBuilder setAction(Consumer<ReactionEmote> action) {
        this.action = action;
        return this;
    }

    /**
     * Sets the {@link java.util.function.Consumer Consumer} to perform if the
     * {@link com.jagrosh.jdautilities.menu.buttonmenu.ButtonMenu ButtonMenu} is done,
     * either via cancellation, a timeout, or a selection being made.<p>
     *
     * This accepts the message used to display the menu when called.
     *
     * @param  finalAction
     *         The Runnable action to perform if the ButtonMenu is done
     *
     * @return This builder
     */
    public ButtonMenuBuilder setFinalAction(Consumer<Message> finalAction)
    {
        this.finalAction = finalAction;
        return this;
    }
    
    /**
     * Sets the {@link java.lang.Runnable Runnable} to perform if the 
     * {@link com.jagrosh.jdautilities.menu.buttonmenu.ButtonMenu ButtonMenu} times out.
     * 
     * @param  cancel
     *         The Runnable action to perform if the ButtonMenu times out
     *         
     * @return This builder
     *
     * @deprecated
     *         Replace with {@link ButtonMenuBuilder#setFinalAction(Consumer)}.
     */
    @Deprecated
    public ButtonMenuBuilder setCancel(Runnable cancel) {
        this.finalAction = (m) -> cancel.run();
        return this;
    }
    
    /**
     * Adds String unicode emojis as button choices.
     * 
     * <p>Any non-unicode {@link net.dv8tion.jda.core.entities.Emote Emote}s should be
     * added using {@link ButtonMenuBuilder#addChoices(Emote...)}.
     * 
     * @param  emojis
     *         The String unicode emojis to add
     *         
     * @return This builder
     */
    public ButtonMenuBuilder addChoices(String... emojis) {
        this.choices.addAll(Arrays.asList(emojis));
        return this;
    }
    
    /**
     * Adds custom {@link net.dv8tion.jda.core.entities.Emote Emote}s as button choices.
     * 
     * <p>Any regular unicode emojis should be added using {@link ButtonMenuBuilder#addChoices(String...)}.
     * 
     * @param  emotes
     *         The Emote objects to add
     *         
     * @return This builder
     */
    public ButtonMenuBuilder addChoices(Emote... emotes) {
        Arrays.asList(emotes).stream().map(e -> e.getId()).forEach(e -> this.choices.add(e));
        return this;
    }
    
    /**
     * Sets the String unicode emojis as button choices.
     * 
     * <p>Any non-unicode {@link net.dv8tion.jda.core.entities.Emote Emote}s should be
     * set using {@link ButtonMenuBuilder#setChoices(Emote...)}.
     * 
     * @param  emojis
     *         The String unicode emojis to set
     *         
     * @return This builder
     */
    public ButtonMenuBuilder setChoices(String... emojis) {
        this.choices.clear();
        this.choices.addAll(Arrays.asList(emojis));
        return this;
    }
    
    /**
     * Sets the {@link net.dv8tion.jda.core.entities.Emote Emote}s as button choices.
     * 
     * <p>Any regular unicode emojis should be set using {@link ButtonMenuBuilder#addChoices(String...)}.
     * 
     * @param  emotes
     *         The Emote objects to set
     *         
     * @return This builder
     */
    public ButtonMenuBuilder setChoices(Emote... emotes) {
        this.choices.clear();
        Arrays.asList(emotes).stream().map(e -> e.getId()).forEach(e -> this.choices.add(e));
        return this;
    }
}
