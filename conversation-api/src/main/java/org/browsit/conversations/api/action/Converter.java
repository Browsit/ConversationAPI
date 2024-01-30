package org.browsit.conversations.api.action;

/**
 * @author Illusion created on 2/9/2023
 */
public interface Converter<A> {

    /**
     * Converts the input to the desired type.
     * @param input Input to convert.
     * @return The converted input, or null if the input could not be converted.
     */
    A convert(String input);
}
