package org.browsit.conversations.api;

import java.util.Optional;
import java.util.UUID;
import org.browsit.conversations.api.data.Conversation;
import org.browsit.conversations.api.provider.ConversationsProvider;

public final class Conversations {

    private static ConversationsProvider provider;

    private Conversations() {
    }

    public static void init(ConversationsProvider provider) {
        if (Conversations.provider != null) {
            throw new IllegalStateException("Conversations provider is already initialized");
        }

        Conversations.provider = provider;
    }

    public static void cleanUp() {
        validateProvider();
        Conversations.provider.cleanUp();
    }

    public static Conversation create(UUID userId) {
        validateProvider();
        return Conversations.provider.createConversation(userId);
    }

    public static Optional<Conversation> getConversationOf(UUID userId) {
        validateProvider();
        return Conversations.provider.getConversationOf(userId);
    }

    private static void validateProvider() {
        if (Conversations.provider == null) {
            throw new IllegalStateException("Conversations provider is not initialized");
        }
    }
}
