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
package com.jagrosh.jdautilities.menu.pagination;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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
public class PaginatorBuilder extends MenuBuilder<PaginatorBuilder, Paginator> {
    
    private BiFunction<Integer,Integer,Color> color = (page, pages) -> null;
    private BiFunction<Integer,Integer,String> text = (page, pages) -> null;
    private Consumer<Message> finalAction = m -> m.delete().queue();
    private int columns = 1;
    private int itemsPerPage = 12;
    private boolean showPageNumbers = true;
    private boolean numberItems = false;
    private boolean waitOnSinglePage = false;
    
    private final List<String> strings = new LinkedList<>();
    
    @Override
    public Paginator build()
    {
        if(waiter==null)
            throw new IllegalArgumentException("Must set an EventWaiter");
        if(strings.isEmpty())
            throw new IllegalArgumentException("Must include at least one item to paginate");
        return new Paginator(waiter, users, roles, timeout, unit, color, text, finalAction, 
                columns, itemsPerPage, showPageNumbers, numberItems, strings, waitOnSinglePage);
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
    public PaginatorBuilder setColor(Color color)
    {
        this.color = (i0, i1) -> color;
        return this;
    }
    
    /**
     * Sets the {@link java.awt.Color Color} of the {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}, 
     * relative to the total page number and the current page as determined by the provided
     * {@link java.util.function.BiFunction BiFunction}.
     * <br>As the page changes, the BiFunction will re-process the current page number and the total
     * page number, allowing for the color of the embed to change depending on the page number.
     * 
     * @param  colorBiFunction
     *         A BiFunction that uses both current and total page numbers to get a Color for the MessageEmbed
     * 
     * @return This builder
     */
    public PaginatorBuilder setColor(BiFunction<Integer,Integer,Color> colorBiFunction)
    {
        this.color = colorBiFunction;
        return this;
    }
    
    /**
     * Sets the text of the {@link net.dv8tion.jda.core.entities.Message Message} to be displayed
     * when the {@link com.jagrosh.jdautilities.menu.pagination.Paginator Paginator} is built.
     * 
     * <p>This is displayed directly above the embed.
     * 
     * @param  text
     *         The Message content to be displayed above the embed when the Paginator is built
     *         
     * @return This builder
     */
    public PaginatorBuilder setText(String text)
    {
        this.text = (i0, i1) -> text;
        return this;
    }
    
    /**
     * Sets the text of the {@link net.dv8tion.jda.core.entities.Message Message} to be displayed
     * relative to the total page number and the current page as determined by the provided
     * {@link java.util.function.BiFunction BiFunction}.
     * <br>As the page changes, the BiFunction will re-process the current page number and the total
     * page number, allowing for the displayed text of the Message to change depending on the page number.
     * 
     * @param  textBiFunction
     *         The BiFunction that uses both current and total page numbers to get text for the Message
     *         
     * @return This builder
     */
    public PaginatorBuilder setText(BiFunction<Integer,Integer,String> textBiFunction)
    {
        this.text = textBiFunction;
        return this;
    }
    
    /**
     * Sets the {@link java.util.function.Consumer Consumer} to perform if the 
     * {@link com.jagrosh.jdautilities.menu.pagination.Paginator Paginator} times out.
     * 
     * @param  finalAction
     *         The Consumer action to perform if the Paginator times out
     *         
     * @return This builder
     */
    public PaginatorBuilder setFinalAction(Consumer<Message> finalAction)
    {
        this.finalAction = finalAction;
        return this;
    }
    
    /**
     * Sets the number of columns each page will have.
     * <br>By default this is 1.
     * 
     * @param  columns
     *         The number of columns
     *         
     * @return This builder
     */
    public PaginatorBuilder setColumns(int columns)
    {
        if(columns<1 || columns>3)
            throw new IllegalArgumentException("Only 1, 2, or 3 columns are supported");
        this.columns = columns;
        return this;
    }
    
    /**
     * Sets the number of items that will appear on each page.
     * 
     * @param  num
     *         Always positive, never-zero number of items per page
     *         
     * @throws java.lang.IllegalArgumentException 
     *         If the provided number is less than 1
     *         
     * @return This builder
     */
    public PaginatorBuilder setItemsPerPage(int num)
    {
        if(num<1)
            throw new IllegalArgumentException("There must be at least one item per page");
        this.itemsPerPage = num;
        return this;
    }
    
    /**
     * Sets whether or not the page number will be shown.
     * 
     * @param  show
     *         {@code true} if the page number should be shown, {@code false} if it should not
     *         
     * @return This builder
     */
    public PaginatorBuilder showPageNumbers(boolean show)
    {
        this.showPageNumbers = show;
        return this;
    }
    
    /**
     * Sets whether or not the items will be automatically numbered.
     * 
     * @param  number
     *         {@code true} if the items should be numbered, {@code false} if it should not
     * 
     * @return This builder
     */
    public PaginatorBuilder useNumberedItems(boolean number)
    {
        this.numberItems = number;
        return this;
    }
    
    /**
     * Sets whether the {@link com.jagrosh.jdautilities.menu.pagination.Paginator Paginator} will instantly
     * timeout, and possibly run a provided {@link java.lang.Runnable Runnable}, if only a single slide is available to display.
     * 
     * @param  wait
     *         {@code true} if the Paginator will still generate
     * 
     * @return This builder
     */
    public PaginatorBuilder waitOnSinglePage(boolean wait)
    {
        this.waitOnSinglePage = wait;
        return this;
    }

    /**
     * Clears the list of String items to paginate.
     * 
     * @return This builder
     */
    public PaginatorBuilder clearItems()
    {
        strings.clear();
        return this;
    }
    
    /**
     * Adds String items to the list of items to paginate.
     * 
     * @param  items
     *         The String list of items to add
     *         
     * @return This builder
     */
    public PaginatorBuilder addItems(String... items)
    {
        strings.addAll(Arrays.asList(items));
        return this;
    }
    
    /**
     * Sets the String list of items to paginate.
     * <br>This method clears all previously set items before setting.
     * 
     * @param  items
     *         The String list of items to paginate
     *         
     * @return This builder
     */
    public PaginatorBuilder setItems(String... items)
    {
        strings.clear();
        strings.addAll(Arrays.asList(items));
        return this;
    }
}
