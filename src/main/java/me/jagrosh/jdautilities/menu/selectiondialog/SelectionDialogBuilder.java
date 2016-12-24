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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import me.jagrosh.jdautilities.menu.MenuBuilder;

/**
 *
 * @author John Grosh
 */
public class SelectionDialogBuilder extends MenuBuilder {

    private final List<String> choices = new LinkedList<>();
    private String leftEnd = "";
    private String rightEnd  = "";
    private String defaultLeft = "";
    private String defaultRight = "";
    private Function<Integer,Color> color = i -> null;
    private boolean loop = true;
    private Function<Integer,String> text = i -> null;
    private Consumer<Integer> success;
    private Runnable cancel = () -> {};
    
    /**
     * Builds a Selection Dialog with the set attributes
     * @return a SelectionDialog
     */
    @Override
    public SelectionDialog build() {
        if(waiter==null)
            throw new IllegalArgumentException("Must set an EventWaiter");
        if(choices.isEmpty())
            throw new IllegalArgumentException("Must have at least one choice");
        if(success==null)
            throw new IllegalArgumentException("Must provide a selection consumer");
        return new SelectionDialog(waiter,users,roles,timeout,unit,choices,leftEnd,rightEnd,
                defaultLeft,defaultRight,color,loop,success,cancel,text);
    }

    /**
     * Sets the color of the selection embed
     * @param color the color of the selection embed
     * @return the builder
     */
    @Override
    public SelectionDialogBuilder setColor(Color color) {
        this.color = i -> color;
        return this;
    }
    
    /**
     * Sets the color as a function of the selection
     * @param color the color function
     * @return the builder
     */
    public SelectionDialogBuilder setColor(Function<Integer,Color> color) {
        this.color = color;
        return this;
    }
    
    /**
     * Sets the text above the embed
     * @param text the text
     * @return the builder
     */
    public SelectionDialogBuilder setText(String text) {
        this.text = i -> text;
        return this;
    }
    
    /**
     * Sets the text above the embed as a function of the current selection
     * @param text the text function
     * @return the builder
     */
    public SelectionDialogBuilder setText(Function<Integer,String> text) {
        this.text = text;
        return this;
    }
    
    /**
     * Sets the text to use on either end of the selected item
     * @param left the left end
     * @param right the right end
     * @return the builder
     */
    public SelectionDialogBuilder setSelectedEnds(String left, String right) {
        this.leftEnd = left;
        this.rightEnd = right;
        return this;
    }
    
    /**
     * Sets the text to use on either side of all unselected items. This will not
     * be applied to the selected item
     * @param left the left end
     * @param right the right end
     * @return the builder
     */
    public SelectionDialogBuilder setDefaultEnds(String left, String right) {
        this.defaultLeft = left;
        this.defaultRight = right;
        return this;
    }
    
    /**
     * Sets if moving up when at the top selection jumps to the bottom
     * @param loop true if pressing up while at the top selection should loop
     * to the bottom
     * @return the builder
     */
    public SelectionDialogBuilder useLooping(boolean loop) {
        this.loop = loop;
        return this;
    }
    
    /**
     * Sets the action to perform once a selection is made
     * @param selection a consumer of the selection. This is one-based indexing
     * @return the builder
     */
    public SelectionDialogBuilder setSelectionConsumer(Consumer<Integer> selection)
    {
        this.success = selection;
        return this;
    }
    
    /**
     * Sets the action to take if the cancel button is used, or if the dialog
     * times out
     * @param cancel the action to take when the dialog is canceled or timed out
     * @return the builder
     */
    public SelectionDialogBuilder setCanceledRunnable(Runnable cancel)
    {
        this.cancel = cancel;
        return this;
    }
    
    /**
     * Sets the choices to be shown as selections
     * @param choices the choices to show
     * @return the builder
     */
    public SelectionDialogBuilder setChoices(String... choices)
    {
        this.choices.clear();
        this.choices.addAll(Arrays.asList(choices));
        return this;
    }
    
    /**
     * Adds choices to be shown as selections
     * @param choices the choices to add
     * @return the builder
     */
    public SelectionDialogBuilder addChoices(String... choices)
    {
        this.choices.addAll(Arrays.asList(choices));
        return this;
    }
}
