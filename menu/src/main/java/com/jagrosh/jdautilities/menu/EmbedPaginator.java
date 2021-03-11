/*
 * Copyright 2016-2018 John Grosh (jagrosh) & Kaidan Gustave (TheMonitorLizard)
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

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * A {@link com.jagrosh.jdautilities.menu.Menu Menu} implementation, nearly identical to 
 * {@link com.jagrosh.jdautilities.menu.Paginator Paginator}, that displays an individual
 * {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} on each page instead of a list of text items.
 * 
 * <p>Like Paginator, reaction functions allow the user to traverse to the last page using the left arrow, the next
 * page using the right arrow, and to stop the EmbedPaginator prematurely using the stop reaction.
 * 
 * @author Andre_601
 */
public class EmbedPaginator extends Menu{
    
    private final BiFunction<Integer, Integer, String> text;
    private final Consumer<Message> finalAction;
    private final boolean waitOnSinglePage;
    private final List<MessageEmbed> embeds;
    private final int bulkSkipNumber;
    private final boolean wrapPageEnds;
    private final String leftText;
    private final String rightText;
    private final boolean allowTextInput;
    
    public static final String BIG_LEFT = "\u23EA";
    public static final String LEFT = "\u25C0";
    public static final String STOP = "\u23F9";
    public static final String RIGHT = "\u25B6";
    public static final String BIG_RIGHT = "\u23E9";
    
    protected EmbedPaginator(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
                             BiFunction<Integer, Integer, String> text, Consumer<Message> finalAction,
                             boolean waitOnSinglePage, List<MessageEmbed> embeds, int bulkSkipNumber,
                             boolean wrapPageEnds, String leftText, String rightText, boolean allowTextInput)
    {
        super(waiter, users, roles, timeout, unit);
        this.text = text;
        this.finalAction = finalAction;
        this.waitOnSinglePage = waitOnSinglePage;
        this.embeds = embeds;
        this.bulkSkipNumber = bulkSkipNumber;
        this.wrapPageEnds = wrapPageEnds;
        this.leftText = leftText;
        this.rightText = rightText;
        this.allowTextInput = allowTextInput;
    }
    
    /**
     * Begins pagination on page 1 as a new {@link net.dv8tion.jda.api.entities.Message Message}
     * in the provided {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}.
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
     * {@link net.dv8tion.jda.api.entities.Message Message}.
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
     * Begins pagination as a new {@link net.dv8tion.jda.api.entities.Message Message}
     * in the provided {@link net.dv8tion.jda.api.entities.MessageChannel MessageChannel}, starting
     * on whatever page number is provided.
     *
     * @param  channel
     *         The MessageChannel to send the new Message to
     * @param  pageNum
     *         The page number to begin on
     */
    public void paginate(MessageChannel channel, int pageNum)
    {
        if(pageNum < 1)
            pageNum = 1;
        else if(pageNum > embeds.size())
            pageNum = embeds.size();
        Message msg = renderPage(pageNum);
        initialize(channel.sendMessage(msg), pageNum);
    }
    
    /**
     * Begins pagination displaying this by editing the provided
     * {@link net.dv8tion.jda.api.entities.Message Message}, starting on whatever
     * page number is provided.
     *
     * @param  message
     *         The MessageChannel to send the new Message to
     * @param  pageNum
     *         The page number to begin on
     */
    public void paginate(Message message, int pageNum)
    {
        if(pageNum < 1)
            pageNum = 1;
        else if(pageNum > embeds.size())
            pageNum = embeds.size();
        Message msg = renderPage(pageNum);
        initialize(message.editMessage(msg), pageNum);
    }
    
