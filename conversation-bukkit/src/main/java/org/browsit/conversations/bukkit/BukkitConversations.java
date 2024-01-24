package org.browsit.conversations.bukkit;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.browsit.conversations.api.Conversations;
import org.browsit.conversations.api.provider.ConversationsProvider;
import org.browsit.conversations.impl.provider.AdventureConversationsProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Illusion
 * created on 2/16/2023
 * <p>
 * Bukkit wrapper for {@link Conversations}.
 */
public class BukkitConversations {

    private static boolean initialized;

    /**
     * Initializes the Conversations API.
     */
    public static void init(JavaPlugin plugin) {
        if (initialized) throw new IllegalStateException("Conversations(Bukkit) API already initialized");
        BukkitAudiences ba = BukkitAudiences.create(plugin);
        ConversationsProvider provider = AdventureConversationsProvider.create(ba);
        Conversations.init(provider);
        plugin.getServer().getPluginManager().registerEvents(new BukkitConversationsForwarder(), plugin);
        initialized = true;
    }

    /**
     * Cleans up the Conversations API.
     */
    public static void cleanUp() {
        if (!initialized) throw new IllegalStateException("Conversations(Bukkit) API not initialized");
        Conversations.cleanUp();
    }
}
