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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import me.jagrosh.jdautilities.menu.MenuBuilder;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.MessageReaction.ReactionEmote;

/**
 *
 * @author John Grosh
 */
public class ButtonMenuBuilder extends MenuBuilder {

    private Color color;
    private String text;
    private String description;
    private final List<String> choices = new LinkedList<>();
    private Consumer<ReactionEmote> action;
    private Runnable cancel = () -> {};
    
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
        return new ButtonMenu(waiter,users,roles,timeout,unit,color,text,description,choices,action,cancel);
    }

    /**
     * Sets the color of the embed, if description is set
     * @param color the color
     * @return the builder
     */
    @Override
    public ButtonMenuBuilder setColor(Color color) {
        this.color = color;
        return this;
    }
    
    /**
     * Sets the text (message content)
     * @param text the message content
     * @return the builder
     */
    public ButtonMenuBuilder setText(String text) {
        this.text = text;
        return this;
    }
    
    /**
     * Sets the description to be placed in an embed. If this is null, no
     * embed will be shown
     * @param description the content of the embed
     * @return the builder
     */
    public ButtonMenuBuilder setDescription(String description) {
        this.description = description;
        return this;
    }
    
    /**
     * Sets the action to perform with the menu result
     * @param action the action to perform
     * @return the builder
     */
    public ButtonMenuBuilder setAction(Consumer<ReactionEmote> action) {
        this.action = action;
        return this;
    }
    
    /**
     * Sets the action to perform if the menu times out
     * @param cancel the action to perform
     * @return the builder
     */
    public ButtonMenuBuilder setCancel(Runnable cancel) {
        this.cancel = cancel;
        return this;
    }
    
    /**
     * Adds unicode emojis as button choices
     * @param emojis the unicode strings to add
     * @return the builder
     */
    public ButtonMenuBuilder addChoices(String... emojis) {
        this.choices.addAll(Arrays.asList(emojis));
        return this;
    }
    
    /**
     * Adds custom emotes as button choices
     * @param emotes the Emote objects to add
     * @return the builder
     */
    public ButtonMenuBuilder addChoices(Emote... emotes) {
        Arrays.asList(emotes).stream().map(e -> e.getId()).forEach(e -> this.choices.add(e));
        return this;
    }
    
    /**
     * Sets the choices to a list of unicode emojis
     * @param emojis the unicode strings
     * @return the builder
     */
    public ButtonMenuBuilder setChoices(String... emojis) {
        this.choices.clear();
        this.choices.addAll(Arrays.asList(emojis));
        return this;
    }
    
    /**
     * Sets the choices to a list of Emotes
     * @param emotes the Emote objects to add
     * @return the builder
     */
    public ButtonMenuBuilder setChoices(Emote... emotes) {
        this.choices.clear();
        Arrays.asList(emotes).stream().map(e -> e.getId()).forEach(e -> this.choices.add(e));
        return this;
    }
}
