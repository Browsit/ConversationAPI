package org.browsit.conversations.api.audience;

import net.kyori.adventure.text.Component;

public interface ConversationAudience {

    void sendMessage(String message);

    void sendMessage(Component component);

}
