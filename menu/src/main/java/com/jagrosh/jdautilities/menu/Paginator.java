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

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.Checks;

/**
 * A {@link com.jagrosh.jdautilities.menu.Menu Menu} implementation that paginates a
 * set of one or more text items across one or more pages.
 *
 * <p>When displayed, a Paginator will add three reactions in the following order:
 * <ul>
 *     <li><b>Left Arrow</b> - Causes the Paginator to traverse one page backwards.</li>
 *     <li><b>Stop</b> - Stops the Paginator.</li>
 *     <li><b>Right Arrow</b> - Causes the Paginator to traverse one page forwards.</li>
 * </ul>
 *
 * Additionally, if specified in the {@link Paginator.Builder}, two "bulk skip" reactions
 * will be added to allow a certain number of pages to be skipped left or right.
 * <br>Paginator.Builders can also set a Paginator to accept various forms of text-input,
 * such as left and right text commands, and even user specified page number via text.
 *
 * @author John Grosh
 */
public class Paginator extends Menu
{
    private final BiFunction<Integer,Integer,Color> color;
    private final BiFunction<Integer,Integer,String> text;
    private final int columns;
    private final int itemsPerPage;
    private final boolean showPageNumbers;
    private final boolean numberItems;
    private final List<String> strings;
    private final int pages;
    private final Consumer<Message> finalAction;
    private final boolean waitOnSinglePage;
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
    
    Paginator(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
              BiFunction<Integer,Integer,Color> color, BiFunction<Integer,Integer,String> text,
              Consumer<Message> finalAction, int columns, int itemsPerPage, boolean showPageNumbers,
              boolean numberItems, List<String> items, boolean waitOnSinglePage, int bulkSkipNumber,
              boolean wrapPageEnds, String leftText, String rightText, boolean allowTextInput)
    {
        super(waiter, users, roles, timeout, unit);
        this.color = color;
        this.text = text;
        this.columns = columns;
        this.itemsPerPage = itemsPerPage;
        this.showPageNumbers = showPageNumbers;
        this.numberItems = numberItems;
        this.strings = items;
        this.pages = (int)Math.ceil((double)strings.size()/itemsPerPage);
        this.finalAction = finalAction;
        this.waitOnSinglePage = waitOnSinglePage;
        this.bulkSkipNumber = bulkSkipNumber;
        this.wrapPageEnds = wrapPageEnds;
        this.leftText = leftText;
        this.rightText = rightText;
        this.allowTextInput = allowTextInput;
    }

