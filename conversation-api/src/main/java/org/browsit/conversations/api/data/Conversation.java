package org.browsit.conversations.api.data;

import java.util.UUID;
import org.browsit.conversations.api.action.Prompt;
import org.browsit.conversations.api.audience.ConversationAudience;
import org.browsit.conversations.api.clause.Clause;

public interface Conversation {

    Conversation prompt(Prompt<?> prompt);
    Conversation endWhen(Clause clause);
    Conversation echo(boolean echo);
    Conversation chatVisibility(ChatVisibility chatVisibility);
    Conversation by(String name);
    Conversation finishingText(String text);

    ChatVisibility getChatVisibility();
    boolean echoOn();

    void handleInput(String input);
    void next();

    void start();
    boolean isFinished();
    void finish();

    boolean inConversation(UUID userId);

    ConversationAudience getAudience();

}
