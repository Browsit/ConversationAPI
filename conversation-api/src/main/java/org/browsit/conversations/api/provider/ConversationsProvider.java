package org.browsit.conversations.api.provider;

import java.util.Optional;
import java.util.UUID;
import org.browsit.conversations.api.data.Conversation;

public interface ConversationsProvider {

    void cleanUp();

    Conversation createConversation(UUID userId);
    Optional<Conversation> getConversationOf(UUID userId);
}
