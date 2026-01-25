package org.browsit.conversations.impl.action;

import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.browsit.conversations.api.action.Converter;
import org.browsit.conversations.api.action.Fetch;
import org.browsit.conversations.api.action.Prompt;
import org.browsit.conversations.api.audience.ConversationAudience;
import org.browsit.conversations.api.util.Constants;
import org.browsit.conversations.impl.data.ConversationImpl;
import org.jetbrains.annotations.NotNull;

/**
 * @author Illusion created on 2/8/2023
 */
public class PromptImpl<A> implements Prompt<A> {

    private final ConversationImpl conversation;
    private final Component text;
    private int id;

    private int attempts = 3;
    private int currentAttempt;
    private boolean complete;

    private Fetch<A> inputHandler;
    private Predicate<A> inputFilter;
    private Converter<A> stringConverter;
    private Component conversionFailedText = Constants.CONVERSION_FAILED_MESSAGE;
    private Component filterFailedText = Constants.INVALID_INPUT_MESSAGE;
    private Component attemptsOverText = Constants.ATTEMPTS_OVER_MESSAGE;

    public PromptImpl(String text, ConversationImpl impl) {
        this.text = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        this.conversation = impl;
    }

    /**
     * The handler/converter which takes the String input and converts it into the required type before passing it down to a {@link #filter(Predicate)})}.
     *
     * @apiNote Can't be null.
     */
    @Override
    public PromptImpl<A> converter(@NotNull Converter<A> stringConverter) {
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
    @Override
    public PromptImpl<A> filter(Predicate<A> inputFilter) {
        this.inputFilter = inputFilter;
        return this;
    }

    /**
     * The last pass, here is where you actually define what you want to do with the given input. This happens after the input has been converted, and if
     * necessary filtered.
     */
    @Override
    public PromptImpl<A> fetch(Fetch<A> input) {
        this.inputHandler = input;
        return this;
    }

    /**
     * The max. attempts the input giver will have before the prompt gets cancelled.
     */
    @Override
    public PromptImpl<A> attempts(int maxAttempts) {
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
    @Override
    public PromptImpl<A> conversionFailText(@NotNull String text) {
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
    @Override
    public PromptImpl<A> filterFailText(@NotNull String text) {
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
    @Override
    public PromptImpl<A> allAttemptsFailedText(@NotNull String text) {
        this.attemptsOverText = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        return this;
    }

    // ** INTERAL METHODS **//

    public void display() {
        if (this.conversation.getBy() != null) {
            this.conversation.getAudience().sendMessage(this.conversation.getBy().append(Component.text(" ")).append(this.text));
            return;
        }
        this.conversation.getAudience().sendMessage(this.text);
    }

    public void handleInput(String input) {
        this.currentAttempt++;

        final ConversationAudience audience = this.conversation.getAudience();

        final A converted;
        try {
            converted = this.stringConverter.convert(input);
        } catch (Exception expected) {
            audience.sendMessage(this.conversionFailedText);
            return;
        }

        if (converted == null) {
            audience.sendMessage(this.conversionFailedText);
            return;
        }

        if (this.inputFilter != null) {
            if (this.inputFilter.test(converted)) {
                this.inputHandler.execute(converted, audience);
                this.complete = true;
                this.conversation.next();
                return;
            }
            audience.sendMessage(this.filterFailedText);
            return;
        }
        this.inputHandler.execute(converted, audience);
        this.complete = true;
        this.conversation.next();
    }

    public boolean shouldHandle() {
        return this.currentAttempt < this.attempts;
    }

    public Component getAttemptsOverText() {
        return this.attemptsOverText;
    }

    protected boolean isComplete() {
        return this.complete;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
