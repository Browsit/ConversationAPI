package org.browsit.conversations.api.action;

import org.browsit.conversations.api.audience.ConversationAudience;

/**
 * @author Illusion created on 2/8/2023
 */
public interface Fetch<A> {

    /**
     * Runs an action with the converted input.
     * @param input converted input
     * @param sender sender of the input
     */
    void execute(A input, ConversationAudience sender);
}
