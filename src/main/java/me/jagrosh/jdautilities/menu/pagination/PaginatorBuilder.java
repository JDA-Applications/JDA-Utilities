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
package me.jagrosh.jdautilities.menu.pagination;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import me.jagrosh.jdautilities.menu.MenuBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author John Grosh
 */
public class PaginatorBuilder extends MenuBuilder {
    
    private BiFunction<Integer,Integer,Color> color = (page, pages) -> null;
    private BiFunction<Integer,Integer,String> text = (page, pages) -> null;
    private Consumer<Message> finalAction = m -> m.delete().queue();
    private int columns = 1;
    private int itemsPerPage = 12;
    private boolean showPageNumbers = true;
    private boolean numberItems = false;
    private boolean waitOnSinglePage = false;
    
    private final List<String> strings = new LinkedList<>();
    
    /**
     * Builds a paginator from the provided values
     * @return a built Paginator
     */
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
     * Sets the color of the embed, default is no color
     * @param color the color
     * @return the builder after the color has been set
     */
    @Override
    public PaginatorBuilder setColor(Color color)
    {
        this.color = (i0, i1) -> color;
        return this;
    }
    
    /**
     * Sets the color of the embed page to be a function of the current page number and
     * the total number of pages
     * @param colorBiFunction a function of the page number and total pages to a color
     * @return the builder after the colors have been set
     */
    public PaginatorBuilder setColor(BiFunction<Integer,Integer,Color> colorBiFunction)
    {
        this.color = colorBiFunction;
        return this;
    }
    
    /**
     * Sets the text to appear above the embed
     * @param text the text to appear above the embed
     * @return the builder after the text has been set
     */
    public PaginatorBuilder setText(String text)
    {
        this.text = (i0, i1) -> text;
        return this;
    }
    
    /**
     * Sets the text above the embed to be a function of the current page number and
     * the total number of pages
     * @param textBiFunction the bifunction to use to determine text
     * @return the builder after the text bifunction has been set
     */
    public PaginatorBuilder setText(BiFunction<Integer,Integer,String> textBiFunction)
    {
        this.text = textBiFunction;
        return this;
    }
    
    /**
     * Sets the final action to take, either when the paginator stop button is
     * pressed, or when it times out
     * @param finalAction the final action to take
     * @return the builder
     */
    public PaginatorBuilder setFinalAction(Consumer<Message> finalAction)
    {
        this.finalAction = finalAction;
        return this;
    }
    
    /**
     * Sets the number of columns each page will have, default 1
     * @param columns the number of columns
     * @return the builder after the number of columns have been set
     */
    public PaginatorBuilder setColumns(int columns)
    {
        if(columns<1 || columns>3)
            throw new IllegalArgumentException("Only 1, 2, or 3 columns are supported");
        this.columns = columns;
        return this;
    }
    
    /**
     * Sets the number of items that will appear on each page
     * @param num the number of items per page
     * @return the builder after the number is set
     */
    public PaginatorBuilder setItemsPerPage(int num)
    {
        if(num<1)
            throw new IllegalArgumentException("There must be at least one item per page");
        this.itemsPerPage = num;
        return this;
    }
    
    /**
     * Sets whether or not the page number will be shown
     * @param show true if the page number should be shown
     * @return the builder when the bool has been set
     */
    public PaginatorBuilder showPageNumbers(boolean show)
    {
        this.showPageNumbers = show;
        return this;
    }
    
    /**
     * Sets whether or not the items will be automatically numbered
     * @param number true if the items should be numbered
     * @return the builder after the bool has been set
     */
    public PaginatorBuilder useNumberedItems(boolean number)
    {
        this.numberItems = number;
        return this;
    }
    
    /**
     * Sets whether or not the paginator should wait for an input when
     * only one page is visible
     * @param wait if the paginator should wait
     * @return the builder
     */
    public PaginatorBuilder waitOnSinglePage(boolean wait)
    {
        this.waitOnSinglePage = wait;
        return this;
    }

    /**
     * Clears the list of items to paginate
     * @return the builder after the list is cleared
     */
    public PaginatorBuilder clearItems()
    {
        strings.clear();
        return this;
    }
    
    /**
     * Adds items to the list of items to paginate
     * @param items the list of items to add
     * @return the builder after the items have been added
     */
    public PaginatorBuilder addItems(String... items)
    {
        strings.addAll(Arrays.asList(items));
        return this;
    }
    
    /**
     * Sets the list of items to paginate
     * @param items the list of items to paginate
     * @return the builder after the items have been set
     */
    public PaginatorBuilder setItems(String... items)
    {
        strings.clear();
        strings.addAll(Arrays.asList(items));
        return this;
    }
}
