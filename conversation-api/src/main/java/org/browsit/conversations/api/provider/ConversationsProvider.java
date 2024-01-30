package org.browsit.conversations.api.provider;

import java.util.Optional;
import java.util.UUID;
import org.browsit.conversations.api.data.Conversation;

/**
 * The ConversationsProvider is responsible for creating and managing conversations.
 * This is the main entry point for the API.
 */
public interface ConversationsProvider {

    /**
     * Cleans up the provider.
     */
    void cleanUp();

    /**
     * Creates a conversation for the given user.
     * @param userId The user to create the conversation for.
     * @return The conversation.
     */
    Conversation createConversation(UUID userId);

    /**
     * Gets the conversation of the given user.
     * @param userId The user to get the conversation of.
     * @return An optional, containing the conversation if it exists.
     */
    Optional<Conversation> getConversationOf(UUID userId);
}
