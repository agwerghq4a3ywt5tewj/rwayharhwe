package com.fallengod.testament.listeners;

import com.fallengod.testament.FallenGodPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Marker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.Set;

/**
 * Listens for datapack-generated altars and registers them with the plugin
 */
public class DatapackAltarListener implements Listener {
    
    private final FallenGodPlugin plugin;
    private final NamespacedKey altarTypeKey;
    
    public DatapackAltarListener(FallenGodPlugin plugin) {
        this.plugin = plugin;
        this.altarTypeKey = new NamespacedKey(plugin, "altar_type");
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        // Check for altar markers in loaded chunks
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getType() == EntityType.MARKER) {
                checkAndRegisterAltar((Marker) entity);
            }
        }
    }
    
    private void checkAndRegisterAltar(Marker marker) {
        Set<String> tags = marker.getScoreboardTags();
        
        // Check for altar tags
        String godType = null;
        if (tags.contains("fallen_altar")) {
            godType = "fallen";
        } else if (tags.contains("banishment_altar")) {
            godType = "banishment";
        } else if (tags.contains("abyssal_altar")) {
            godType = "abyssal";
        } else if (tags.contains("sylvan_altar")) {
            godType = "sylvan";
        } else if (tags.contains("tempest_altar")) {
            godType = "tempest";
        } else if (tags.contains("veil_altar")) {
            godType = "veil";
        }
        
        if (godType != null && !tags.contains("registered")) {
            // Register the altar with the plugin
            Location altarLocation = marker.getLocation();
            plugin.getAltarService().registerAltar(godType, altarLocation);
            
            // Mark as registered
            marker.addScoreboardTag("registered");
            marker.getPersistentDataContainer().set(altarTypeKey, PersistentDataType.STRING, godType);
            
            plugin.getLogger().info(String.format("Registered datapack-generated %s altar at %s", 
                godType, formatLocation(altarLocation)));
        }
    }
    
    private String formatLocation(Location loc) {
        return loc.getWorld().getName() + " (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }
}