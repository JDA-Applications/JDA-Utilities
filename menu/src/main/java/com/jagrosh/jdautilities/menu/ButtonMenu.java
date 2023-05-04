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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.utils.Checks;

/**
 * A {@link com.jagrosh.jdautilities.menu.Menu Menu} implementation that creates
 * a organized display of emotes/emojis as buttons paired with options, and below
 * the menu reactions corresponding to each button.
 *
 * @author John Grosh
 */
public class ButtonMenu extends Menu
{
    private final Color color;
    private final String text;
    private final String description;
    private final List<String> choices;
    private final Consumer<Emoji> action;
    private final Consumer<Message> finalAction;
    
    ButtonMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
               Color color, String text, String description, List<String> choices, Consumer<Emoji> action, Consumer<Message> finalAction)
    {
        super(waiter, users, roles, timeout, unit);
        this.color = color;
        this.text = text;
        this.description = description;
        this.choices = choices;
        this.action = action;
        this.finalAction = finalAction;
    }

    /**
     * Shows the ButtonMenu as a new {@link net.dv8tion.jda.api.entities.Message Message}
     * in the provided {@link net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion MessageChannel}.
     * 
     * @param  channel
     *         The MessageChannel to send the new Message to
     */
    @Override
    public void display(MessageChannelUnion channel)
    {
        initialize(channel.sendMessage(getMessageCreate()));
    }

    /**
     * Displays this ButtonMenu by editing the provided {@link net.dv8tion.jda.api.entities.Message Message}.
     * 
     * @param  message
     *         The Message to display the Menu in
     */
    @Override
    public void display(Message message)
    {
        initialize(message.editMessage(getMessage()));
    }

    // Initializes the ButtonMenu using a Message RestAction
    // This is either through editing a previously existing Message
    // OR through sending a new one to a TextChannel.
    private void initialize(RestAction<Message> ra)
    {
        ra.queue(m -> {
            for(int i=0; i<choices.size(); i++)
            {
                // Get the emote to display.
                Emoji emote;
                try {
                    emote = m.getJDA().getEmojiById(choices.get(i));
                } catch(Exception e) {
                    emote = null;
                }
                // If the emote is null that means that it might be an emoji.
                // If it's neither, that's on the developer and we'll let it
                // throw an error when we queue a rest action.
                RestAction<Void> r = emote==null ? m.addReaction(Emoji.fromFormatted(choices.get(i))) : m.addReaction(emote);
                if(i+1<choices.size())
                    r.queue(); // If there is still more reactions to add we delay using the EventWaiter
                else
                {
                    // This is the last reaction added.
                    r.queue(v -> {
                        waiter.waitForEvent(MessageReactionAddEvent.class, event -> {
                            // If the message is not the same as the ButtonMenu
                            // currently being displayed.
                            if(!event.getMessageId().equals(m.getId()))
                                return false;

                            // If the reaction is an Emote we get the Snowflake,
                            // otherwise we get the unicode value.
                            String re = event.getReaction().getEmoji().getAsReactionCode();

                            // If the value we got is not registered as a button to
                            // the ButtonMenu being displayed we return false.
                            if(!choices.contains(re))
                                return false;

                            // Last check is that the person who added the reaction
                            // is a valid user.
                            return isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
                        }, (MessageReactionAddEvent event) -> {
                            // What happens next is after a valid event
                            // is fired and processed above.

                            // Preform the specified action with the ReactionEmote
                            action.accept(event.getReaction().getEmoji());
                            finalAction.accept(m);
                        }, timeout, unit, () -> finalAction.accept(m));
                    });
                }
            }
        });
    }

    // Generates a ButtonMenu message
    private MessageCreateData getMessageCreate()
    {
        MessageCreateBuilder mbuilder = new MessageCreateBuilder();
        if(text!=null)
            mbuilder.setContent(text);
        if(description!=null)
            mbuilder.setEmbeds(new EmbedBuilder().setColor(color).setDescription(description).build());
        return mbuilder.build();
    }

    // Generates a ButtonMenu message
    private MessageEditData getMessage()
    {
        MessageEditBuilder mbuilder = new MessageEditBuilder();
        if(text!=null)
            mbuilder.setContent(text);
        if(description!=null)
            mbuilder.setEmbeds(new EmbedBuilder().setColor(color).setDescription(description).build());
        return mbuilder.build();
    }

    /**
     * The {@link com.jagrosh.jdautilities.menu.Menu.Builder Menu.Builder} for
     * a {@link com.jagrosh.jdautilities.menu.ButtonMenu ButtonMenu}.
     *
     * @author John Grosh
     */
    public static class Builder extends Menu.Builder<Builder, ButtonMenu>
    {
        private Color color;
        private String text;
        private String description;
        private final List<String> choices = new LinkedList<>();
        private Consumer<Emoji> action;
        private Consumer<Message> finalAction = (m) -> {};

        /**
         * Builds the {@link com.jagrosh.jdautilities.menu.ButtonMenu ButtonMenu}
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
         *             <li>Neither text nor description were set.</li>
         *         </ul>
         */
        @Override
        public ButtonMenu build()
        {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!choices.isEmpty(), "Must have at least one choice");
            Checks.check(action != null, "Must provide an action consumer");
            Checks.check(text != null || description != null, "Either text or description must be set");

            return new ButtonMenu(waiter, users, roles, timeout, unit, color, text, description, choices, action, finalAction);
        }

        /**
         * Sets the {@link java.awt.Color Color} of the {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}.
         *
         * @param  color
         *         The Color of the MessageEmbed
         *
         * @return This builder
         */
        public Builder setColor(Color color)
        {
            this.color = color;
            return this;
        }

        /**
         * Sets the text of the {@link net.dv8tion.jda.api.entities.Message Message} to be displayed
         * when the {@link com.jagrosh.jdautilities.menu.ButtonMenu ButtonMenu} is built.
         *
         * <p>This is displayed directly above the embed.
         *
         * @param  text
         *         The Message content to be displayed above the embed when the ButtonMenu is built
         *
         * @return This builder
         */
        public Builder setText(String text)
        {
            this.text = text;
            return this;
        }

        /**
         * Sets the description to be placed in an {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}.
         * <br>If this is {@code null}, no MessageEmbed will be displayed
         *
         * @param  description
         *         The content of the MessageEmbed's description
         *
         * @return This builder
         */
        public Builder setDescription(String description)
        {
            this.description = description;
            return this;
        }

        /**
         * Sets the {@link java.util.function.Consumer Consumer} action to perform upon selecting a button.
         *
         * @param  action
         *         The Consumer action to perform upon selecting a button
         *
         * @return This builder
         */
        public Builder setAction(Consumer<Emoji> action)
        {
            this.action = action;
            return this;
        }

        /**
         * Sets the {@link java.util.function.Consumer Consumer} to perform if the
         * {@link com.jagrosh.jdautilities.menu.ButtonMenu ButtonMenu} is done,
         * either via cancellation, a timeout, or a selection being made.<p>
         *
         * This accepts the message used to display the menu when called.
         *
         * @param  finalAction
         *         The Runnable action to perform if the ButtonMenu is done
         *
         * @return This builder
         */
        public Builder setFinalAction(Consumer<Message> finalAction)
        {
            this.finalAction = finalAction;
            return this;
        }

        /**
         * Adds a single String unicode emoji as a button choice.
         *
         * <p>Any non-unicode {@link net.dv8tion.jda.api.entities.emoji.Emoji Emote} should be
         * added using {@link ButtonMenu.Builder#addChoice(Emoji)
         * ButtonMenu.Builder#addChoice(Emote)}.
         *
         * @param  emoji
         *         The String unicode emoji to add
         *
         * @return This builder
         */
        public Builder addChoice(String emoji)
        {
            this.choices.add(emoji);
            return this;
        }

        /**
         * Adds a single custom {@link net.dv8tion.jda.api.entities.emoji.Emoji  Emote} as button choices.
         *
         * <p>Any regular unicode emojis should be added using {@link
         * ButtonMenu.Builder#addChoice(String)
         * ButtonMenu.Builder#addChoice(String)}.
         *
         * @param  emote
         *         The Emote object to add
         *
         * @return This builder
         */
        public Builder addChoice(Emoji emote)
        {
            return addChoice(emote.getFormatted());
        }

        /**
         * Adds String unicode emojis as button choices.
         *
         * <p>Any non-unicode {@link net.dv8tion.jda.api.entities.emoji.Emoji  Emote}s should be
         * added using {@link ButtonMenu.Builder#addChoices(Emoji...)
         * ButtonMenu.Builder#addChoices(Emote...)}.
         *
         * @param  emojis
         *         The String unicode emojis to add
         *
         * @return This builder
         */
        public Builder addChoices(String... emojis)
        {
            for(String emoji : emojis)
                addChoice(emoji);
            return this;
        }

        /**
         * Adds custom {@link net.dv8tion.jda.api.entities.emoji.Emoji  Emote}s as button choices.
         *
         * <p>Any regular unicode emojis should be added using {@link
         * ButtonMenu.Builder#addChoices(String...)
         * ButtonMenu.Builder#addChoices(String...)}.
         *
         * @param  emotes
         *         The Emote objects to add
         *
         * @return This builder
         */
        public Builder addChoices(Emoji... emotes)
        {
            for(Emoji emote : emotes)
                addChoice(emote);
            return this;
        }

        /**
         * Sets the String unicode emojis as button choices.
         *
         * <p>Any non-unicode {@link net.dv8tion.jda.api.entities.emoji.Emoji  Emote}s should be
         * set using {@link ButtonMenu.Builder#setChoices(Emoji...)
         * ButtonMenu.Builder#setChoices(Emote...)}.
         *
         * @param  emojis
         *         The String unicode emojis to set
         *
         * @return This builder
         */
        public Builder setChoices(String... emojis)
        {
            this.choices.clear();
            return addChoices(emojis);
        }

        /**
         * Sets the {@link net.dv8tion.jda.api.entities.emoji.Emoji  Emote}s as button choices.
         *
         * <p>Any regular unicode emojis should be set using {@link
         * ButtonMenu.Builder#setChoices(String...)
         * ButtonMenu.Builder#setChoices(String...)}.
         *
         * @param  emotes
         *         The Emote objects to set
         *
         * @return This builder
         */
        public Builder setChoices(Emoji... emotes)
        {
            this.choices.clear();
            return addChoices(emotes);
        }
    }
}
