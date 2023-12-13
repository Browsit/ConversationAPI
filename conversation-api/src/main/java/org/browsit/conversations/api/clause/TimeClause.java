package org.browsit.conversations.api.clause;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * @author Illusion
 * created on 2/9/2023
 */
public class TimeClause implements Clause.Ticking {

    private final Component triggerMessage;
    private final long max;
    private long current;

    public TimeClause(long millis, @Nullable Component clauseTriggerMsg) {
        this.max = millis;
        this.triggerMessage = clauseTriggerMsg;
    }

    @Override
    public void tick() {
        current++;
    }

    @Override
    public boolean hasBeenTriggered() {
        return current >= max;
    }

    @Override
    @Nullable
    public Component getTriggerMessage() {
        return triggerMessage;
    }
}
