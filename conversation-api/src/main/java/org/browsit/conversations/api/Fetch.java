package me.illusion.conversations.api;

import net.kyori.adventure.audience.Audience;

/**
 * @author Illusion
 * created on 2/8/2023
 */
public interface Fetch<A> {

    void execute(A input, Audience sender);
}
