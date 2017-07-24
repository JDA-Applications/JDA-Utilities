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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import com.jagrosh.jdautilities.menu.MenuBuilder;

/**
 *
 * @author John Grosh
 */
public class OrderedMenuBuilder extends MenuBuilder<OrderedMenuBuilder, OrderedMenu> {

    private Color color;
    private String text;
    private String description;
    private final List<String> choices = new LinkedList<>();
    private Consumer<Integer> action;
    private Runnable cancel = () -> {};
    private boolean useLetters = false;
    private boolean allowTypedInput = true;
    private boolean addCancel = false;
    
    @Override
    public OrderedMenu build() {
        if(waiter==null)
            throw new IllegalArgumentException("Must set an EventWaiter");
        if(choices.isEmpty())
            throw new IllegalArgumentException("Must have at least one choice");
        if(choices.size()>10)
            throw new IllegalArgumentException("Must have no more than ten choices");
        if(action==null)
            throw new IllegalArgumentException("Must provide an action consumer");
        if(text==null && description==null)
            throw new IllegalArgumentException("Either text or description must be set");
        return new OrderedMenu(waiter,users,roles,timeout,unit,color,text,description,choices,
                action,cancel,useLetters,allowTypedInput,addCancel);
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
    public OrderedMenuBuilder setColor(Color color) {
        this.color = color;
        return this;
    }
    
    /**
     * Sets the builder to build an {@link com.jagrosh.jdautilities.menu.orderedmenu.OrderedMenu OrderedMenu} 
     * using letters for ordering and reactions (IE: A, B, C, etc.).
     * <br>As a note - by default the builder will use <b>numbers</b> not letters.
     * 
     * @return This builder
     */
    public OrderedMenuBuilder useLetters() {
        this.useLetters = true;
        return this;
    }
    
    /**
     * Sets the builder to build an {@link com.jagrosh.jdautilities.menu.orderedmenu.OrderedMenu OrderedMenu}
     * using numbers for ordering and reactions (IE: A, B, C, etc.).
     * 
     * @return This builder
     */
    public OrderedMenuBuilder useNumbers() {
        this.useLetters = false;
        return this;
    }
    
    /**
     * If {@code true}, {@link net.dv8tion.jda.core.entities.User User}s can type the number or 
     * letter of the input to make their selection, in addition to the reaction option.
     * 
     * @param  allow
     *         {@code true} if raw text input is allowed, {@code false} if it is not
     *         
     * @return This builder
     */
    public OrderedMenuBuilder allowTextInput(boolean allow) {
        this.allowTypedInput = allow;
        return this;
    }
    
    /**
     * If {@code true}, adds a cancel button that performs the timeout action when selected.
     * 
     * @param  use
     *         {@code true} if the cancel button should be shown, {@code false} if it should not
     *         
     * @return This builder
     */
    public OrderedMenuBuilder useCancelButton(boolean use) {
        this.addCancel = use;
        return this;
    }
    
    /**
     * Sets the text of the {@link net.dv8tion.jda.core.entities.Message Message} to be displayed
     * when the {@link com.jagrosh.jdautilities.menu.orderedmenu.OrderedMenu OrderedMenu} is built.
     * 
     * <p>This is displayed directly above the embed.
     * 
     * @param  text
     *         The Message content to be displayed above the embed when the OrderedMenu is built
     *         
     * @return This builder
     */
    public OrderedMenuBuilder setText(String text) {
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
    public OrderedMenuBuilder setDescription(String description) {
        this.description = description;
        return this;
    }
    
    /**
     * Sets the {@link java.util.function.Consumer Consumer} action to perform upon selecting a option.
     * 
     * @param  action
     *         The Consumer action to perform upon selecting a button
     * 
     * @return This builder
     */
    public OrderedMenuBuilder setAction(Consumer<Integer> action) {
        this.action = action;
        return this;
    }
    
    /**
     * Sets the {@link java.lang.Runnable Runnable} to perform if the 
     * {@link com.jagrosh.jdautilities.menu.orderedmenu.OrderedMenu OrderedMenu} times out.
     * 
     * @param  cancel
     *         The Runnable action to perform if the ButtonMenu times out
     *         
     * @return This builder
     */
    public OrderedMenuBuilder setCancel(Runnable cancel) {
        this.cancel = cancel;
        return this;
    }
    
    /**
     * Adds the String choices.
     * <br>These correspond to the button in order of addition.
     * 
     * @param  choices
     *         The String choices to add
     *         
     * @return This builder
     */
    public OrderedMenuBuilder addChoices(String... choices) {
        this.choices.addAll(Arrays.asList(choices));
        return this;
    }
    
    /**
     * Sets the String choices.
     * <br>These correspond to the button in the order they are set.
     * 
     * @param  choices
     *         The String choices to set
     *         
     * @return This builder
     */
    public OrderedMenuBuilder setChoices(String... choices) {
        this.choices.clear();
        this.choices.addAll(Arrays.asList(choices));
        return this;
    }
}
