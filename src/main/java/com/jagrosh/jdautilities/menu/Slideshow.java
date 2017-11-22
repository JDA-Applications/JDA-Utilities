/*
 * Copyright 2016 John Grosh (jagrosh).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jdautilities.menu;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import com.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.RestAction;

/**
 *
 * @author John Grosh
 */
public class Slideshow extends Menu
{
    private final BiFunction<Integer,Integer,Color> color;
    private final BiFunction<Integer,Integer,String> text;
    private final BiFunction<Integer,Integer,String> description;
    private final boolean showPageNumbers;
    private final List<String> urls;
    private final Consumer<Message> finalAction;
    private final boolean waitOnSinglePage;
    
    public static final String LEFT = "\u25C0";
    public static final String STOP = "\u23F9";
    public static final String RIGHT = "\u25B6";
    
    Slideshow(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
            BiFunction<Integer,Integer,Color> color, BiFunction<Integer,Integer,String> text, BiFunction<Integer,Integer,String> description,
            Consumer<Message> finalAction, boolean showPageNumbers, List<String> items, boolean waitOnSinglePage)
    {
        super(waiter, users, roles, timeout, unit);
        this.color = color;
        this.text = text;
        this.description = description;
        this.showPageNumbers = showPageNumbers;
        this.urls = items;
        this.finalAction = finalAction;
        this.waitOnSinglePage = waitOnSinglePage;
    }

    /**
     * Begins pagination on page 1 as a new {@link net.dv8tion.jda.core.entities.Message Message} 
     * in the provided {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     * 
     * @param  channel
     *         The MessageChannel to send the new Message to
     */
    @Override
    public void display(MessageChannel channel)
    {
        paginate(channel, 1);
    }

    /**
     * Begins pagination on page 1 displaying this by editing the provided
     * {@link net.dv8tion.jda.core.entities.Message Message}.
     * 
     * @param  message
     *         The Message to display the Menu in
     */
    @Override
    public void display(Message message)
    {
        paginate(message, 1);
    }
    
    /**
     * Begins pagination as a new {@link net.dv8tion.jda.core.entities.Message Message} 
     * in the provided {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}, starting
     * on whatever page number is provided.
     * 
     * @param  channel
     *         The MessageChannel to send the new Message to
     * @param  pageNum
     *         The page number to begin on
     */
    public void paginate(MessageChannel channel, int pageNum)
    {
        if(pageNum<1)
            pageNum = 1;
        else if (pageNum>urls.size())
            pageNum = urls.size();
        Message msg = renderPage(pageNum);
        initialize(channel.sendMessage(msg), pageNum);
    }
    
    /**
     * Begins pagination displaying this by editing the provided
     * {@link net.dv8tion.jda.core.entities.Message Message}, starting on whatever
     * page number is provided.
     * 
     * @param  message
     *         The MessageChannel to send the new Message to
     * @param  pageNum
     *         The page number to begin on
     */
    public void paginate(Message message, int pageNum)
    {
        if(pageNum<1)
            pageNum = 1;
        else if (pageNum>urls.size())
            pageNum = urls.size();
        Message msg = renderPage(pageNum);
        initialize(message.editMessage(msg), pageNum);
    }
    
    private void initialize(RestAction<Message> action, int pageNum)
    {
        action.queue(m->{
            if(urls.size()>1)
            {
                m.addReaction(LEFT).queue();
                m.addReaction(STOP).queue();
                m.addReaction(RIGHT).queue(v -> pagination(m, pageNum), t -> pagination(m, pageNum));
            }
            else if(waitOnSinglePage)
            {
                m.addReaction(STOP).queue(v -> pagination(m, pageNum), t -> pagination(m, pageNum));
            }
            else
            {
                finalAction.accept(m);
            }
        });
    }
    
    private void pagination(Message message, int pageNum)
    {
        waiter.waitForEvent(MessageReactionAddEvent.class, (MessageReactionAddEvent event) -> {
            if(!event.getMessageId().equals(message.getId()))
                return false;
            if(!(LEFT.equals(event.getReaction().getEmote().getName()) 
                    || STOP.equals(event.getReaction().getEmote().getName())
                    || RIGHT.equals(event.getReaction().getEmote().getName())))
                return false;
            return isValidUser(event);
        }, event -> {
            int newPageNum = pageNum;
            switch(event.getReaction().getEmote().getName())
            {
                case LEFT:  if(newPageNum>1) newPageNum--; break;
                case RIGHT: if(newPageNum<urls.size()) newPageNum++; break;
                case STOP: finalAction.accept(message); return;
            }
            try{event.getReaction().removeReaction(event.getUser()).queue();}catch(PermissionException e){}
            int n = newPageNum;
            message.editMessage(renderPage(newPageNum)).queue(m -> {
                pagination(m, n);
            });
        }, timeout, unit, () -> finalAction.accept(message));
    }
    
