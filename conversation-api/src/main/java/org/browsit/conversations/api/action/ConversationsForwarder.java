package org.browsit.conversations.api.action;

import java.util.UUID;
import org.browsit.conversations.api.data.Conversation;
import org.browsit.conversations.api.Conversations;


/**
 * @author Illusion created on 2/9/2023
 * <p>
 * Dummy interface to forward input coming in from platform-dependent events to {@link Conversations}.
 */
public interface ConversationsForwarder<A> {

    /**
     * Registers this forwarder.
     *
     * @param base Base required to register, e.g JavaPlugin for Bukkit.
     */
    void register(A base);

    /**
     * Forwards the input to the sender's current {@link Conversation}, if existent.
     *
     * @param onSuccess Runnable that executes when the input was forwarded succesfully.
     */
    default void forwardInput(Conversation conversation, UUID sender, String input, Runnable onSuccess) {
        conversation.handleInput(input);
        onSuccess.run();
    }
}
