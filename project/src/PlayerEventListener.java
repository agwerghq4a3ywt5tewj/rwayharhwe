package com.fallengod.testament.listeners;

import com.fallengod.testament.FallenGodPlugin;
import com.fallengod.testament.services.TestamentService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles general player events
 */
public class PlayerEventListener implements Listener {
    
    private final FallenGodPlugin plugin;
    private final TestamentService testamentService;
    
    public PlayerEventListener(FallenGodPlugin plugin, TestamentService testamentService) {
        this.plugin = plugin;
        this.testamentService = testamentService;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        try {
            // Update fragment counts based on inventory
            testamentService.updatePlayerFragmentCounts(player);
            
            plugin.getLogger().fine("Updated fragment counts for " + player.getName());
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error updating fragment counts for " + player.getName() + ": " + e.getMessage());
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        try {
            // Clear player cache to free memory
            testamentService.clearPlayerCache(player.getUniqueId());
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error clearing cache for " + player.getName() + ": " + e.getMessage());
        }
    }
}