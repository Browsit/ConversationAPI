package org.browsit.conversations.impl.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.browsit.conversations.api.action.Prompt;
import org.browsit.conversations.api.audience.AdventureConversationAudience;
import org.browsit.conversations.api.audience.ConversationAudience;
import org.browsit.conversations.api.clause.Clause;
import org.browsit.conversations.api.data.ChatVisibility;
import org.browsit.conversations.api.data.Conversation;
import org.browsit.conversations.api.util.StringValidator;
import org.browsit.conversations.impl.action.PromptImpl;
import org.browsit.conversations.impl.provider.AdventureConversationsProvider;
import org.jetbrains.annotations.Nullable;

/**
 * @author Illusion created on 2/8/2023
 */
public class ConversationImpl implements Conversation {

    private final AdventureConversationsProvider provider;

    private final Audience audience;
    private final ConversationAudience wrappedAudience;

    private boolean finished, echo;

    @Nullable
    private Component by, onComplete;

    @Nullable
    private List<Clause> endClauses;

    private ChatVisibility chatVisibility = ChatVisibility.ALL;

    private List<PromptImpl<?>> prompts;
    private PromptImpl<?> currentPrompt;

    /**
     * @param provider          The provider that created this conversation.
     * @param adventureAudience The audience that this conversation is for.
     */
    public ConversationImpl(AdventureConversationsProvider provider, Audience adventureAudience) {
        this.provider = provider;
        this.audience = adventureAudience;
        this.wrappedAudience = new AdventureConversationAudience(this.audience);
    }

    /**
     * Actually executes the conversation.
     */
    @Override
    public void start() {
        if (this.finished) {
            throw new IllegalStateException("Can't run finished conversation multiple times");
        }

        this.currentPrompt = this.nextPrompt();
        if (this.currentPrompt != null) {
            this.currentPrompt.display();
        }
    }

    private PromptImpl<?> nextPrompt() {
        final int id = this.currentPrompt == null ? 0 : this.currentPrompt.getId();
        for (final PromptImpl<?> prompt : this.prompts) {
            if (prompt.getId() == id + 1) {
                return prompt;
            }
        }
        return null;
    }

    /**
     * Ticks the conversation, used for updating timers etc.
     */
    public void tick() {
        if (this.finished) {
            return;
        }
        if (this.endClauses != null) {
            for (final Clause clause : this.endClauses) {
                if (clause instanceof Clause.Ticking) {
                    final Clause.Ticking tickingClause = (Clause.Ticking) clause;
                    tickingClause.tick();
                }

                if (clause.hasBeenTriggered()) {
                    if (clause.getTriggerMessage() != null) {
                        this.audience.sendMessage(clause.getTriggerMessage());
                    }
                    this.finish();
                    return;
                }
            }
        }
    }

    /**
     * Adds a {@link Prompt} to the conversation.
     */
    @Override
    public Conversation prompt(Prompt<?> prompt) {
        if (prompt == null) {
            throw new IllegalArgumentException("Prompt can't be null");
        }

        if (this.prompts == null) {
            this.prompts = new ArrayList<>();
        }

        // Ideally we'd make Prompt a data object or toss its logic here, so that
        // classes that implement Prompt don't have to inherit from PromptImpl.
        PromptImpl<?> impl = (PromptImpl<?>) prompt;
        impl.setConversation(this);
        impl.setId(this.prompts.size() + 1);
        this.prompts.add(impl);
        return this;
    }

    /**
     * Specifies a clause for when this conversation should end. There's no limit to the amount of clauses you can add.
     */
    @Override
    public Conversation endWhen(Clause clause) {
        if (clause == null) {
            throw new IllegalArgumentException("Clause can't be null");
        }

        if (this.endClauses == null) {
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
    @Override
    public Conversation finishingText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text can't be null");
        }

        this.onComplete = LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        return this;
    }

    /**
     * A name that gets prepended to each line of this conversation.
     * <p>
     * example; name = Fish: prompt = Hello
     * <p>
     * Result = Fish: Hello
     */
    @Override
    public Conversation by(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name can't be null");
        }

        this.by = LegacyComponentSerializer.legacyAmpersand().deserialize(name);
        return this;
    }

    /**
     * Whether the input should be echo'd back to the sender.
     */
    @Override
    public Conversation echo(boolean flag) {
        this.echo = flag;
        return this;
    }

    @Override
    public Conversation chatVisibility(ChatVisibility visibility) {
        if (visibility == null) {
            throw new IllegalArgumentException("Visibility can't be null");
        }

        this.chatVisibility = visibility;
        return this;

    }

    public boolean inConversation(UUID uuid) {
        if (this.audience == null) {
            return false;
        }

        final Audience loneAudience = this.audience.filterAudience(input -> input.get(Identity.UUID).map(value -> value.equals(uuid)).orElse(false));
        return loneAudience.pointers().get(Identity.UUID).isPresent();
    }

    public boolean isFinished() {
        return this.finished;
    }

    @Override
    public void finish() {
        this.finished = true;
        provider.endInternal(this);
    }

    public boolean echoOn() {
        return this.echo;
    }

    public ChatVisibility getChatVisibility() {
        return this.chatVisibility;
    }

    //** INTERNAL **//

    public ConversationAudience getAudience() {
        return this.wrappedAudience;
    }

    public void handleInput(String input) {
        final String clean = StringValidator.clean(input);

        if (!this.currentPrompt.shouldHandle()) {
            provider.endInternal(this);
            if (this.currentPrompt.getAttemptsOverText() != null) {
                this.audience.sendMessage(this.currentPrompt.getAttemptsOverText());
            }
            return;
        }
        this.currentPrompt.handleInput(clean);
    }

    public void next() {
        final PromptImpl<?> next = this.nextPrompt();
        if (next == null) {
            this.finish();
            if (this.onComplete != null) {
                if (this.by != null) {
                    this.audience.sendMessage(this.by.append(Component.text(" ").append(this.onComplete)));
                } else {
                    this.audience.sendMessage(this.onComplete);
                }
            }
            return;
        }
        this.currentPrompt = next;
        this.currentPrompt.display();
    }

    public Component getBy() {
        return this.by;
    }
}