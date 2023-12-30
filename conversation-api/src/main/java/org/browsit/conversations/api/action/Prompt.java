package org.browsit.conversations.api.action;

import java.util.function.Predicate;
import org.browsit.conversations.api.Conversations;
import org.browsit.conversations.api.util.Constants;
import org.jetbrains.annotations.NotNull;

public interface Prompt<A> {
    
    static <A> Prompt<A> createPrompt(String name) {
        return Conversations.createPrompt(name);
    }

    /**
     * The handler/converter which takes the String input and converts it into the required type before passing it down to a {@link #filter(Predicate)})}.
     *
     * @apiNote Can't be null.
     */
    Prompt<A> converter(@NotNull Converter<A> stringConverter);

    /**
     * The filter allows you to take the converted input and validate it, before it will be passed down to {@link #fetch(Fetch)} (Fetch)}.
     * <p>
     * e.g: Check if age > 18.
     *
     * @apiNote Can be null.
     */
    Prompt<A> filter(Predicate<A> inputFilter);

    /**
     * The last pass, here is where you actually define what you want to do with the given input. This happens after the input has been converted, and if
     * necessary filtered.
     */
    Prompt<A> fetch(Fetch<A> input);

    /**
     * The max. attempts the input giver will have before the prompt gets cancelled.
     */
    Prompt<A> attempts(int maxAttempts);

    /**
     * The error message that gets displayed when the {@link #converter(Converter)} fails.
     * <p>
     * e.g Invalid input, only numbers are allowed.
     *
     * @apiNote Can be null, will default to {@link Constants#INVALID_INPUT_MESSAGE}.
     */
    Prompt<A> conversionFailText(@NotNull String text);

    /**
     * The error message that gets displayed when the {@link #filter(Predicate)} fails.
     * <p>
     * e.g A name can't contain special characters
     *
     * @apiNote Can be null, will default to {@link Constants#INVALID_INPUT_MESSAGE}.
     */
    Prompt<A> filterFailText(@NotNull String text);

    /**
     * The error message that gets displayed when the sender has ran out of attempts.
     * <p>
     * e.g You've run out of attempts!
     *
     * @apiNote If null, won't display anything.
     */
    Prompt<A> allAttemptsFailedText(@NotNull String text);
}
