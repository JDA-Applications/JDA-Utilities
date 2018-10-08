package com.jagrosh.jdautilities.menu;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.Checks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link com.jagrosh.jdautilities.menu.Menu Menu} implementation that creates
 * a scrollable display of text choices that users can scroll through.
 * <br>This is very similar to the {@link com.jagrosh.jdautilities.menu.SelectionDialog SelectionDialog},
 * though it only displays the current selection and provides the possibility to design the selection display freely.
 *
 * @author Johnny_JayJay (https://github.com/johnnyjayjay)
 */
public class ScrollSelection extends SelectionDialog
{

    private final String format;

    private ScrollSelection(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
                            List<String> choices, Function<Integer, Color> color, boolean loop, BiConsumer<Message, Integer> success,
                            Consumer<Message> cancel, Function<Integer, String> text, String format)
    {
        super(waiter, users, roles, timeout, unit, choices, null, null, null, null, color, loop, success, cancel, text);
        this.format = format;
    }

    @Override
    protected Message render(int selection)
    {
        String content = text.apply(selection);
        return new MessageBuilder()
            .setContent(content != null ? content : "")
            .setEmbed(new EmbedBuilder()
                .setColor(color.apply(selection))
                .setDescription(String.format(format, choices.get(selection - 1)))
                .build())
            .build();
    }

    /**
     * The {@link com.jagrosh.jdautilities.menu.Menu.Builder Menu.Builder} for
     * a {@link com.jagrosh.jdautilities.menu.ScrollSelection ScrollSelection}.
     *
     * @author Johnny_JayJay (https://github.com/johnnyjayjay)
     */
    public static class Builder extends Menu.Builder<Builder, ScrollSelection>
    {

        private final List<String> choices = new ArrayList<>();
        private Function<Integer,Color> color = i -> null;
        private boolean loop = true;
        private Function<Integer,String> text = i -> null;
        private BiConsumer<Message, Integer> selection;
        private Consumer<Message> cancel = (m) -> {};
        private String format = null;

        /**
         * Builds the {@link com.jagrosh.jdautilities.menu.ScrollSelection ScrollSelection}
         * with this Builder.
         *
         * @return The ScrollSelection built from this Builder.
         *
         * @throws java.lang.IllegalArgumentException
         *         If one of the following is violated:
         *         <ul>
         *             <li>No {@link com.jagrosh.jdautilities.commons.waiter.EventWaiter EventWaiter} was set.</li>
         *             <li>No choices were set.</li>
         *             <li>No action {@link java.util.function.BiConsumer BiConsumer} was set.</li>
         *             <li>No format String was set.</li>
         *             <li>The format String does not contain one String formatter ({@code %s}) exactly.</li>
         *         </ul>
         */
        @Override
        public ScrollSelection build()
        {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!choices.isEmpty(), "Must have at least one choice");
            Checks.check(selection != null, "Must provide a selection consumer");
            Checks.check(format != null, "Must set a format String");
            Checks.check(format.split("%s").length == 2, "Format String must contain one String formatter (%s), no more, no less");

            return new ScrollSelection(waiter, users, roles, timeout, unit, choices,
                color, loop, selection, cancel, text, format);
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
         * Sets if scrolling up when at the top selection jumps to the last selection, and vice versa.
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

        /**
         * Sets the format in which this selection is presented.
         * <br>Example: {@code "```\n%s```"} as a parameter would result in a format
         * that always shows the current selection in a code block
         * (the {@code %s} is replaced by the current selection later)
         *
         * @param  format
         *         The format String. Must contain one String formatter ({@code "%s"}), no more, no less.
         *
         * @return This builder
         */
        public Builder setFormat(String format)
        {
            this.format = format;
            return this;
        }

    }
}