    private Message renderPage(int pageNum)
    {
        MessageBuilder mbuilder = new MessageBuilder();
        EmbedBuilder ebuilder = new EmbedBuilder();
        ebuilder.setImage(urls.get(pageNum-1));
        ebuilder.setColor(color.apply(pageNum, urls.size()));
        ebuilder.setDescription(description.apply(pageNum, urls.size()));
        if(showPageNumbers)
            ebuilder.setFooter("Image "+pageNum+"/"+urls.size(), null);
        mbuilder.setEmbed(ebuilder.build());
        if(text!=null)
            mbuilder.append(text.apply(pageNum, urls.size()));
        return mbuilder.build();
    }

    /**
     * The {@link com.jagrosh.jdautilities.menu.Menu.Builder Menu.Builder} for
     * a {@link com.jagrosh.jdautilities.menu.Slideshow Slideshow}.
     *
     * @author John Grosh
     */
    public static class Builder extends Menu.Builder<Builder, Slideshow>
    {
        private BiFunction<Integer,Integer,Color> color = (page, pages) -> null;
        private BiFunction<Integer,Integer,String> text = (page, pages) -> null;
        private BiFunction<Integer,Integer,String> description = (page, pages) -> null;
        private Consumer<Message> finalAction = m -> m.delete().queue();
        private boolean showPageNumbers = true;
        private boolean waitOnSinglePage = false;

        private final List<String> strings = new LinkedList<>();

        /**
         * Builds the {@link com.jagrosh.jdautilities.menu.Slideshow Slideshow}
         * with this Builder.
         *
         * @return The Paginator built from this Builder.
         *
         * @throws java.lang.IllegalArgumentException
         *         If one of the following is violated:
         *         <ul>
         *             <li>No {@link com.jagrosh.jdautilities.waiter.EventWaiter EventWaiter} was set.</li>
         *             <li>No items were set to paginate.</li>
         *         </ul>
         */
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
         * Sets the {@link java.awt.Color Color} of the {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}.
         *
         * @param  color
         *         The Color of the MessageEmbed
         *
         * @return This builder
         */
        public Builder setColor(Color color)
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
        public Builder setColor(BiFunction<Integer,Integer,Color> colorBiFunction)
        {
            this.color = colorBiFunction;
            return this;
        }

        /**
         * Sets the text of the {@link net.dv8tion.jda.core.entities.Message Message} to be displayed
         * when the {@link com.jagrosh.jdautilities.menu.Slideshow Slideshow} is built.
         *
         * <p>This is displayed directly above the embed.
         *
         * @param  text
         *         The Message content to be displayed above the embed when the Slideshow is built
         *
         * @return This builder
         */
        public Builder setText(String text)
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
        public Builder setText(BiFunction<Integer,Integer,String> textBiFunction)
        {
            this.text = textBiFunction;
            return this;
        }

        /**
         * Sets the description of the {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}
         * in the {@link net.dv8tion.jda.core.entities.Message Message} to be displayed when the
         * {@link com.jagrosh.jdautilities.menu.Slideshow Slideshow} is built.
         *
         * @param  description
         *         The description of the MessageEmbed
         *
         * @return This builder
         */
        public Builder setDescription(String description)
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
        public Builder setDescription(BiFunction<Integer,Integer,String> descriptionBiFunction)
        {
            this.description = descriptionBiFunction;
            return this;
        }

        /**
         * Sets the {@link java.util.function.Consumer Consumer} to perform if the
         * {@link com.jagrosh.jdautilities.menu.Slideshow Slideshow} times out.
         *
         * @param  finalAction
         *         The Consumer action to perform if the Slideshow times out
         *
         * @return This builder
         */
        public Builder setFinalAction(Consumer<Message> finalAction)
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
        public Builder showPageNumbers(boolean show)
        {
            this.showPageNumbers = show;
            return this;
        }

        /**
         * Sets whether the {@link com.jagrosh.jdautilities.menu.Slideshow Slideshow} will instantly
         * timeout, and possibly run a provided {@link java.lang.Runnable Runnable}, if only a single slide is available to display.
         *
         * @param  wait
         *         {@code true} if the Slideshow will still generate
         *
         * @return This builder
         */
        public Builder waitOnSinglePage(boolean wait)
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
        public Builder addItems(String... items)
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
        public Builder setUrls(String... items)
        {
            strings.clear();
            strings.addAll(Arrays.asList(items));
            return this;
        }
    }

}
