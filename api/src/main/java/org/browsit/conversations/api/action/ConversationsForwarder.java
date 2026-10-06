package org.browsit.conversations.api.action;

import java.util.UUID;
import org.browsit.conversations.api.Conversations;
import org.browsit.conversations.api.data.Conversation;

/**
 * @author Illusion created on 2/9/2023
 * <p>
 * Dummy interface to forward input coming in from platform-dependent events to {@link Conversations}.
 *
 * @param <A> The base type required to register, e.g JavaPlugin for Bukkit.
 * @param <P> The player/audience type, used to schedule input on the correct thread.
 */
public interface ConversationsForwarder<A, P> {

    /**
     * Registers this forwarder.
     *
     * @param base Base required to register, e.g JavaPlugin for Bukkit.
     */
    void register(A base);

    /**
     * Forwards the input to the sender's current {@link Conversation}, if existent.
     * <p>
     * Note: without a player reference, thread-correct scheduling (e.g. Folia) cannot be applied.
     *
     * @param onSuccess Runnable that executes when the input was forwarded succesfully.
     */
    default void forwardInput(Conversation conversation, String input, Runnable onSuccess) {
        conversation.handleInput(input);
        onSuccess.run();
    }

    /**
     * Forwards the input to the sender's current {@link Conversation}, if existent. Providing a player reference
     * allows implementations to schedule the input on the correct thread for that player.
     *
     * @param onSuccess Runnable that executes when the input was forwarded succesfully.
     */
    default void forwardInput(Conversation conversation, String input, P player, Runnable onSuccess) {
        forwardInput(conversation, input, onSuccess);
    }
}
