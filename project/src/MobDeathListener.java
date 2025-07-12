package com.fallengod.testament.listeners;

import com.fallengod.testament.FallenGodPlugin;
import com.fallengod.testament.services.FragmentSpawningService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Handles mob death events for fragment drops
 */
public class MobDeathListener implements Listener {
    
    private final FragmentSpawningService fragmentSpawningService;
    private final FallenGodPlugin plugin;
    
    public MobDeathListener(FallenGodPlugin plugin, FragmentSpawningService fragmentSpawningService) {
        this.plugin = plugin;
        this.fragmentSpawningService = fragmentSpawningService;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        try {
            fragmentSpawningService.handleMobDeath(event);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error handling mob death: " + e.getMessage());
        }
    }
}