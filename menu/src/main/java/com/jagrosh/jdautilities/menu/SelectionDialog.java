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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.Checks;

/**
 * A {@link com.jagrosh.jdautilities.menu.Menu Menu} implementation that creates
 * a listed display of text choices horizontally that users can scroll through
 * using reactions and make selections.
 *
 * @author John Grosh
 */
public class SelectionDialog extends Menu
{
    private final List<String> choices;
    private final String leftEnd, rightEnd;
    private final String defaultLeft, defaultRight;
    private final Function<Integer,Color> color;
    private final boolean loop;
    private final Function<Integer,String> text;
    private final BiConsumer<Message, Integer> success;
    private final Consumer<Message> cancel;
    
    public static final String UP = "\uD83D\uDD3C";
    public static final String DOWN = "\uD83D\uDD3D";
    public static final String SELECT = "\u2705";
    public static final String CANCEL = "\u274E";
    
    SelectionDialog(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
                    List<String> choices, String leftEnd, String rightEnd, String defaultLeft, String defaultRight,
                    Function<Integer,Color> color, boolean loop, BiConsumer<Message, Integer> success,
                    Consumer<Message> cancel, Function<Integer,String> text)
    {
        super(waiter, users, roles, timeout, unit);
        this.choices = choices;
        this.leftEnd = leftEnd;
        this.rightEnd = rightEnd;
        this.defaultLeft = defaultLeft;
        this.defaultRight = defaultRight;
        this.color = color;
        this.loop = loop;
        this.success = success;
        this.cancel = cancel;
        this.text = text;
    }

    /**
     * Shows the SelectionDialog as a new {@link net.dv8tion.jda.core.entities.Message Message} 
     * in the provided {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}, starting with
     * the first selection.
     * 
     * @param  channel
     *         The MessageChannel to send the new Message to
     */
    @Override
    public void display(MessageChannel channel)
    {
        showDialog(channel, 1);
    }

    /**
     * Displays this SelectionDialog by editing the provided 
     * {@link net.dv8tion.jda.core.entities.Message Message}, starting with the first selection.
     * 
     * @param  message
     *         The Message to display the Menu in
     */
    @Override
    public void display(Message message)
    {
        showDialog(message, 1);
    }
    
    /**
     * Shows the SelectionDialog as a new {@link net.dv8tion.jda.core.entities.Message Message} 
     * in the provided {@link net.dv8tion.jda.core.entities.MessageChannel MessageChannel}, starting with
     * the number selection provided.
     * 
     * @param  channel
     *         The MessageChannel to send the new Message to
     * @param  selection
     *         The number selection to start on
     */
    public void showDialog(MessageChannel channel, int selection)
    {
        if(selection<1)
            selection = 1;
        else if(selection>choices.size())
            selection = choices.size();
        Message msg = render(selection);
        initialize(channel.sendMessage(msg), selection);
    }
    
    /**
     * Displays this SelectionDialog by editing the provided 
     * {@link net.dv8tion.jda.core.entities.Message Message}, starting with the number selection
     * provided.
     * 
     * @param  message
     *         The Message to display the Menu in
     * @param  selection
     *         The number selection to start on
     */
    public void showDialog(Message message, int selection)
    {
        if(selection<1)
            selection = 1;
        else if(selection>choices.size())
            selection = choices.size();
        Message msg = render(selection);
        initialize(message.editMessage(msg), selection);
    }
    
    private void initialize(RestAction<Message> action, int selection)
    {
        action.queue(m -> {
            if(choices.size()>1)
            {
                m.addReaction(UP).queue();
                m.addReaction(SELECT).queue();
                m.addReaction(CANCEL).queue();
                m.addReaction(DOWN).queue(v -> selectionDialog(m, selection), v -> selectionDialog(m, selection));
            }
            else
            {
                m.addReaction(SELECT).queue();
                m.addReaction(CANCEL).queue(v -> selectionDialog(m, selection), v -> selectionDialog(m, selection));
            }
        });
    }
    
