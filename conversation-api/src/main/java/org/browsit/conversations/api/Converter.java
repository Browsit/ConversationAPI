package org.browsit.conversations.api;

/**
 * @author Illusion
 * created on 2/9/2023
 */
public interface Converter<A> {

    A convert(String input);
}
