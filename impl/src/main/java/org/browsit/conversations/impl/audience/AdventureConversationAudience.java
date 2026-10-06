package org.browsit.conversations.impl.audience;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.browsit.conversations.api.audience.ConversationAudience;

public class AdventureConversationAudience implements ConversationAudience {

    private final Audience adventureAudience;

    public AdventureConversationAudience(Audience adventureAudience) {
        this.adventureAudience = adventureAudience;
    }

    @Override
    public void sendMessage(String message) {
        if (message == null) {
            return;
        }

        this.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }

    @Override
    public void sendMessage(Component component) {
        if (component == null) {
            return;
        }

        this.adventureAudience.sendMessage(component);
    }
}
