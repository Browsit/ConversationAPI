package org.browsit.conversations.api.data;

import java.util.UUID;
import java.util.function.Consumer;
import org.browsit.conversations.api.action.Prompt;
import org.browsit.conversations.api.audience.ConversationAudience;
import org.browsit.conversations.api.clause.Clause;

public interface Conversation {

    /**
     * Adds a {@link Prompt} to the conversation.
     *
     * @param name   The name of the prompt.
     * @param type   The complex type class of the prompt.
     * @param prompt An action that gets executed when the prompt is created.
     * @param <T>    The complex type of the prompt.
     * @return The conversation.
     */
    <T> Conversation prompt(String name, Class<T> type, Consumer<Prompt<T>> prompt);

    /**
     * Specifies a clause for when this conversation should end. There's no limit to the amount of clauses you can add.
     *
     * @param clause The clause.
     * @return The conversation.
     */
    Conversation endWhen(Clause clause);

    /**
     * Toggles "echo" on or off. This determines whether the player sees their own messages or not. Defaults to false.
     *
     * @param echo Whether to echo or not.
     * @return The conversation.
     */
    Conversation echo(boolean echo);

    /**
     * Sets the chat visibility of the conversation.
     *
     * @param chatVisibility The chat visibility.
     * @return The conversation.
     */
    Conversation chatVisibility(ChatVisibility chatVisibility);

    /**
     * A name that gets prepended to each line of this conversation.
     * <p>
     * example; name = Fish: prompt = Hello
     * <p>
     * Result = Fish: Hello
     * <p>
     *
     * @param name The name.
     * @return The conversation.
     */
    Conversation by(String name);

    /**
     * The text that gets displayed when the conversation is finished/complete.
     *
     * @param text The text to display.
     * @return The conversation.
     * @apiNote Can be null.
     */
    Conversation finishingText(String text);

    /**
     * Gets the chat visibility of the conversation.
     *
     * @return The chat visibility, ALL by default.
     */
    ChatVisibility getChatVisibility();

    /**
     * Gets the echo status of the conversation.
     *
     * @return The echo status, FALSE by default.
     */
    boolean echoOn();

    /**
     * Handles the input of the player. This is called when the player sends a message.
     *
     * @param input The input of the player.
     */
    void handleInput(String input);

    /**
     * Advances the conversation to the next prompt.
     */
    void next();

    /**
     * Actually executes the conversation.
     */
    void start();

    /**
     * Checks if the conversation is finished.
     *
     * @return TRUE if the conversation is finished, FALSE otherwise.
     */
    boolean isFinished();

    /**
     * Forcefully ends the conversation.
     */
    void finish();

    /**
     * Checks if the player is in the conversation.
     *
     * @param userId The UUID of the player.
     * @return TRUE if the player is in the conversation, FALSE otherwise.
     */
    boolean inConversation(UUID userId);

    /**
     * Gets the audience of the conversation.
     *
     * @return The audience.
     */
    ConversationAudience getAudience();

}
