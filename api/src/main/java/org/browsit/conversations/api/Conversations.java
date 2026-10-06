package org.browsit.conversations.api;

import java.util.Optional;
import java.util.UUID;
import org.browsit.conversations.api.data.Conversation;
import org.browsit.conversations.api.provider.ConversationsProvider;

/**
 * The Conversations class is the main entry point for the API.
 */
public final class Conversations {

    private static ConversationsProvider provider;

    private Conversations() {
    }

    /**
     * Initializes the Conversations provider. This should only be called once.
     * @throws IllegalStateException If the provider is already initialized.
     * @param provider The provider to initialize.
     */
    public static void init(ConversationsProvider provider) {
        if (Conversations.provider != null) {
            throw new IllegalStateException("Conversations provider is already initialized");
        }

        Conversations.provider = provider;
    }

    /**
     * Cleans up the provider.
     * @throws IllegalStateException If the provider is not initialized.
     */
    public static void cleanUp() {
        validateProvider();
        Conversations.provider.cleanUp();
    }

    /**
     * Creates a conversation for the given user.
     * @param userId The user to create the conversation for.
     * @throws IllegalStateException If the provider is not initialized.
     * @return The conversation.
     */
    public static Conversation create(UUID userId) {
        validateProvider();
        return Conversations.provider.createConversation(userId);
    }

    /**
     * Gets the conversation of the given user.
     * @param userId The user to get the conversation of.
     * @throws IllegalStateException If the provider is not initialized.
     * @return An optional, containing the conversation if it exists.
     */
    public static Optional<Conversation> getConversationOf(UUID userId) {
        validateProvider();
        return Conversations.provider.getConversationOf(userId);
    }

    /**
     * Validates that the provider is initialized.
     * @throws IllegalStateException If the provider is not initialized.
     */
    private static void validateProvider() {
        if (Conversations.provider == null) {
            throw new IllegalStateException("Conversations provider is not initialized");
        }
    }
}
