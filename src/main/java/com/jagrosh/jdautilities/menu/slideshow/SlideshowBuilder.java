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
package com.jagrosh.jdautilities.menu.slideshow;

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
public class SlideshowBuilder extends MenuBuilder<SlideshowBuilder, Slideshow> {
    
    private BiFunction<Integer,Integer,Color> color = (page, pages) -> null;
    private BiFunction<Integer,Integer,String> text = (page, pages) -> null;
    private BiFunction<Integer,Integer,String> description = (page, pages) -> null;
    private Consumer<Message> finalAction = m -> m.delete().queue();
    private boolean showPageNumbers = true;
    private boolean waitOnSinglePage = false;
    
    private final List<String> strings = new LinkedList<>();
    
    @Override
    public Slideshow build()
    {
        if(waiter==null)
            throw new IllegalArgumentException("Must set an EventWaiter");
        if(strings.isEmpty())
            throw new IllegalArgumentException("Must include at least one item to paginate");
        return new Slideshow(waiter, users, roles, timeout, unit, color, text, description, finalAction, 
                showPageNumbers, strings, waitOnSinglePage);
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
    public SlideshowBuilder setColor(Color color)
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
    public SlideshowBuilder setColor(BiFunction<Integer,Integer,Color> colorBiFunction)
    {
        this.color = colorBiFunction;
        return this;
    }
    
    /**
     * Sets the text of the {@link net.dv8tion.jda.core.entities.Message Message} to be displayed
     * when the {@link com.jagrosh.jdautilities.menu.slideshow.Slideshow Slideshow} is built.
     * 
     * <p>This is displayed directly above the embed.
     * 
     * @param  text
     *         The Message content to be displayed above the embed when the Slideshow is built
     *         
     * @return This builder
     */
    public SlideshowBuilder setText(String text)
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
    public SlideshowBuilder setText(BiFunction<Integer,Integer,String> textBiFunction)
    {
        this.text = textBiFunction;
        return this;
    }
    
    /**
     * Sets the description of the {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}
     * in the {@link net.dv8tion.jda.core.entities.Message Message} to be displayed when the 
     * {@link com.jagrosh.jdautilities.menu.slideshow.Slideshow Slideshow} is built.
     * 
     * @param  description
     *         The description of the MessageEmbed
     *         
     * @return This builder
     */
    public SlideshowBuilder setDescription(String description)
    {
        this.description = (i0, i1) -> description;
        return this;
    }
    
    /**
     * Sets the description of the {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}
     * in the {@link net.dv8tion.jda.core.entities.Message Message} to be displayed relative to the
     * total page number and the current page as determined by the provided {@link java.util.function.BiFunction BiFunction}.
     * <br>As the page changes, the BiFunction will re-process the current page number and the total
     * page number, allowing for the displayed description of the MessageEmbed to change depending on the page number.
     * 
     * @param  descriptionBiFunction
     *         The BiFunction that uses both current and total page numbers to get description for the MessageEmbed
     *         
     * @return This builder
     */
    public SlideshowBuilder setDescription(BiFunction<Integer,Integer,String> descriptionBiFunction)
    {
        this.description = descriptionBiFunction;
        return this;
    }
    
    /**
     * Sets the {@link java.util.function.Consumer Consumer} to perform if the 
     * {@link com.jagrosh.jdautilities.menu.slideshow.Slideshow Slideshow} times out.
     * 
     * @param  finalAction
     *         The Consumer action to perform if the Slideshow times out
     *         
     * @return This builder
     */
    public SlideshowBuilder setFinalAction(Consumer<Message> finalAction)
    {
        this.finalAction = finalAction;
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
    public SlideshowBuilder showPageNumbers(boolean show)
    {
        this.showPageNumbers = show;
        return this;
    }
    
    /**
     * Sets whether the {@link com.jagrosh.jdautilities.menu.slideshow.Slideshow Slideshow} will instantly
     * timeout, and possibly run a provided {@link java.lang.Runnable Runnable}, if only a single slide is available to display.
     * 
     * @param  wait
     *         {@code true} if the Slideshow will still generate
     *         
     * @return This builder
     */
    public SlideshowBuilder waitOnSinglePage(boolean wait)
    {
        this.waitOnSinglePage = wait;
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
    public SlideshowBuilder addItems(String... items)
    {
        strings.addAll(Arrays.asList(items));
        return this;
    }
    
    /**
     * Sets the String list of urls to paginate.
     * <br>This method clears all previously set items before setting.
     * 
     * @param  items
     *         The String list of urls to paginate
     *         
     * @return This builder
     */
    public SlideshowBuilder setUrls(String... items)
    {
        strings.clear();
        strings.addAll(Arrays.asList(items));
        return this;
    }
}