    private void initialize(RestAction<Message> action, int pageNum)
    {
        action.queue(m -> {
            if(embeds.size()>1)
            {
                if(bulkSkipNumber > 1)
                    m.addReaction(BIG_LEFT).queue();
                m.addReaction(LEFT).queue();
                m.addReaction(STOP).queue();
                if(bulkSkipNumber > 1)
                    m.addReaction(RIGHT).queue();
                m.addReaction(bulkSkipNumber > 1 ? BIG_RIGHT : RIGHT)
                    .queue(v -> pagination(m, pageNum), t -> pagination(m, pageNum));
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
        if(allowTextInput || (leftText != null && rightText != null))
            paginationWithTextInput(message, pageNum);
        else
            paginationWithoutTextInput(message, pageNum);
    }
    
    private void paginationWithTextInput(Message message, int pageNum)
    {
        waiter.waitForEvent(GenericMessageEvent.class, event -> {
            if(event instanceof MessageReactionAddEvent)
                return checkReaction((MessageReactionAddEvent) event, message.getIdLong());
            else if(event instanceof MessageReceivedEvent)
            {
                MessageReceivedEvent mre = (MessageReceivedEvent) event;
                if(!mre.getChannel().equals(message.getChannel()))
                    return false;
                String rawContent = mre.getMessage().getContentRaw().trim();
                if(leftText != null && rightText != null)
                {
                    if(rawContent.equalsIgnoreCase(leftText) || rawContent.equalsIgnoreCase(rightText))
                        return isValidUser(mre.getAuthor(), mre.isFromGuild() ? mre.getGuild() : null);
                }
                
                if(allowTextInput)
                {
                    try {
                        int i = Integer.parseInt(rawContent);
                        
                        if(1 <= i && i <= embeds.size() && i != pageNum)
                            return isValidUser(mre.getAuthor(), mre.isFromGuild() ? mre.getGuild() : null);
                    } catch(NumberFormatException ignored) {}
                }
            }
            return false;
        }, event -> {
            if(event instanceof MessageReactionAddEvent)
            {
                handleMessageReactionAddAction((MessageReactionAddEvent)event, message, pageNum);
            }
            else
            {
                MessageReceivedEvent mre = (MessageReceivedEvent) event;
                String rawContent = mre.getMessage().getContentRaw().trim();
                
                int pages = embeds.size();
                final int targetPage;
                
                if(leftText != null && rawContent.equalsIgnoreCase(leftText) && (1 < pageNum || wrapPageEnds))
                    targetPage = pageNum - 1 < 1 && wrapPageEnds ? pages : pageNum - 1;
                else if(rightText != null && rawContent.equalsIgnoreCase(rightText) && (pageNum < pages || wrapPageEnds))
                    targetPage = pageNum + 1 > pages && wrapPageEnds ? 1 : pageNum + 1;
                else
                    targetPage = Integer.parseInt(rawContent);
                
                message.editMessage(renderPage(targetPage)).queue(m -> pagination(m, targetPage));
                mre.getMessage().delete().queue(v -> {}, t -> {});
            }
        }, timeout, unit, () -> finalAction.accept(message));
    }
    
    private void paginationWithoutTextInput(Message message, int pageNum)
    {
        waiter.waitForEvent(MessageReactionAddEvent.class,
            event -> checkReaction(event, message.getIdLong()),
            event -> handleMessageReactionAddAction(event, message, pageNum),
            timeout, unit, () -> finalAction.accept(message));
    }
    
    private boolean checkReaction(MessageReactionAddEvent event, long messageId)
    {
        if(event.getMessageIdLong() != messageId)
            return false;
        switch(event.getReactionEmote().getName())
        {
            case LEFT:
            case STOP:
            case RIGHT:
                return isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
            case BIG_LEFT:
            case BIG_RIGHT:
                return bulkSkipNumber > 1 && isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
            default:
                return false;
        }
    }
    
    private void handleMessageReactionAddAction(MessageReactionAddEvent event, Message message, int pageNum)
    {
        int newPageNum = pageNum;
        int pages = embeds.size();
        switch(event.getReaction().getReactionEmote().getName())
        {
            case LEFT:
                if(newPageNum == 1 && wrapPageEnds)
                    newPageNum = pages + 1;
                if(newPageNum > 1)
                    newPageNum--;
                break;
            case RIGHT:
                if(newPageNum == pages && wrapPageEnds)
                    newPageNum = 0;
                if(newPageNum < pages)
                    newPageNum++;
                break;
            case BIG_LEFT:
                if(newPageNum > 1 || wrapPageEnds)
                {
                    for(int i = 1; (newPageNum > 1 || wrapPageEnds) && i < bulkSkipNumber; i++)
                    {
                        if(newPageNum == 1 && wrapPageEnds)
                            newPageNum = pages + 1;
                        newPageNum--;
                    }
                }
                break;
            case BIG_RIGHT:
                if(newPageNum < pages || wrapPageEnds)
                {
                    for(int i = 1; (newPageNum < pages || wrapPageEnds) && i < bulkSkipNumber; i++)
                    {
                        if(newPageNum == pages && wrapPageEnds)
                            newPageNum = 0;
                        newPageNum++;
                    }
                }
                break;
            case STOP:
                finalAction.accept(message);
                return;
        }
        
        try {
            event.getReaction().removeReaction(event.getUser()).queue();
        } catch(PermissionException ignored) {}
        
        int n = newPageNum;
        message.editMessage(renderPage(newPageNum)).queue(m -> pagination(m, n));
    }
    
    private Message renderPage(int pageNum)
    {
        MessageBuilder mbuilder = new MessageBuilder();
        MessageEmbed membed = this.embeds.get(pageNum-1);
        mbuilder.setEmbed(membed);
        if(text != null)
            mbuilder.append(text.apply(pageNum, embeds.size()));
        return mbuilder.build();
    }
    
    /**
     * The {@link com.jagrosh.jdautilities.menu.Menu.Builder Menu.Builder} for
     * a {@link com.jagrosh.jdautilities.menu.EmbedPaginator EmbedPaginator}.
     * 
     * @author Andre_601
     */
    public static class Builder extends Menu.Builder<Builder, EmbedPaginator>
    {
        
        private BiFunction<Integer, Integer, String> text = (page, pages) -> null;
        private Consumer<Message> finalAction = m -> m.delete().queue();
        private boolean waitOnSinglePage = false;
        private int bulkSkipNumber = 1;
        private boolean wrapPageEnds = false;
        private String leftText = null;
        private String rightText = null;
        private boolean allowTextInput = false;
    
        private final List<MessageEmbed> embeds = new LinkedList<>();
    
        /**
         * Builds the {@link com.jagrosh.jdautilities.menu.EmbedPaginator EmbedPaginator} with this Builder.
         * 
         * @return The Paginator built from this Builder.
         * 
         * @throws java.lang.IllegalArgumentException
         *         If one of the following is violated:
         *         <ul>
         *             <li>No {@link com.jagrosh.jdautilities.commons.waiter.EventWaiter EventWaiter} was set.</li>
         *             <li>No items were set to paginate.</li>
         *         </ul>
         */
        @Override
        public EmbedPaginator build()
        {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!embeds.isEmpty(), "Must include at least one item to paginate");
            
            return new EmbedPaginator(
                waiter, users, roles, timeout, unit, text, finalAction, waitOnSinglePage, embeds, bulkSkipNumber,
                wrapPageEnds, leftText, rightText, allowTextInput
            );
        }
    
        /**
         * Sets the text of the {@link net.dv8tion.jda.api.entities.Message Message} to be displayed when the
         * {@link com.jagrosh.jdautilities.menu.EmbedPaginator EmbedPaginator} is built.
         * 
         * @param  text
         *         The Message content to be displayed above the embed when the EmbedPaginator is built.
         *         
         * @return This builder
         */
        public Builder setText(String text)
        {
            this.text = (i0, i1) -> text;
            return this;
        }
    
        /**
         * Sets the text of the {@link net.dv8tion.jda.api.entities.Message Message} to be displayed relative to the
         * total page number and the current page as determined by the provided 
         * {@link java.util.function.BiFunction BiFunction}.
         * <br>As the page changes, the BiFunction will re-process the current page number and the total page number,
         * allowing for the displayed text of the Message to change depending on the page number.
         * 
         * @param  textBiFunction
         *         The BiFunction that uses both current and total page numbers, to get text for the Message
         *         
         * @return This builder
         */
        public Builder setText(BiFunction<Integer, Integer, String> textBiFunction)
        {
            this.text = textBiFunction;
            return this;
        }
    
        /**
         * Sets the {@link java.util.function.Consumer Consumer} to perform if the
         * {@link com.jagrosh.jdautilities.menu.EmbedPaginator EmbedPaginator} times out.
         * 
         * @param  finalAction
         *         The Consumer action to perform if the EmbedPaginator times out
         *         
         * @return This builder
         */
        public Builder setFinalAction(Consumer<Message> finalAction)
        {
            this.finalAction = finalAction;
            return this;
        }
    
        /**
         * Sets whether the {@link com.jagrosh.jdautilities.menu.EmbedPaginator EmbedPaginator} will instantly
         * timeout, and possibly run a provided {@link java.lang.Runnable Runnable}, if only a single slide is
         * available to display.
         * 
         * @param  waitOnSinglePage
         *         {@code true} if the EmbedPaginator will still generate
         *         
         * @return This builder
         */
        public Builder waitOnSinglePage(boolean waitOnSinglePage)
        {
            this.waitOnSinglePage = waitOnSinglePage;
            return this;
        }
    
        /**
         * Clears all previously set items.
         * 
         * @return This builder
         */
        public Builder clearItems()
        {
            this.embeds.clear();
            return this;
        }
    
        /**
         * Adds {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} to the list of items to paginate.
         * 
         * @param  embeds
         *         The list of MessageEmbeds to add
         *         
         * @return This builder
         */
        public Builder addItems(MessageEmbed... embeds)
        {
            this.embeds.addAll(Arrays.asList(embeds));
            return this;
        }
    
        /**
         * Adds the collection of provided {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} to the list
         * of items to paginate.
         * 
         * @param  embeds
         *         The collection of MessageEmbeds to add
         *         
         * @return This builder
         */
        public Builder addItems(Collection<MessageEmbed> embeds)
        {
            this.embeds.addAll(embeds);
            return this;
        }
    
        /**
         * Adds {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} to the list of items to paginate.
         * <br>This method creates a new, basic MessageEmbed containing only the provided String as description.
         * <br>Use the {@link com.jagrosh.jdautilities.menu.Paginator Paginator} for more Embed customization,
         * without providing your own MessageEmbed instances.
         * 
         * @param  items
         *         The String list of items to add as MessageEmbeds
         * 
         * @throws java.lang.IllegalArgumentException
         *         When one of the provided Strings is longer than 2048 characters.
         *         
         * @return This builder
         */
        public Builder addItems(String... items)
        {
            for(String item : items)
            {
                Checks.check(item.length() <= MessageEmbed.TEXT_MAX_LENGTH, "Text may not be longer than 2048 characters.");
                this.embeds.add(new EmbedBuilder().setDescription(item).build());
            }
            return this;
        }
    
        /**
         * Sets the {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} to paginate.
         * <br>This method clears all previously set items before adding the provided MessageEmbeds.
         * 
         * @param  embeds
         *         The MessageEmbed list of items to add
         *         
         * @return This builder
         */
        public Builder setItems(MessageEmbed... embeds)
        {
            this.embeds.clear();
            addItems(Arrays.asList(embeds));
            return this;
        }
    
        /**
         * Sets the {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} to paginate.
         * <br>This method clears all previously set items before adding the provided collection of MessageEmbeds.
         * 
         * @param  embeds
         *         The collection of MessageEmbeds to set.
         *         
         * @return This builder
         */
        public Builder setItems(Collection<MessageEmbed> embeds)
        {
            this.embeds.clear();
            addItems(embeds);
            return this;
        }
    
        /**
         * Sets the {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbeds} to paginate.
         * <br>This method clears all previously set items before setting each String as a new MessageEmbed.
         * <br>Use the {@link com.jagrosh.jdautilities.menu.Paginator Paginator} for more Embed customization,
         * without providing your own MessageEmbed instances.
         * 
         * @param  items
         *         The String list of items to add
         * 
         * @throws java.lang.IllegalArgumentException
         *         When one of the provided Strings is longer than 2048 characters.
         *         
         * @return This builder
         */
        public Builder setItems(String... items)
        {
            this.embeds.clear();
            addItems(items);
            return this;
        }
    
        /**
         * Sets the {@link com.jagrosh.jdautilities.menu.EmbedPaginator EmbedPaginator}'s bulk-skip function to
         * skip multiple pages using alternate forward and backwards reactions.
         * 
         * @param  bulkSkipNumber
         *         The number of pages to skip when the bulk-skip reactions are used.
         *         
         * @return This builder
         */
        public Builder setBulkSkipNumber(int bulkSkipNumber)
        {
            this.bulkSkipNumber = Math.max(bulkSkipNumber, 1);
            return this;
        }
    
        /**
         * Sets the {@link com.jagrosh.jdautilities.menu.EmbedPaginator EmbedPaginator} to wrap from the last page
         * to the first when traversing right and vice versa from the left.
         * 
         * @param  wrapPageEnds
         *         {@code true} to enable wrapping.
         *         
         * @return This builder
         */
        public Builder wrapPageEnds(boolean wrapPageEnds)
        {
            this.wrapPageEnds = wrapPageEnds;
            return this;
        }
    
        /**
         * Sets the {@link com.jagrosh.jdautilities.menu.EmbedPaginator EmbedPaginator} to allow a page number to
         * be specified by a user via text.
         * 
         * <p>Note that setting this doesn't mean that left and right text inputs provided via
         * {@link EmbedPaginator.Builder#setLeftRightText(String, String)} will be invalidated if they were set
         * previously! To invalidate those, provide {@code null} for one or both of the parameters of that method.
         * 
         * @param  allowTextInput
         *         {@code true} if the EmbedPaginator will allow page-number text input.
         *         
         * @return This builder
         */
        public Builder allowTextInput(boolean allowTextInput)
        {
            this.allowTextInput = allowTextInput;
            return this;
        }
    
        /**
         * Sets the {@link com.jagrosh.jdautilities.menu.EmbedPaginator EmbedPaginator} to traverse left or right
         * when a provided text input is sent in the form of a Message to the 
         * {@link net.dv8tion.jda.api.entities.GuildChannel GuildChannel} the menu is displayed in.
         * 
         * <p>If one or both these parameters are provided {@code null} this resets both of them and they will no
         * longer be available when the Paginator is built.
         * 
         * @param  left
         *         The left text input, causes the EmbedPaginator to traverse one page left.
         * @param  right
         *         The right text input, causes the EmbedPaginator to traverse one page right.
         *         
         * @return This builder
         */
        public Builder setLeftRightText(String left, String right)
        {
            if(left == null || right == null)
            {
                leftText = null;
                rightText = null;
            }
            else
            {
                leftText = left;
                rightText = right;
            }
            return this;
        }
    }
}
