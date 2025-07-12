package com.fallengod.testament.listeners;

import com.fallengod.testament.FallenGodPlugin;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;

/**
 * Handles world generation events to automatically create altars in new worlds
 */
public class WorldGenerationListener implements Listener {
    
    private final FallenGodPlugin plugin;
    
    public WorldGenerationListener(FallenGodPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldInit(WorldInitEvent event) {
        World world = event.getWorld();
        
        // Skip if altar generation is disabled
        if (!plugin.getConfig().getBoolean("forge.auto_generate", true)) {
            plugin.getLogger().info("Auto-generation disabled, skipping altar generation for world: " + world.getName());
            return;
        }
        
        // Skip certain world types that shouldn't have altars
        if (shouldSkipWorld(world)) {
            plugin.getLogger().info("Skipping altar generation for world type: " + world.getEnvironment() + " (" + world.getName() + ")");
            return;
        }
        
        plugin.getLogger().info("New world detected: " + world.getName() + " - Scheduling altar generation");
        
        // Schedule altar generation with a delay to ensure world is fully loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            try {
                plugin.getAltarPlacementManager().generateAltars(world);
                plugin.getLogger().info("Altar generation initiated for new world: " + world.getName());
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to generate altars for new world " + world.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, 100L); // 5 second delay (100 ticks)
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        
        // Skip if altar generation is disabled
        if (!plugin.getConfig().getBoolean("forge.auto_generate", true)) {
            return;
        }
        
        // Skip certain world types
        if (shouldSkipWorld(world)) {
            return;
        }
        
        // Check if this world already has altars
        if (hasExistingAltars(world)) {
            plugin.getLogger().fine("World " + world.getName() + " already has altars, skipping generation");
            return;
        }
        
        plugin.getLogger().info("Loaded world without altars: " + world.getName() + " - Scheduling altar generation");
        
        // Schedule altar generation for worlds that don't have altars yet
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            try {
                plugin.getAltarPlacementManager().generateAltars(world);
                plugin.getLogger().info("Altar generation initiated for loaded world: " + world.getName());
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to generate altars for loaded world " + world.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, 200L); // 10 second delay for loaded worlds
    }
    
    /**
     * Determines if a world should be skipped for altar generation
     */
    private boolean shouldSkipWorld(World world) {
        // Skip nether and end worlds by default (configurable)
        boolean skipNether = plugin.getConfig().getBoolean("forge.skip_nether", true);
        boolean skipEnd = plugin.getConfig().getBoolean("forge.skip_end", true);
        
        switch (world.getEnvironment()) {
            case NETHER:
                return skipNether;
            case THE_END:
                return skipEnd;
            case NORMAL:
            default:
                return false;
        }
    }
    
    /**
     * Checks if a world already has altars generated
     */
    private boolean hasExistingAltars(World world) {
        var altarLocations = plugin.getAltarService().getAllAltarLocations();
        
        // Check if any registered altars exist in this world
        for (var entry : altarLocations.entrySet()) {
            if (entry.getValue().getWorld().equals(world)) {
                return true;
            }
        }
        
        return false;
    }
}