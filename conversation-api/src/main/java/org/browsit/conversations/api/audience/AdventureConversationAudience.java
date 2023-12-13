package org.browsit.conversations.api.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class AdventureConversationAudience implements ConversationAudience {

    private final Audience adventureAudience;

    public AdventureConversationAudience(Audience adventureAudience) {
        this.adventureAudience = adventureAudience;
    }

    @Override
    public void sendMessage(String message) {
        sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }

    @Override
    public void sendMessage(Component component) {
        adventureAudience.sendMessage(component);
    }
}
