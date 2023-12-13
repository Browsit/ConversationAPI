package org.browsit.conversations.api;

import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.browsit.conversations.api.util.Constants;
import org.jetbrains.annotations.NotNull;

/**
 * @author Illusion created on 2/8/2023
 */
public class Prompt<A> {

    private final Component text;
    private int id;

    private int attempts = 3;
    private int currentAttempt;
    private boolean complete;

    private Conversation conversation;
    private Fetch<A> inputHandler;
    private Predicate<A> inputFilter;
    private Converter<A> stringConverter;
    private Component conversionFailedText, filterFailedText = Constants.INVALID_INPUT_MESSAGE;
    private Component attemptsOverText;

    public Prompt(String text) {
        this.text = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    /**
     * The handler/converter which takes the String input and converts it into the required type before passing it down to a {@link #filter(Predicate)})}.
     *
     * @apiNote Can't be null.
     */
    public Prompt<A> converter(@NotNull Converter<A> stringConverter) {
        this.stringConverter = stringConverter;
        return this;
    }

    /**
     * The filter allows you to take the converted input and validate it, before it will be passed down to {@link #fetch(Fetch)} (Fetch)}.
     * <p>
     * e.g: Check if age > 18.
     *
     * @apiNote Can be null.
     */
    public Prompt<A> filter(Predicate<A> inputFilter) {
        this.inputFilter = inputFilter;
        return this;
    }

    /**
     * The last pass, here is where you actually define what you want to do with the given input. This happens after the input has been converted, and if
     * necessary filtered.
     */
    public Prompt<A> fetch(Fetch<A> input) {
        this.inputHandler = input;
        return this;
    }

    /**
     * The max. attempts the input giver will have before the prompt gets cancelled.
     */
    public Prompt<A> attempts(int maxAttempts) {
        this.attempts = maxAttempts;
        return this;
    }

    /**
     * The error message that gets displayed when the {@link #converter(Converter)} fails.
     * <p>
     * e.g Invalid input, only numbers are allowed.
     *
     * @apiNote Can be null, will default to {@link Constants#INVALID_INPUT_MESSAGE}.
     */
    public Prompt<A> conversionFailText(@NotNull String text) {
        this.conversionFailedText = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        return this;
    }

    /**
     * The error message that gets displayed when the {@link #filter(Predicate)} fails.
     * <p>
     * e.g A name can't contain special characters
     *
     * @apiNote Can be null, will default to {@link Constants#INVALID_INPUT_MESSAGE}.
     */
    public Prompt<A> filterFailText(@NotNull String text) {
        this.filterFailedText = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        return this;
    }

    /**
     * The error message that gets displayed when the sender has ran out of attempts.
     * <p>
     * e.g You've run out of attempts!
     *
     * @apiNote If null, won't display anything.
     */
    public Prompt<A> allAttemptsFailedText(@NotNull String text) {
        this.attemptsOverText = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        return this;
    }

    // ** INTERAL METHODS **//
    protected void display() {
        if (conversation.getBy() != null) {
            conversation.getAudience().sendMessage(conversation.getBy().append(Component.text(" ")).append(text));
            return;
        }
        conversation.getAudience().sendMessage(text);
    }

    protected void handleInput(String input) {
        currentAttempt++;

        A converted;
        try {
            converted = stringConverter.convert(input);
        } catch (Exception e) {
            conversation.getAudience().sendMessage(conversionFailedText);
            return;
        }

        if (converted == null) {
            conversation.getAudience().sendMessage(conversionFailedText);
            return;
        }

        if (inputFilter != null) {
            if (inputFilter.test(converted)) {
                inputHandler.execute(converted, conversation.getWrappedAudience());
                complete = true;
                conversation.next();
                return;
            }
            conversation.getAudience().sendMessage(filterFailedText);
            return;
        }
        inputHandler.execute(converted, conversation.getWrappedAudience());
        complete = true;
        conversation.next();
    }

    protected boolean shouldHandle() {
        return currentAttempt < attempts;
    }

    protected Component getAttemptsOverText() {
        return attemptsOverText;
    }

    protected boolean isComplete() {
        return complete;
    }

    protected int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    protected void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }
}
