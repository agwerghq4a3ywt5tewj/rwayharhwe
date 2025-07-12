package com.fallengod.testament.services;

import com.fallengod.testament.FallenGodPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages altar interactions and fragment reunification
 */
public class AltarService {
    
    private final FallenGodPlugin plugin;
    private final TestamentService testamentService;
    private final Logger logger;
    private final Map<String, Location> altarLocations = new ConcurrentHashMap<>();
    
    public AltarService(FallenGodPlugin plugin, TestamentService testamentService) {
        this.plugin = plugin;
        this.testamentService = testamentService;
        this.logger = plugin.getLogger();
    }
    
    /**
     * Handles altar interaction for fragment reunification
     */
    public boolean handleAltarInteraction(Player player, String godType, Location altarLocation) {
        // Validate god type
        if (!plugin.getFragmentManager().getValidGodTypes().contains(godType)) {
            player.sendMessage("§cInvalid god type: " + godType);
            return false;
        }
        
        // Check if testament is already completed
        if (testamentService.isTestamentCompleted(player, godType)) {
            player.sendMessage("§e§lYou have already completed the " + godType + " testament!");
            return false;
        }
        
        // Check if player has all 7 unique fragments
        int fragmentCount = testamentService.getFragmentCount(player, godType);
        if (fragmentCount < 7) {
            player.sendMessage("§c§lYou need all 7 unique " + godType + " fragments to use this altar!");
            player.sendMessage("§7Current progress: " + fragmentCount + "/7 fragments");
            
            // Show detailed progress
            var fragments = testamentService.getFragmentsFound(player, godType);
            StringBuilder missing = new StringBuilder("§7Missing fragments: §c");
            boolean first = true;
            for (int i = 1; i <= 7; i++) {
                if (!fragments.contains(i)) {
                    if (!first) missing.append(", ");
                    missing.append(i);
                    first = false;
                }
            }
            player.sendMessage(missing.toString());
            return false;
        }
        
        // Process reunification
        return processReunification(player, godType);
    }
    
    /**
     * Processes the reunification of fragments
     */
    private boolean processReunification(Player player, String godType) {
        try {
            player.sendMessage("§6§l✦ TESTAMENT REUNIFICATION COMPLETE ✦");
            player.sendMessage("§e§lThe fragments of " + godType.toUpperCase() + " have been reunited!");
            
            // Complete the testament (this handles fragment removal and rewards)
            testamentService.checkAndCompleteTestament(player, godType);
            
            return true;
            
        } catch (Exception e) {
            logger.severe("Error during testament reunification for " + player.getName() + ": " + e.getMessage());
            player.sendMessage("§cAn error occurred during reunification. Please contact an administrator.");
            return false;
        }
    }
    
    /**
     * Registers an altar location
     */
    public void registerAltar(String godType, Location location) {
        if (location == null || location.getWorld() == null) {
            logger.warning("Attempted to register altar with invalid location for god type: " + godType);
            return;
        }
        
        altarLocations.put(godType, location.clone());
        logger.info("Registered " + godType + " altar at " + formatLocation(location));
    }
    
    /**
     * Gets the location of an altar
     */
    public Location getAltarLocation(String godType) {
        Location location = altarLocations.get(godType);
        return location != null ? location.clone() : null;
    }
    
    /**
     * Gets all registered altar locations
     */
    public Map<String, Location> getAllAltarLocations() {
        Map<String, Location> copy = new ConcurrentHashMap<>();
        for (Map.Entry<String, Location> entry : altarLocations.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().clone());
        }
        return copy;
    }
    
    /**
     * Removes an altar registration
     */
    public boolean removeAltar(String godType) {
        Location removed = altarLocations.remove(godType);
        if (removed != null) {
            logger.info("Removed " + godType + " altar registration");
            return true;
        }
        return false;
    }
    
    /**
     * Checks if an altar is registered for a god type
     */
    public boolean hasAltar(String godType) {
        return altarLocations.containsKey(godType);
    }
    
    private String formatLocation(Location loc) {
        return loc.getWorld().getName() + " (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }
}