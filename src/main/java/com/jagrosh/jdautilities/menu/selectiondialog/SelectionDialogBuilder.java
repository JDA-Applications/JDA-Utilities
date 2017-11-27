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
package com.jagrosh.jdautilities.menu.selectiondialog;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import com.jagrosh.jdautilities.menu.MenuBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 * Scheduled for reallocation in 2.0
 *
 * <p>Full information on these and other 2.0 deprecations and changes can be found
 * <a href="https://gist.github.com/TheMonitorLizard/4f09ac2a3c9d8019dc3cde02cc456eee">here</a>
 *
 * @author John Grosh
 */
public class SelectionDialogBuilder extends MenuBuilder<SelectionDialogBuilder, SelectionDialog> {

    private final List<String> choices = new LinkedList<>();
    private String leftEnd = "";
    private String rightEnd  = "";
    private String defaultLeft = "";
    private String defaultRight = "";
    private Function<Integer,Color> color = i -> null;
    private boolean loop = true;
    private Function<Integer,String> text = i -> null;
    private BiConsumer<Message, Integer> selection;
    private Consumer<Message> cancel = (m) -> {};
    
    @Override
    public SelectionDialog build() {
        if(waiter==null)
            throw new IllegalArgumentException("Must set an EventWaiter");
        if(choices.isEmpty())
            throw new IllegalArgumentException("Must have at least one choice");
        if(selection == null)
            throw new IllegalArgumentException("Must provide a selection consumer");
        return new SelectionDialog(waiter,users,roles,timeout,unit,choices,leftEnd,rightEnd,
                defaultLeft,defaultRight,color,loop,selection,cancel,text);
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
    public SelectionDialogBuilder setColor(Color color) {
        this.color = i -> color;
        return this;
    }
    
    /**
     * Sets the {@link java.awt.Color Color} of the {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}, 
     * relative to the current selection number as determined by the provided
     * {@link java.util.function.Function Function}.
     * <br>As the selection changes, the Function will re-process the current selection number, 
     * allowing for the color of the embed to change depending on the selection number.
     * 
     * @param  color
     *         A Function that uses current selection number to get a Color for the MessageEmbed
     * 
     * @return This builder
     */
    public SelectionDialogBuilder setColor(Function<Integer,Color> color) {
        this.color = color;
        return this;
    }
    
    /**
     * Sets the text of the {@link net.dv8tion.jda.core.entities.Message Message} to be displayed
     * when the {@link com.jagrosh.jdautilities.menu.selectiondialog.SelectionDialog SelectionDialog} is built.
     * 
     * <p>This is displayed directly above the embed.
     * 
     * @param  text
     *         The Message content to be displayed above the embed when the SelectionDialog is built
     *         
     * @return This builder
     */
    public SelectionDialogBuilder setText(String text) {
        this.text = i -> text;
        return this;
    }
    
    /**
     * Sets the text of the {@link net.dv8tion.jda.core.entities.Message Message} to be displayed
     * relative to the current selection number as determined by the provided
     * {@link java.util.function.Function Function}.
     * <br>As the selection changes, the Function will re-process the current selection number, 
     * allowing for the displayed text of the Message to change depending on the selection number.
     * 
     * @param  text
     *         A Function that uses current selection number to get a text for the Message
     * 
     * @return This builder
     */
    public SelectionDialogBuilder setText(Function<Integer,String> text) {
        this.text = text;
        return this;
    }
    
    /**
     * Sets the text to use on either end of the selected item.
     * <br>Usage is primarily to mark which item is currently selected.
     * 
     * @param  left
     *         The left selection end
     * @param  right
     *         The right selection end
     *         
     * @return This builder
     */
    public SelectionDialogBuilder setSelectedEnds(String left, String right) {
        this.leftEnd = left;
        this.rightEnd = right;
        return this;
    }
    
    /**
     * Sets the text to use on either side of all unselected items. This will not
     * be applied to the selected item.
     * <br>Usage is primarily to mark which items are not currently selected.
     * 
     * @param  left
     *         The left non-selection end
     * @param  right
     *         The right non-selection end
     *         
     * @return This builder
     */
    public SelectionDialogBuilder setDefaultEnds(String left, String right) {
        this.defaultLeft = left;
        this.defaultRight = right;
        return this;
    }
    
    /**
     * Sets if moving up when at the top selection jumps to the bottom, and visa-versa.
     * 
     * @param  loop
     *         {@code true} if pressing up while at the top selection should loop
     *         to the bottom, {@code false} if it should not
     *         
     * @return This builder
     */
    public SelectionDialogBuilder useLooping(boolean loop) {
        this.loop = loop;
        return this;
    }
    
    /**
     * Sets a {@link java.util.function.Consumer Consumer} action to perform once a selection is made.
     * <br>The {@link java.lang.Integer Integer} provided is that of the selection made by the user,
     * and selections are in order of addition, 1 being the first String choice.
     * 
     * @param  selection
     *         A Consumer for the selection. This is one-based indexing.
     *         
     * @return This builder
     *
     * @deprecated
     *         Replace with {@link SelectionDialogBuilder#setSelectionConsumer(BiConsumer)}
     */
    @Deprecated
    public SelectionDialogBuilder setSelectionConsumer(Consumer<Integer> selection)
    {
        this.selection = (m, i) -> selection.accept(i);
        return this;
    }


    /**
     * Sets a {@link java.util.function.BiConsumer BiConsumer} action to perform once a selection is made.
     * <br>The {@link net.dv8tion.jda.core.entities.Message Message} provided is the one used to display
     * the menu and the {@link java.lang.Integer Integer} is that of the selection made by the user,
     * and selections are in order of addition, 1 being the first String choice.
     *
     * @param  selection
     *         A Consumer for the selection. This is one-based indexing.
     *
     * @return This builder
     */
    public SelectionDialogBuilder setSelectionConsumer(BiConsumer<Message, Integer> selection)
    {
        this.selection = selection;
        return this;
    }
    
    /**
     * Sets a {@link java.lang.Runnable Runnable} action to take if the cancel button is used, or if
     * the SelectionDialog times out.
     * 
     * @param  cancel
     *         The action to take when the SelectionDialog is canceled or times out
     * 
     * @return This builder
     *
     * @deprecated
     *         Replace with {@link SelectionDialogBuilder#setCanceled(Consumer)}
     */
    @Deprecated
    public SelectionDialogBuilder setCanceledRunnable(Runnable cancel)
    {
        this.cancel = (m) -> cancel.run();
        return this;
    }

    /**
     * Sets a {@link java.util.function.Consumer Consumer} action to take if the menu is cancelled, either
     * via the cancel button being used, or if the SelectionDialog times out.
     *
     * @param  cancel
     *         The action to take when the SelectionDialog is cancelled
     *
     * @return This builder
     */
    public SelectionDialogBuilder setCanceled(Consumer<Message> cancel)
    {
        this.cancel = cancel;
        return this;
    }
    
    /**
     * Clears the choices to be shown.
     * 
     * @return This builder
     */
    public SelectionDialogBuilder clearChoices()
    {
        this.choices.clear();
        return this;
    }
    
    /**
     * Sets the String choices to be shown as selections.
     * 
     * @param  choices
     *         The String choices to show
     * @return the builder
     */
    public SelectionDialogBuilder setChoices(String... choices)
    {
        this.choices.clear();
        this.choices.addAll(Arrays.asList(choices));
        return this;
    }
    
    /**
     * Adds String choices to be shown as selections.
     * 
     * @param  choices
     *         The String choices to add
     *         
     * @return This builder
     */
    public SelectionDialogBuilder addChoices(String... choices)
    {
        this.choices.addAll(Arrays.asList(choices));
        return this;
    }
}
