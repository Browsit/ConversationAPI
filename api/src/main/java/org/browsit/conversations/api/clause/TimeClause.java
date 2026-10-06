package org.browsit.conversations.api.clause;

import net.kyori.adventure.text.Component;
import org.browsit.conversations.api.audience.ConversationAudience;
import org.jetbrains.annotations.Nullable;

/**
 * @author Illusion created on 2/9/2023
 */
public abstract class TimeClause implements Clause.Ticking {

    private final long max;
    private long current;

    private TimeClause(long millis) {
        this.max = millis;
    }

    public static Clause.Ticking create(long millis, @Nullable Component clauseTriggerMsg) {
        return new ComponentTimeClause(millis, clauseTriggerMsg);
    }

    public static Clause.Ticking create(long millis, @Nullable String clauseTriggerMsg) {
        return new StringTimeClause(millis, clauseTriggerMsg);
    }

    @Override
    public void tick() {
        this.current++;
    }

    @Override
    public boolean hasBeenTriggered() {
        return this.current >= this.max;
    }

    private static class StringTimeClause extends TimeClause {

        private final String triggerMessage;

        private StringTimeClause(long millis, String triggerMessage) {
            super(millis);
            this.triggerMessage = triggerMessage;
        }

        @Override
        public void trigger(ConversationAudience audience) {
            audience.sendMessage(this.triggerMessage);
        }
    }

    private static class ComponentTimeClause extends TimeClause {
        private final Component triggerMessage;

        private ComponentTimeClause(long millis, Component triggerMessage) {
            super(millis);
            this.triggerMessage = triggerMessage;
        }

        @Override
        public void trigger(ConversationAudience audience) {
            audience.sendMessage(this.triggerMessage);
        }
    }
}