    /**
     * Begins pagination on page 1 as a new {@link net.dv8tion.jda.core.entities.Message Message} 
     * in the provided {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}.
     *
     * <p>Starting on another page is available via {@link
     * Paginator#paginate(MessageChannel, int)
     * Paginator#paginate(MessageChannel, int)}.
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
     * Begins pagination on page 1 displaying this Pagination by editing the provided 
     * {@link net.dv8tion.jda.core.entities.Message Message}.
     *
     * <p>Starting on another page is available via
     * {@link Paginator#paginate(Message, int) Paginator#paginate(Message, int)}.
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
        else if (pageNum>pages)
            pageNum = pages;
        Message msg = renderPage(pageNum);
        initialize(channel.sendMessage(msg), pageNum);
    }
    
    /**
     * Begins pagination displaying this Pagination by editing the provided 
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
        else if (pageNum>pages)
            pageNum = pages;
        Message msg = renderPage(pageNum);
        initialize(message.editMessage(msg), pageNum);
    }

    private void initialize(RestAction<Message> action, int pageNum)
    {
        action.queue(m -> {
            setAttachedMessage(m);
            if(pages > 1)
            {
                if(bulkSkipNumber > 1)
                    m.addReaction(BIG_LEFT).queue();
                m.addReaction(LEFT).queue();
                m.addReaction(STOP).queue();
                if(bulkSkipNumber > 1)
                    m.addReaction(RIGHT).queue();
                m.addReaction(bulkSkipNumber > 1? BIG_RIGHT : RIGHT)
                 .queue(v -> pagination(pageNum), t -> pagination(pageNum));
            }
            else if(waitOnSinglePage)
            {
                // Go straight to without text-input because only one page is available
                m.addReaction(STOP).queue(
                    v -> paginationWithoutTextInput(pageNum),
                    t -> paginationWithoutTextInput(pageNum)
                );
            }
            else
            {
                finalAction.accept(m);
            }
        });
    }

    private void pagination(int pageNum)
    {
        if(allowTextInput || (leftText != null && rightText != null))
            paginationWithTextInput(pageNum);
        else
            paginationWithoutTextInput(pageNum);
    }

    private void paginationWithTextInput(int pageNum)
    {
        setCancelFuture(waiter.waitForEvent(GenericMessageEvent.class, event -> {
            if(event instanceof MessageReactionAddEvent)
                return checkReaction((MessageReactionAddEvent) event);
            else if(event instanceof MessageReceivedEvent)
            {
                MessageReceivedEvent mre = (MessageReceivedEvent) event;
                // Wrong channel
                if(!mre.getChannel().equals(getAttachedMessage().getChannel()))
                    return false;
                String rawContent = mre.getMessage().getContentRaw().trim();
                if(leftText != null && rightText != null)
                {
                    if(rawContent.equalsIgnoreCase(leftText) || rawContent.equalsIgnoreCase(rightText))
                        return isValidUser(mre.getAuthor(), mre.getGuild());
                }

                if(allowTextInput)
                {
                    try {
                        int i = Integer.parseInt(rawContent);
                        // Minimum 1, Maximum the number of pages, never the current page number
                        if(1 <= i && i <= pages && i != pageNum)
                            return isValidUser(mre.getAuthor(), mre.getGuild());
                    } catch(NumberFormatException ignored) {}
                }
            }
            // Default return false
            return false;
        }, event -> {
            if(event instanceof MessageReactionAddEvent)
            {
                handleMessageReactionAddAction((MessageReactionAddEvent) event, pageNum);
            }
            else
            {
                MessageReceivedEvent mre = ((MessageReceivedEvent) event);
                String rawContent = mre.getMessage().getContentRaw().trim();

                final int targetPage;

                if(leftText != null && rawContent.equalsIgnoreCase(leftText) && (1 < pageNum || wrapPageEnds))
                    targetPage = pageNum - 1 < 1 && wrapPageEnds? pages : pageNum - 1;
                else if(rightText != null && rawContent.equalsIgnoreCase(rightText) && (pageNum < pages || wrapPageEnds))
                    targetPage = pageNum + 1 > pages && wrapPageEnds? 1 : pageNum + 1;
                else
                {
                    // This will run without fail because we know the above conditions don't apply but our logic
                    // when checking the event in the block above this action block has guaranteed this is the only
                    // option at this point
                    targetPage = Integer.parseInt(rawContent);
                }

                getAttachedMessage().editMessage(renderPage(targetPage)).queue(m -> pagination(targetPage));
                mre.getMessage().delete().queue(v -> {}, t -> {}); // delete the calling message so it doesn't get spammy
            }
        }, timeout, unit, () -> callFinalAction(finalAction)));
    }
    
    private void paginationWithoutTextInput(int pageNum)
    {
        setCancelFuture(waiter.waitForEvent(MessageReactionAddEvent.class,
            event -> checkReaction(event), // Check Reaction
            event -> handleMessageReactionAddAction(event, pageNum), // Handle Reaction
            timeout, unit, () -> callFinalAction(finalAction)));
    }

    // Private method that checks MessageReactionAddEvents
    private boolean checkReaction(MessageReactionAddEvent event)
    {
        if(event.getMessageIdLong() != getAttachedMessage().getIdLong())
            return false;
        switch(event.getReactionEmote().getName())
        {
            // LEFT, STOP, RIGHT, BIG_LEFT, BIG_RIGHT all fall-through to
            // return if the User is valid or not. If none trip, this defaults
            // and returns false.
            case LEFT:
            case STOP:
            case RIGHT:
                return isValidUser(event.getUser(), event.getGuild());
            case BIG_LEFT:
            case BIG_RIGHT:
                return bulkSkipNumber > 1 && isValidUser(event.getUser(), event.getGuild());
            default:
                return false;
        }
    }

    // Private method that handles MessageReactionAddEvents
    private void handleMessageReactionAddAction(MessageReactionAddEvent event, int pageNum)
    {
        int newPageNum = pageNum;
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
                callFinalAction(finalAction);
                return;
        }

        try {
            event.getReaction().removeReaction(event.getUser()).queue();
        } catch(PermissionException ignored) {}

        int n = newPageNum;
        getAttachedMessage().editMessage(renderPage(newPageNum)).queue(m -> pagination(n));
    }
    
    private Message renderPage(int pageNum)
    {
        MessageBuilder mbuilder = new MessageBuilder();
        EmbedBuilder ebuilder = new EmbedBuilder();
        int start = (pageNum-1)*itemsPerPage;
        int end = strings.size() < pageNum*itemsPerPage ? strings.size() : pageNum*itemsPerPage;
        if(columns == 1)
        {
            StringBuilder sbuilder = new StringBuilder();
            for(int i=start; i<end; i++)
                sbuilder.append("\n").append(numberItems ? "`"+(i+1)+".` " : "").append(strings.get(i));
            ebuilder.setDescription(sbuilder.toString());
        }
        else
        {
            int per = (int)Math.ceil((double)(end-start)/columns);
            for(int k=0; k<columns; k++)
            {
                StringBuilder strbuilder = new StringBuilder();
                for(int i=start+k*per; i<end && i<start+(k+1)*per; i++)
                    strbuilder.append("\n").append(numberItems ? (i+1)+". " : "").append(strings.get(i));
                ebuilder.addField("", strbuilder.toString(), true);
            }
        }
        
        ebuilder.setColor(color.apply(pageNum, pages));
        if(showPageNumbers)
            ebuilder.setFooter("Page "+pageNum+"/"+pages, null);
        mbuilder.setEmbed(ebuilder.build());
        if(text!=null)
            mbuilder.append(text.apply(pageNum, pages));
        return mbuilder.build();
    }

    /**
     * The {@link com.jagrosh.jdautilities.menu.Menu.Builder Menu.Builder} for
     * a {@link com.jagrosh.jdautilities.menu.Paginator Paginator}.
     *
     * @author John Grosh
     */
    public static class Builder extends Menu.Builder<Builder, Paginator>
    {
        private BiFunction<Integer,Integer,Color> color = (page, pages) -> null;
        private BiFunction<Integer,Integer,String> text = (page, pages) -> null;
        private Consumer<Message> finalAction = m -> m.delete().queue();
        private int columns = 1;
        private int itemsPerPage = 12;
        private boolean showPageNumbers = true;
        private boolean numberItems = false;
        private boolean waitOnSinglePage = false;
        private int bulkSkipNumber = 1;
        private boolean wrapPageEnds = false;
        private String textToLeft = null;
        private String textToRight = null;
        private boolean allowTextInput = false;

