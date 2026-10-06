package org.browsit.conversations.api.audience;

import net.kyori.adventure.text.Component;

public interface ConversationAudience {

    /**
     * Sends a legacy message to the audience.
     * @param message Message to send.
     */
    void sendMessage(String message);

    /**
     * Sends an adventure message to the audience.
     * @param component Message to send.
     */
    void sendMessage(Component component);

}
