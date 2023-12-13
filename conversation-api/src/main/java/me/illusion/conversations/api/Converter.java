package me.illusion.conversations.api;

/**
 * Created by Illusion on 2/9/2023
 */
public interface Converter<A> {

    A convert(String input);
}
