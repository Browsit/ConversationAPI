package me.illusion.conversations.bukkit;

import me.illusion.conversations.api.ChatVisibility;
import me.illusion.conversations.api.Conversations;
import me.illusion.conversations.api.ConversationsForwarder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;

/**
 * Created by Illusion on 2/9/2023
 * <p>
 * The Bukkit {@link ConversationsForwarder}.
 */
class BukkitConversationsForwarder implements ConversationsForwarder<JavaPlugin>, Listener {

    @Override
    public void register(JavaPlugin base) {
        base.getServer().getPluginManager().registerEvents(this, base);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        // Check whether any recipients are in a conversation which can't receive chat messages
        Iterator<Player> recipients = event.getRecipients().iterator();
        while (recipients.hasNext()) {
            Player recipient = recipients.next();
            Conversations.getConversationOf(recipient.getUniqueId()).ifPresent(conversation -> {
                if (conversation.getChatVisibility() != ChatVisibility.ALL)
                    recipients.remove();
            });
        }

        // Now we check if the message sender is in a conversation, if so we forward the input
        Player chatter = event.getPlayer();

        Conversations.getConversationOf(chatter.getUniqueId()).ifPresent(conversation -> {
            if (conversation.echoOn())
                chatter.sendMessage(event.getMessage());

            forwardInput(conversation, chatter.getUniqueId(), event.getMessage(), () -> {
                event.setCancelled(true);
            });
        });
    }
}
