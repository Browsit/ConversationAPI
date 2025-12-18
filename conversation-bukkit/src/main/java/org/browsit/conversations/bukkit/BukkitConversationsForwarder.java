package org.browsit.conversations.bukkit;

import org.browsit.conversations.api.Conversations;
import org.browsit.conversations.api.data.ChatVisibility;
import org.browsit.conversations.api.action.ConversationsForwarder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;

/**
 * @author Illusion created on 2/9/2023
 * <p>
 * The Bukkit {@link ConversationsForwarder}.
 */
public class BukkitConversationsForwarder implements ConversationsForwarder<JavaPlugin>, Listener {
    JavaPlugin base;

    @Override
    public void register(JavaPlugin base) {
        this.base = base;
        base.getServer().getPluginManager().registerEvents(this, base);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
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
        if (base == null) {
            Bukkit.getLogger().severe("BukkitConversationsForwarder was not registered");
        }
        final BukkitConversationsForwarder bcf = this;
        Bukkit.getScheduler().runTask(base, () ->
                Conversations.getConversationOf(chatter.getUniqueId()).ifPresent(conversation -> {
            if (conversation.echoOn()) {
                chatter.sendMessage(event.getMessage());
            }

            bcf.forwardInput(conversation, event.getMessage(), () -> {
                event.setCancelled(true);
            });
        }));
    }
}
