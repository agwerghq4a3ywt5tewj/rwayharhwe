package com.fallengod.testament.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import com.fallengod.testament.data.PlayerTestamentDataStore;

/**
 * Handles saving player data on logout
 */
public class PlayerDataSaveListener implements Listener {
    private final PlayerTestamentDataStore dataStore;
    private final Plugin plugin;

    public PlayerDataSaveListener(PlayerTestamentDataStore dataStore, Plugin plugin) {
        this.dataStore = dataStore;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            dataStore.save();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save player testament progress on logout: " + e.getMessage());
        }
    }
}