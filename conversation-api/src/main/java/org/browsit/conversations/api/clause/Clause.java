package org.browsit.conversations.api.clause;

import net.kyori.adventure.text.Component;
import org.browsit.conversations.api.audience.ConversationAudience;

/**
 * @author Illusion created on 2/8/2023
 */
public interface Clause {

    /**
     * Returns whether this clause has been triggered or not.
     */
    boolean hasBeenTriggered();

    /**
     * Runs the trigger action.
     */
    void trigger(ConversationAudience audience);

    interface Ticking extends Clause {

        void tick();
    }
}
