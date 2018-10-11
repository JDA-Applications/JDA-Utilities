package com.jagrosh.jdautilities.menu;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.requests.RestAction;

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
 * A {@link com.jagrosh.jdautilities.menu.Menu Menu} extension made for menus that provides
 * a List of Strings to choose from (via reactions).
 *
 * <p>Implementations of this class should also have a nested Builder class which extends
 * {@link com.jagrosh.jdautilities.menu.SelectionMenu.Builder}.
 *
 * @see   com.jagrosh.jdautilities.menu.SelectionDialog
 * @see   com.jagrosh.jdautilities.menu.ScrollSelection
 *
 * @author John Grosh / Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
public abstract class SelectionMenu extends Menu
{

    public static final String UP = "\uD83D\uDD3C";
    public static final String DOWN = "\uD83D\uDD3D";
    public static final String SELECT = "\u2705";
    public static final String CANCEL = "\u274E";

    protected final boolean loop;
    protected final boolean singleSelectionMode;
    protected final List<String> choices;
    protected final BiConsumer<Message, Integer> success;
    protected final Consumer<Message> cancel;
    protected final Function<Integer, Color> colorFunction;
    protected final Function<Integer, String> textFunction;

    private final List<String> reactions;


    protected SelectionMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
                            List<String> choices, boolean loop, boolean singleSelectionMode, Consumer<Message> cancel, Function<Integer, Color> colorFunction,
                            Function<Integer, String> textFunction, BiConsumer<Message, Integer> success, List<String> reactions)
    {
        super(waiter, users, roles, timeout, unit);
        this.choices = choices;
        this.loop = loop;
        this.singleSelectionMode = singleSelectionMode;
        this.cancel = cancel;
        this.colorFunction = colorFunction;
        this.textFunction = textFunction;
        this.success = success;
        this.reactions = reactions;
    }

    /**
     * Constructor for backwards compatibility (calls new constructor with singleSelectionMode = false)
     * @deprecated Use Constructor with extra boolean {@code singleSelectionMode} instead
     */
    @Deprecated
    protected SelectionMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
                  List<String> choices, boolean loop, Consumer<Message> cancel, Function<Integer, Color> colorFunction,
                  Function<Integer, String> textFunction, BiConsumer<Message, Integer> success, List<String> reactions)
    {
        this(waiter, users, roles, timeout, unit, choices, loop, false, cancel, colorFunction, textFunction, success, reactions);
    }

    /**
     * Shows the SelectionMenu as a new {@link net.dv8tion.jda.core.entities.Message Message}
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
     * Displays this SelectionMenu by editing the provided
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
     * Shows the SelectionMenu as a new {@link net.dv8tion.jda.core.entities.Message Message}
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
     * Displays this SelectionMenu by editing the provided
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
                reactions.forEach((s) -> m.addReaction(s).queue());
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
        waiter.waitForEvent(MessageReactionAddEvent.class, event ->
            event.getMessageIdLong() == message.getIdLong()
            && reactions.contains(event.getReactionEmote().getName())
            && isValidUser(event.getUser()),
            event -> {

            // get the new selection based on the emoji and the current selection
            int newSelection = getNewSelection(message, event.getReactionEmote().getName(), selection);

            // no new selection should be made (e.g. if the user cancelled)
            if (newSelection == -1)
                return;

            // if this is on a guild and the self member has permission to, remove the reaction
            Guild guild = event.getGuild();
            if (guild != null && guild.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE))
                event.getReaction().removeReaction(event.getUser()).queue();

            message.editMessage(render(newSelection)).queue(m -> selectionDialog(m, newSelection)); // display new selection and wait for the next one
        }, timeout, unit, () -> cancel.accept(message));
    }

    /**
     * Evaluates the next selection based on the previous one and the emoji reacted with.
     * <br>This handles the default reactions (UP, DOWN, SELECT, CANCEL), but may be overwritten
     * for additional reactions used by an implementation of this class.
     *
     * @param  message
     *         The {@link net.dv8tion.jda.core.entities.Message Message} this menu is being displayed in
     *
     * @param  emoji
     *         The emoji the user reacted with (in unicode)
     *
     * @param  selection
     *         The previous selection (before they reacted)
     *
     * @return A new selection that may differ from this default method
     */
    protected int getNewSelection(Message message, String emoji, int selection)
    {
        switch(emoji)
        {
            case UP:
                if(selection>1)
                    selection--;
                else if(loop)
                    selection = choices.size();
                break;
            case DOWN:
                if(selection<choices.size())
                    selection++;
                else if(loop)
                    selection = 1;
                break;
            case SELECT:
                success.accept(message, selection);
                if(singleSelectionMode)
                    selection = -1;
                break;
            case CANCEL:
                cancel.accept(message);
                selection = -1;
        }

        return selection;
    }

    /**
     * A method to overwrite when making implementations of this abstract class.
     * <br>This method provides the message to be displayed based on the current selection of this Menu.
     *
     * @param  selection
     *         The currently selected choice. This is always 1 more than the corresponding index of the {@code choices}-List.
     *         E.g.: when the last entry is selected, {@code selection = choices.size()}.
     *
     * @return The {@link net.dv8tion.jda.core.entities.Message Message} to be displayed
     */
    protected abstract Message render(int selection);

    /**
     * An extendable frame for a chain-method builder that constructs a specified type of
     * {@link com.jagrosh.jdautilities.menu.SelectionMenu SelectionMenu}.<p>
     *
     * Every extension of this class should have a nested class
     * <br>{@literal public static class Builder extends SelectionMenu.Builder<Builder, MySelectionMenu>}.
     *
     * @param  <T>
     *         The Builder type. Usually, this is just the type of this Builder's extension.
     *
     * @param  <V>
     *         The {@link com.jagrosh.jdautilities.menu.SelectionMenu SelectionMenu} to be built.
     *         Usually, this is the enclosing class of this Builder.
     */
    @SuppressWarnings("unchecked")
    public abstract static class Builder<T extends Builder<T, V>, V extends SelectionMenu> extends Menu.Builder<T, V>
    {
        protected final List<String> choices = new ArrayList<>();

        protected boolean loop = true;
        protected boolean singleSelectionMode = false;
        protected Function<Integer, String> text = (i) -> null;
        protected Function<Integer, Color> color = (i) -> null;
        protected BiConsumer<Message, Integer> selection = null;
        protected Consumer<Message> cancel = (m) -> {};


        /**
         * Sets if the Menu should exit when a selection was made.
         * By default, this is false and the menu continues showing choices even after a selection was made.
         *
         * @param  singleSelectionMode
         *         {@code true} if the menu should exit after the first selection being made
         *
         * @return This builder
         */
        public Builder useSingleSelectionMode(boolean singleSelectionMode)
        {
            this.singleSelectionMode = singleSelectionMode;
            return this;
        }

        /**
         * Sets the {@link java.awt.Color Color} of the {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed}.
         *
         * @param  color
         *         The Color of the MessageEmbed
         *
         * @return This builder
         */
        public T setColor(Color color)
        {
            this.color = i -> color;
            return (T) this;
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
        public T setColor(Function<Integer, Color> color)
        {
            this.color = color;
            return (T) this;
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
        public T setText(String text)
        {
            this.text = i -> text;
            return (T) this;
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
        public T setText(Function<Integer, String> text)
        {
            this.text = text;
            return (T) this;
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
        public T useLooping(boolean loop)
        {
            this.loop = loop;
            return (T) this;
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
        public T setSelectionConsumer(BiConsumer<Message, Integer> selection)
        {
            this.selection = selection;
            return (T) this;
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
        public T setCanceled(Consumer<Message> cancel)
        {
            this.cancel = cancel;
            return (T) this;
        }

        /**
         * Clears the choices to be shown.
         *
         * @return This builder
         */
        public T clearChoices()
        {
            this.choices.clear();
            return (T) this;
        }

        /**
         * Sets the String choices to be shown as selections.
         *
         * @param  choices
         *         The String choices to show
         * @return This builder
         */
        public T setChoices(List<String> choices)
        {
            this.choices.clear();
            this.choices.addAll(choices);
            return (T) this;
        }

        /**
         * Sets the String choices to be shown as selections.
         *
         * @param  choices
         *         The String choices to show
         * @return This builder
         */
        public T setChoices(String... choices)
        {
            return this.setChoices(Arrays.asList(choices));
        }

        /**
         * Adds String choices to be shown as selections.
         *
         * @param  choices
         *         The String choices to add
         *
         * @return This builder
         */
        public T addChoices(String... choices)
        {
            this.choices.addAll(Arrays.asList(choices));
            return (T) this;
        }

    }
}