        private final List<String> strings = new LinkedList<>();

        /**
         * Builds the {@link com.jagrosh.jdautilities.menu.Paginator Paginator}
         * with this Builder.
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
        public Paginator build()
        {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!strings.isEmpty(), "Must include at least one item to paginate");

            return new Paginator(waiter, users, roles, timeout, unit, color, text, finalAction,
                columns, itemsPerPage, showPageNumbers, numberItems, strings, waitOnSinglePage,
                bulkSkipNumber, wrapPageEnds, textToLeft, textToRight, allowTextInput);
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
         * when the {@link com.jagrosh.jdautilities.menu.Paginator Paginator} is built.
         *
         * <p>This is displayed directly above the embed.
         *
         * @param  text
         *         The Message content to be displayed above the embed when the Paginator is built
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
         * Sets the {@link java.util.function.Consumer Consumer} to perform if the
         * {@link com.jagrosh.jdautilities.menu.Paginator Paginator} times out.
         *
         * @param  finalAction
         *         The Consumer action to perform if the Paginator times out
         *
         * @return This builder
         */
        public Builder setFinalAction(Consumer<Message> finalAction)
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
        public Builder setColumns(int columns)
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
        public Builder setItemsPerPage(int num)
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
        public Builder showPageNumbers(boolean show)
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
        public Builder useNumberedItems(boolean number)
        {
            this.numberItems = number;
            return this;
        }

        /**
         * Sets whether the {@link com.jagrosh.jdautilities.menu.Paginator Paginator} will instantly
         * timeout, and possibly run a provided {@link java.lang.Runnable Runnable}, if only a single slide is available to display.
         *
         * @param  wait
         *         {@code true} if the Paginator will still generate
         *
         * @return This builder
         */
        public Builder waitOnSinglePage(boolean wait)
        {
            this.waitOnSinglePage = wait;
            return this;
        }

        /**
         * Clears the list of String items to paginate.
         *
         * @return This builder
         */
        public Builder clearItems()
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
        public Builder addItems(String... items)
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
        public Builder setItems(String... items)
        {
            strings.clear();
            strings.addAll(Arrays.asList(items));
            return this;
        }

        /**
         * Sets the {@link com.jagrosh.jdautilities.menu.Paginator Paginator}'s bulk-skip
         * function to skip multiple pages using alternate forward and backwards
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
         * Sets the {@link com.jagrosh.jdautilities.menu.Paginator Paginator} to wrap
         * from the last page to the first when traversing right and visa versa from the left.
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
         * Sets the {@link com.jagrosh.jdautilities.menu.Paginator Paginator} to allow
         * a page number to be specified by a user via text.
         *
         * <p>Note that setting this doesn't mean that left and right text inputs
         * provided via {@link Paginator.Builder#setLeftRightText(String, String)} will
         * be invalidated if they were set previously! To invalidate those, provide
         * {@code null} for one or both of the parameters of that method.
         *
         * @param  allowTextInput
         *         {@code true} if the Paginator will allow page-number text input
         *
         * @return This builder
         */
        public Builder allowTextInput(boolean allowTextInput)
        {
            this.allowTextInput = allowTextInput;
            return this;
        }

        /**
         * Sets the {@link com.jagrosh.jdautilities.menu.Paginator Paginator} to traverse
         * left or right when a provided text input is sent in the form of a Message to
         * the {@link net.dv8tion.jda.core.entities.Channel Channel} the menu is displayed in.
         *
         * <p>If one or both these parameters are provided {@code null} this resets
         * both of them and they will no longer be available when the Paginator is built.
         *
         * @param  left
         *         The left text input, causes the Paginator to traverse one page left
         * @param  right
         *         The right text input, causes the Paginator to traverse one page right
         *
         * @return This builder
         */
        public Builder setLeftRightText(String left, String right)
        {
            if(left == null || right == null)
            {
                textToLeft = null;
                textToRight = null;
            }
            else
            {
                textToLeft = left;
                textToRight = right;
            }
            return this;
        }
    }
}
