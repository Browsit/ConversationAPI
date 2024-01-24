package org.browsit.conversations.impl.provider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.AudienceProvider;
import org.browsit.conversations.api.action.Prompt;
import org.browsit.conversations.api.data.Conversation;
import org.browsit.conversations.api.provider.ConversationsProvider;
import org.browsit.conversations.impl.action.PromptImpl;
import org.browsit.conversations.impl.data.ConversationImpl;

public class AdventureConversationsProvider implements ConversationsProvider {

    private final List<ConversationImpl> conversations = new CopyOnWriteArrayList<>();
    private final AudienceProvider adventureProvider;
    private final ScheduledExecutorService conversationsExecutor;

    private AdventureConversationsProvider(AudienceProvider adventureProvider) {
        this.adventureProvider = adventureProvider;
        this.conversationsExecutor = Executors.newSingleThreadScheduledExecutor();
        this.conversationsExecutor.scheduleAtFixedRate(() -> {
            try {
                this.conversations.forEach(ConversationImpl::tick);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0L, 1L, TimeUnit.MILLISECONDS);
    }

    public static AdventureConversationsProvider create(AudienceProvider audienceProvider) {
        return new AdventureConversationsProvider(audienceProvider);
    }

    @Override
    public void cleanUp() {
        try {
            if (!this.conversationsExecutor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                this.conversationsExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.conversations.clear();
    }

    @Override
    public Conversation createConversation(UUID userId) {
        Audience adventureAudience = this.adventureProvider.player(userId);
        ConversationImpl conversation = new ConversationImpl(this, adventureAudience);
        this.conversations.add(conversation);
        return conversation;
    }

    @Override
    public Optional<Conversation> getConversationOf(UUID userId) {
        return this.conversations.stream()
            .filter(conversation -> conversation.inConversation(userId))
            .findFirst()
            .map(conversation -> conversation);
    }

    public void endInternal(ConversationImpl conversation) {
        this.conversations.remove(conversation);
    }
}
