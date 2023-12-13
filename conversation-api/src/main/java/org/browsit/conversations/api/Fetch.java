package org.browsit.conversations.api;

import org.browsit.conversations.api.audience.ConversationAudience;

/**
 * @author Illusion
 * created on 2/8/2023
 */
public interface Fetch<A> {

    void execute(A input, ConversationAudience sender);
}
