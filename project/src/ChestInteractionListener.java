package com.fallengod.testament.listeners;

import com.fallengod.testament.FallenGodPlugin;
import com.fallengod.testament.services.FragmentSpawningService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

/**
 * Handles chest interaction events for fragment spawning
 */
public class ChestInteractionListener implements Listener {
    
    private final FragmentSpawningService fragmentSpawningService;
    private final FallenGodPlugin plugin;
    
    public ChestInteractionListener(FallenGodPlugin plugin, FragmentSpawningService fragmentSpawningService) {
        this.plugin = plugin;
        this.fragmentSpawningService = fragmentSpawningService;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        // Only handle chest-type inventories
        if (event.getInventory().getType() != InventoryType.CHEST) {
            return;
        }
        
        // Only handle player interactions
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        // Must have a valid location
        if (event.getInventory().getLocation() == null) {
            return;
        }
        
        try {
            Player player = (Player) event.getPlayer();
            fragmentSpawningService.handleChestOpen(event.getInventory().getLocation(), player);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error handling chest interaction: " + e.getMessage());
        }
    }
}