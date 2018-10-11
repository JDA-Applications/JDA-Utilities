package com.jagrosh.jdautilities.menu;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.Checks;

import java.awt.Color;
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
public class ScrollSelection extends SelectionMenu
{

    private final String format;

    private ScrollSelection(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
                            List<String> choices, Function<Integer, Color> color, boolean loop, boolean singleSelectionMode, BiConsumer<Message, Integer> success,
                            Consumer<Message> cancel, Function<Integer, String> text, String format)
    {
        super(waiter, users, roles, timeout, unit, choices, loop, singleSelectionMode, cancel, color, text, success, Arrays.asList(UP, SELECT, CANCEL, DOWN));
        this.format = format;
    }

    @Override
    protected Message render(int selection)
    {
        String content = textFunction.apply(selection);
        return new MessageBuilder()
            .setContent(content != null ? content : "")
            .setEmbed(new EmbedBuilder()
                .setColor(colorFunction.apply(selection))
                .setDescription(String.format(format, choices.get(selection - 1)))
                .build())
            .build();
    }

    /**
     * The Builder for a {@link com.jagrosh.jdautilities.menu.ScrollSelection ScrollSelection} Menu.
     *
     * @author Johnny_JayJay (https://github.com/johnnyjayjay)
     */
    public static class Builder extends SelectionMenu.Builder<Builder, ScrollSelection>
    {

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
            // TODO check if String only contains 1 formatter
            // Checks.check(true, "Format String must contain one String formatter (%s), no more, no less");

            return new ScrollSelection(waiter, users, roles, timeout, unit, choices,
                color, loop, singleSelectionMode, selection, cancel, text, format);
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