    private void selectionDialog(Message message, int selection)
    {
        waiter.waitForEvent(MessageReactionAddEvent.class, event -> {
            if(!event.getMessageId().equals(message.getId()))
                return false;
            if(!(UP.equals(event.getReaction().getReactionEmote().getName())
                    || DOWN.equals(event.getReaction().getReactionEmote().getName())
                    || CANCEL.equals(event.getReaction().getReactionEmote().getName())
                    || SELECT.equals(event.getReaction().getReactionEmote().getName())))
                return false;
            return isValidUser(event.getUser(), event.getGuild());
        }, event -> {
            int newSelection = selection;
            switch(event.getReaction().getReactionEmote().getName())
            {
                case UP:
                    if(newSelection>1)
                        newSelection--;
                    else if(loop)
                        newSelection = choices.size();
                    break;
                case DOWN:
                    if(newSelection<choices.size())
                        newSelection++;
                    else if(loop)
                        newSelection = 1;
                    break;
                case SELECT:
                    success.accept(message, selection);
                    break;
                case CANCEL:
                    cancel.accept(message);
                    return;

            }
            try {
                event.getReaction().removeReaction(event.getUser()).queue();
            } catch (PermissionException ignored) {}
            int n = newSelection;
            message.editMessage(render(n)).queue(m -> selectionDialog(m, n));
        }, timeout, unit, () -> cancel.accept(message));
    }
    
    private Message render(int selection)
    {
        StringBuilder sbuilder = new StringBuilder();
        for(int i=0; i<choices.size(); i++)
            if(i+1==selection)
                sbuilder.append("\n").append(leftEnd).append(choices.get(i)).append(rightEnd);
            else
                sbuilder.append("\n").append(defaultLeft).append(choices.get(i)).append(defaultRight);
        MessageBuilder mbuilder = new MessageBuilder();
        String content = text.apply(selection);
        if(content!=null)
            mbuilder.append(content);
        return mbuilder.setEmbed(new EmbedBuilder()
                .setColor(color.apply(selection))
                .setDescription(sbuilder.toString())
                .build()).build();
    }

    /**
     * The {@link com.jagrosh.jdautilities.menu.Menu.Builder Menu.Builder} for
     * a {@link com.jagrosh.jdautilities.menu.SelectionDialog SelectuibDialog}.
     *
     * @author John Grosh
     */
    public static class Builder extends Menu.Builder<Builder, SelectionDialog>
    {
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

        /**
         * Builds the {@link com.jagrosh.jdautilities.menu.SelectionDialog SelectionDialog}
         * with this Builder.
         *
         * @return The OrderedMenu built from this Builder.
         *
         * @throws java.lang.IllegalArgumentException
         *         If one of the following is violated:
         *         <ul>
         *             <li>No {@link com.jagrosh.jdautilities.commons.waiter.EventWaiter EventWaiter} was set.</li>
         *             <li>No choices were set.</li>
         *             <li>No action {@link java.util.function.Consumer Consumer} was set.</li>
         *         </ul>
         */
        @Override
        public SelectionDialog build()
        {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!choices.isEmpty(), "Must have at least one choice");
            Checks.check(selection != null, "Must provide a selection consumer");

            return new SelectionDialog(waiter,users,roles,timeout,unit,choices,leftEnd,rightEnd,
                    defaultLeft,defaultRight,color,loop, selection, cancel,text);
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
        public Builder setColor(Function<Integer,Color> color)
        {
            this.color = color;
            return this;
        }

        /**
         * Sets the text of the {@link net.dv8tion.jda.core.entities.Message Message} to be displayed
         * when the {@link com.jagrosh.jdautilities.menu.SelectionDialog SelectionDialog} is built.
         *
         * <p>This is displayed directly above the embed.
         *
         * @param  text
         *         The Message content to be displayed above the embed when the SelectionDialog is built
         *
         * @return This builder
         */
        public Builder setText(String text)
        {
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
        public Builder setText(Function<Integer,String> text)
        {
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
        public Builder setSelectedEnds(String left, String right)
        {
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
        public Builder setDefaultEnds(String left, String right)
        {
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
        public Builder useLooping(boolean loop)
        {
            this.loop = loop;
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
        public Builder setSelectionConsumer(BiConsumer<Message, Integer> selection)
        {
            this.selection = selection;
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
        public Builder setCanceled(Consumer<Message> cancel)
        {
            this.cancel = cancel;
            return this;
        }

        /**
         * Clears the choices to be shown.
         *
         * @return This builder
         */
        public Builder clearChoices()
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
        public Builder setChoices(String... choices)
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
        public Builder addChoices(String... choices)
        {
            this.choices.addAll(Arrays.asList(choices));
            return this;
        }
    }
}
