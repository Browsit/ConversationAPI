package org.browsit.conversations.bukkit;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.function.Consumer;
import org.browsit.conversations.api.Conversations;
import org.browsit.conversations.api.data.ChatVisibility;
import org.browsit.conversations.api.action.ConversationsForwarder;
import org.browsit.conversations.api.data.Conversation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Illusion created on 2/9/2023
 * <p>
 * The Bukkit {@link ConversationsForwarder}.
 */
final class BukkitConversationsForwarder implements ConversationsForwarder<JavaPlugin, Player>, Listener {

    private static final String FOLIA_GLOBAL_REGION_SCHEDULER = "io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler";
    private static final String FOLIA_ENTITY_SCHEDULER = "io.papermc.paper.threadedregions.scheduler.EntityScheduler";

    private JavaPlugin base;
    private final boolean folia;
    private Method getSchedulerMethod;
    private Method entitySchedulerRunMethod;
    private boolean entitySchedulerRunTakesRetired;

    BukkitConversationsForwarder() {
        this.folia = isFolia();
        if (this.folia) {
            try {
                this.getSchedulerMethod = Player.class.getMethod("getScheduler");
                this.initFoliaRunMethod();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Unable to initialize Folia scheduler", e);
            }
        }
    }

    private void initFoliaRunMethod() throws ReflectiveOperationException {
        final Class<?> entitySchedulerClass = Class.forName(FOLIA_ENTITY_SCHEDULER);
        for (final Method method : entitySchedulerClass.getMethods()) {
            if (!method.getName().equals("run")) {
                continue;
            }
            final Class<?>[] params = method.getParameterTypes();
            if (params.length < 2 || params.length > 3) {
                continue;
            }
            if (!params[0].isAssignableFrom(Plugin.class) || !params[1].isAssignableFrom(Consumer.class)) {
                continue;
            }
            if (params.length == 3 && params[2] != Runnable.class) {
                continue;
            }
            this.entitySchedulerRunMethod = method;
            this.entitySchedulerRunTakesRetired = params.length == 3;
            return;
        }
        throw new NoSuchMethodException("No compatible EntityScheduler#run method found");
    }

    @Override
    public void register(JavaPlugin base) {
        this.base = base;
        base.getServer().getPluginManager().registerEvents(this, base);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        if (this.base == null) {
            return; // Not initialized yet
        }

        // Check whether any recipients are in a conversation which can't receive chat messages
        final Iterator<Player> recipients = event.getRecipients().iterator();
        while (recipients.hasNext()) {
            final Player recipient = recipients.next();
            Conversations.getConversationOf(recipient.getUniqueId()).ifPresent(conversation -> {
                if (conversation.getChatVisibility() != ChatVisibility.ALL) {
                    recipients.remove();
                }
            });
        }

        // Now we check if the message sender is in a conversation, if so we forward the input
        final Player chatter = event.getPlayer();

        Conversations.getConversationOf(chatter.getUniqueId()).ifPresent(conversation -> {
            if (conversation.echoOn()) {
                chatter.sendMessage(event.getMessage());
            }

            this.forwardInput(conversation, event.getMessage(), chatter, () -> event.setCancelled(true));
        });
    }

    @Override
    public void forwardInput(Conversation conversation, String input, Runnable onSuccess) {
        if (this.base == null) {
            return; // Not initialized yet
        }

        // No player reference, so thread-correct scheduling (e.g. Folia) cannot be applied
        Bukkit.getScheduler().runTask(this.base, () -> {
            conversation.handleInput(input);
        });

        onSuccess.run();
    }

    @Override
    public void forwardInput(Conversation conversation, String input, Player player, Runnable onSuccess) {
        if (this.base == null) {
            return; // Not initialized yet
        }

        if (this.folia) {
            // Folia: run on the thread that owns the player's region
            this.scheduleFolia(conversation, input, player);
        } else {
            forwardInput(conversation, input, onSuccess);
        }
    }

    private static boolean isFolia() {
        try {
            Class.forName(FOLIA_GLOBAL_REGION_SCHEDULER);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void scheduleFolia(Conversation conversation, String input, Player player) {
        try {
            final Object entityScheduler = this.getSchedulerMethod.invoke(player);
            final Consumer<Object> task = ignored -> conversation.handleInput(input);
            if (this.entitySchedulerRunTakesRetired) {
                this.entitySchedulerRunMethod.invoke(entityScheduler, this.base, task, null);
            } else {
                this.entitySchedulerRunMethod.invoke(entityScheduler, this.base, task);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to schedule chat input on Folia", e);
        }
    }
}
