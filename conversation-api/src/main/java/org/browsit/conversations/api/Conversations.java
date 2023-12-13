package org.browsit.conversations.api;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.platform.AudienceProvider;
import org.browsit.conversations.api.data.Conversation;

/**
 * @author Illusion created on 2/9/2023
 */
public final class Conversations {

    private static CopyOnWriteArrayList<Conversation> conversations;
    private static AudienceProvider audienceProvider;
    private static ScheduledExecutorService conversationsExecutor;
    private static boolean initialized;

    /**
     * Initalizes the Conversations API.
     */
    public static void init(AudienceProvider provider) {
        if (initialized) {
            throw new IllegalStateException("Conversations API already initialized");
        }

        conversations = new CopyOnWriteArrayList<>();
        audienceProvider = provider;
        conversationsExecutor = Executors.newSingleThreadScheduledExecutor();
        conversationsExecutor.scheduleAtFixedRate(() -> {
            try {
                conversations.forEach(Conversation::tick);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0L, 1L, TimeUnit.MILLISECONDS);
        initialized = true;
    }

    /**
     * Cleans up the Conversations API.
     */
    public static void cleanUp() {
        if (!initialized) {
            throw new IllegalStateException("Conversations API not initialized");
        }
        try {
            if (!conversationsExecutor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                conversationsExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        conversations.clear();
        initialized = false;
    }

    public static void endConversation(Conversation conversation) {
        if (conversation == null) {
            throw new IllegalStateException("Conversations API not initialized");
        }
        if (!isRegistered(conversation)) {
            return;
        }
        conversation.setFinished(true);
        conversations.remove(conversation);
    }

    public static boolean isRegistered(Conversation conversation) {
        return conversations.contains(conversation);
    }

    public static void registerConversation(Conversation conversation) {
        if (conversation == null) {
            throw new IllegalStateException("Conversations API not initialized");
        }
        if (!isRegistered(conversation)) {
            conversations.add(conversation);
        }
    }

    public static Optional<Conversation> getConversationOf(UUID playerId) {
        for (final Conversation conversation : conversations) {
            if (conversation.inConversation(playerId)) {
                return Optional.of(conversation);
            }
        }
        return Optional.empty();
    }

    public static AudienceProvider provider() {
        return audienceProvider;
    }
}
