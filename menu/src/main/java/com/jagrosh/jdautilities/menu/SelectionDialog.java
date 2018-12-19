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
 * a listed display of text choices horizontally that users can scroll through
 * using reactions and make selections.
 *
 * @author John Grosh
 */
public class SelectionDialog extends SelectionMenu
{
    /**
     * @deprecated Use {@link com.jagrosh.jdautilities.menu.SelectionMenu#UP this} instead
     */
    @Deprecated
    public static final String UP = "\uD83D\uDD3C";

    /**
     * @deprecated Use {@link com.jagrosh.jdautilities.menu.SelectionMenu#DOWN this} instead
     */
    @Deprecated
    public static final String DOWN = "\uD83D\uDD3D";

    /**
     * @deprecated Use {@link com.jagrosh.jdautilities.menu.SelectionMenu#SELECT this} instead
     */
    @Deprecated
    public static final String SELECT = "\u2705";

    /**
     * @deprecated Use {@link com.jagrosh.jdautilities.menu.SelectionMenu#CANCEL this} instead
     */
    @Deprecated
    public static final String CANCEL = "\u274E";

    private final String leftEnd, rightEnd;
    private final String defaultLeft, defaultRight;

    
    private SelectionDialog(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
                    List<String> choices, String leftEnd, String rightEnd, String defaultLeft, String defaultRight,
                    Function<Integer,Color> color, boolean loop, BiConsumer<Message, Integer> success,
                    Consumer<Message> cancel, Function<Integer,String> text, boolean singleSelectionMode)
    {
        super(waiter, users, roles, timeout, unit, choices, loop, singleSelectionMode, cancel, color, text, success,
            Arrays.asList(SelectionMenu.SELECT, SelectionMenu.UP, SelectionMenu.DOWN, SelectionMenu.CANCEL));
        this.leftEnd = leftEnd;
        this.rightEnd = rightEnd;
        this.defaultLeft = defaultLeft;
        this.defaultRight = defaultRight;
    }

    /**
     * Constructor for backwards compatibility (calls new constructor with singleSelectionMode = false)
     * @deprecated Use Constructor with extra boolean {@code singleSelectionMode} instead
     */
    @Deprecated
    SelectionDialog(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
                    List<String> choices, String leftEnd, String rightEnd, String defaultLeft, String defaultRight,
                    Function<Integer,Color> color, boolean loop, BiConsumer<Message, Integer> success,
                    Consumer<Message> cancel, Function<Integer,String> text)
    {
        this(waiter, users, roles, timeout, unit, choices, leftEnd, rightEnd, defaultLeft, defaultRight, color, loop, success, cancel, text, false);
    }

    @Override
    protected Message render(int selection)
    {
        StringBuilder sbuilder = new StringBuilder();
        for(int i=0; i<choices.size(); i++)
            if(i+1==selection)
                sbuilder.append("\n").append(leftEnd).append(choices.get(i)).append(rightEnd);
            else
                sbuilder.append("\n").append(defaultLeft).append(choices.get(i)).append(defaultRight);
        MessageBuilder mbuilder = new MessageBuilder();
        String content = textFunction.apply(selection);
        if(content!=null)
            mbuilder.append(content);
        return mbuilder.setEmbed(new EmbedBuilder()
                .setColor(colorFunction.apply(selection))
                .setDescription(sbuilder.toString())
                .build()).build();
    }

    /**
     * The {@link com.jagrosh.jdautilities.menu.Menu.Builder Menu.Builder} for
     * a {@link com.jagrosh.jdautilities.menu.SelectionDialog SelectionDialog}.
     *
     * @author John Grosh
     */
    public static class Builder extends SelectionMenu.Builder<Builder, SelectionDialog>
    {

        private String leftEnd = "";
        private String rightEnd  = "";
        private String defaultLeft = "";
        private String defaultRight = "";

        /**
         * Builds the {@link com.jagrosh.jdautilities.menu.SelectionDialog SelectionDialog}
         * with this Builder.
         *
         * @return The SelectionDialog built from this Builder.
         *
         * @throws java.lang.IllegalArgumentException
         *         If one of the following is violated:
         *         <ul>
         *             <li>No {@link com.jagrosh.jdautilities.commons.waiter.EventWaiter EventWaiter} was set.</li>
         *             <li>No choices were set.</li>
         *             <li>No action {@link java.util.function.BiConsumer BiConsumer} was set.</li>
         *         </ul>
         */
        @Override
        public SelectionDialog build()
        {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!choices.isEmpty(), "Must have at least one choice");
            Checks.check(selection != null, "Must provide a selection consumer");

            return new SelectionDialog(waiter, users, roles, timeout, unit, choices, leftEnd, rightEnd,
                    defaultLeft, defaultRight, color, loop, selection, cancel, text, singleSelectionMode);
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

    }
}
