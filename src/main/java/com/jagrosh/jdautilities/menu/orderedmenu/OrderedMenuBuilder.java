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
public class OrderedMenuBuilder extends MenuBuilder {

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
     * Sets the color of the embed
     * @param color the color
     * @return the builder
     */
    @Override
    public OrderedMenuBuilder setColor(Color color) {
        this.color = color;
        return this;
    }
    
    /**
     * Uses letters for ordering and reactions
     * @return the builder
     */
    public OrderedMenuBuilder useLetters() {
        this.useLetters = true;
        return this;
    }
    
    /**
     * Uses numbers for ordering and reactions
     * @return the builder
     */
    public OrderedMenuBuilder useNumbers() {
        this.useLetters = false;
        return this;
    }
    
    /**
     * If true, users can type the number or letter of the input to make
     * their selection, in addition to the reaction option.
     * @param allow if text input should be allowed
     * @return the builder
     */
    public OrderedMenuBuilder allowTextInput(boolean allow) {
        this.allowTypedInput = allow;
        return this;
    }
    
    /**
     * If true, adds a cancel button that performs the timeout action
     * when selected
     * @param use if the cancel button should be shown
     * @return the builder
     */
    public OrderedMenuBuilder useCancelButton(boolean use) {
        this.addCancel = use;
        return this;
    }
    
    /**
     * Sets the text (message content)
     * @param text the message content
     * @return the builder
     */
    public OrderedMenuBuilder setText(String text) {
        this.text = text;
        return this;
    }
    
    /**
     * Sets the description to be placed in the embed, above the choices
     * @param description the content of the embed above the choices
     * @return the builder
     */
    public OrderedMenuBuilder setDescription(String description) {
        this.description = description;
        return this;
    }
    
    /**
     * Sets the action to perform with the menu result
     * @param action the action to perform
     * @return the builder
     */
    public OrderedMenuBuilder setAction(Consumer<Integer> action) {
        this.action = action;
        return this;
    }
    
    /**
     * Sets the action to perform if the menu times out
     * @param cancel the action to perform
     * @return the builder
     */
    public OrderedMenuBuilder setCancel(Runnable cancel) {
        this.cancel = cancel;
        return this;
    }
    
    /**
     * Adds choices to the ordered menu
     * @param choices the choices to add
     * @return the builder
     */
    public OrderedMenuBuilder addChoices(String... choices) {
        this.choices.addAll(Arrays.asList(choices));
        return this;
    }
    
    /**
     * Sets the choices in the menu
     * @param choices the choices to use in the menu
     * @return the builder
     */
    public OrderedMenuBuilder setChoices(String... choices) {
        this.choices.clear();
        this.choices.addAll(Arrays.asList(choices));
        return this;
    }
}
