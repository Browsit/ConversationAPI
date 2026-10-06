package org.browsit.conversations.api.util;

import net.kyori.adventure.text.Component;

/**
 * @author Illusion created on 2/9/2023
 */
public final class Constants {

    private Constants() {
    }

    /**
     * The default message that gets displayed when the user enters an invalid input.
     */
    public static final Component INVALID_INPUT_MESSAGE = Component.text("Invalid input, try again.");

    /**
     * The default message that gets displayed when the user enters an input that can't be converted.
     */
    public static final Component CONVERSION_FAILED_MESSAGE = Component.text("Conversion failed, try again.");

    /**
     * The default message that gets displayed when the user runs out of attempts.
     */
    public static final Component ATTEMPTS_OVER_MESSAGE = Component.text("You've run out of attempts.");
}
