package org.browsit.conversations.api;

import java.util.ArrayList;
import java.util.UUID;
import org.browsit.conversations.api.clause.Clause;
import org.browsit.conversations.api.util.StringValidator;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Nullable;

/**
 * @author Illusion
 * created on 2/8/2023
 */
public class Conversation {

    private final Audience audience;

    private boolean finished, echo;

    @Nullable
    private Component by, onComplete;

    @Nullable
    private ArrayList<Clause> endClauses;

    private ChatVisibility chatVisibility = ChatVisibility.ALL;

    private ArrayList<Prompt<?>> prompts;
    private Prompt<?> currentPrompt;

    /**
     * @param participant Audience that participates in this conversation.
     */
    public Conversation(UUID participant) {
        this.audience = Conversations.provider().player(participant);
    }

    /**
     * Actually executes the conversation.
     */
    public void run() {
        if (!Conversations.isRegistered(this)) {
            Conversations.registerConversation(this);
        }

        if (finished) {
            throw new IllegalStateException("Can't run finished conversation multiple times");
        }

        currentPrompt = nextPrompt();
        if (currentPrompt != null) {
            currentPrompt.display();
        }
    }

    /**
     * Ticks the conversation, used for updating timers etc.
     */
    protected void tick() {
        if (!Conversations.isRegistered(this)) {
            return;
        }
        if (finished) {
            return;
        }
        if (endClauses != null) {
            for (Clause clause : endClauses) {
                if (clause instanceof Clause.Ticking) {
                    Clause.Ticking tickingClause = (Clause.Ticking) clause;
                    tickingClause.tick();
                }

                if (clause.hasBeenTriggered()) {
                    if (clause.getTriggerMessage() != null) {
                        audience.sendMessage(clause.getTriggerMessage());
                    }
                    Conversations.endConversation(this);
                    finished = true;
                    return;
                }
            }
        }
    }

    /**
     * Adds a {@link Prompt} to the conversation.
     */
    public Conversation prompt(Prompt<?> prompt) {
        if (this.prompts == null) {
            this.prompts = new ArrayList<>();
        }

        prompt.setConversation(this);
        prompt.setId(prompts.size() + 1);
        prompts.add(prompt);
        return this;
    }

    /**
     * Specifies a clause for when this conversation should end. There's no limit to the amount of clauses you can add.
     */
    public Conversation endWhen(Clause clause) {
        if (endClauses == null) {
            this.endClauses = new ArrayList<>();
        }
        this.endClauses.add(clause);
        return this;
    }

    /**
     * The text that gets displayed when the conversation is finished/complete.
     *
     * @apiNote Can be null.
     */
    public Conversation finishingText(String component) {
        this.onComplete = LegacyComponentSerializer.legacyAmpersand().deserialize(component);
        return this;
    }

    /**
     * A name that gets prepended to each line of this conversation.
     * <p>
     * example; name = Fish: prompt = Hello
     * <p>
     * Result = Fish: Hello
     */
    public Conversation by(String name) {
        this.by = LegacyComponentSerializer.legacyAmpersand().deserialize(name);
        return this;
    }

    /**
     * Sets what is and what isn't visible through chat during the conversation.
     */
    public Conversation chatVisbility(ChatVisibility visibility) {
        this.chatVisibility = visibility;
        return this;
    }

    /**
     * Whether the input should be echo'd back to the sender.
     */
    public Conversation echo(boolean flag) {
        this.echo = flag;
        return this;
    }

    public boolean inConversation(UUID uuid) {
        Audience loneAudience = audience.filterAudience(input -> input.get(Identity.UUID).map(value -> value.equals(uuid)).orElse(false));
        return loneAudience.pointers().get(Identity.UUID).isPresent();
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean echoOn() {
        return echo;
    }

    public ChatVisibility getChatVisibility() {
        return chatVisibility;
    }

    //** INTERNAL **//
    protected @Nullable Component getBy() {
        return by;
    }

    protected Audience getAudience() {
        return audience;
    }

    protected void handleInput(String input) {
        String clean = StringValidator.clean(input);

        if (!currentPrompt.shouldHandle()) {
            Conversations.endConversation(this);
            if (currentPrompt.getAttemptsOverText() != null) {
                audience.sendMessage(currentPrompt.getAttemptsOverText());
            }
            return;
        }
        currentPrompt.handleInput(clean);
    }

    protected void next() {
        Prompt<?> next = nextPrompt();
        if (next == null) {
            Conversations.endConversation(this);
            finished = true;
            if (onComplete != null) {
                if (by != null) {
                    audience.sendMessage(by.append(Component.text(" ").append(onComplete)));
                } else {
                    audience.sendMessage(onComplete);
                }
            }
            return;
        }
        this.currentPrompt = next;
        this.currentPrompt.display();
    }


    private Prompt<?> nextPrompt() {
        int id = currentPrompt == null ? 0 : currentPrompt.getId();
        for (Prompt<?> prompt : prompts) {
            if (prompt.getId() == id + 1) {
                return prompt;
            }
        }
        return null;
    }
}
