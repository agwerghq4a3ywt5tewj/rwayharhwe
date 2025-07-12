package com.fallengod.testament.services;

import com.fallengod.testament.FallenGodPlugin;
import com.fallengod.testament.data.PlayerTestamentData;
import com.fallengod.testament.data.PlayerTestamentDataStore;
import com.fallengod.testament.items.FragmentManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Unified service for managing testament progress and fragment tracking
 */
public class TestamentService {
    
    private final FallenGodPlugin plugin;
    private final PlayerTestamentDataStore dataStore;
    private final FragmentManager fragmentManager;
    private final Logger logger;
    
    // Thread-safe cache for performance
    private final Map<UUID, Map<String, Integer>> fragmentCountCache = new ConcurrentHashMap<>();
    
    public TestamentService(FallenGodPlugin plugin, PlayerTestamentDataStore dataStore, FragmentManager fragmentManager) {
        this.plugin = plugin;
        this.dataStore = dataStore;
        this.fragmentManager = fragmentManager;
        this.logger = plugin.getLogger();
    }
    
    /**
     * Updates player progress when they obtain a fragment
     */
    public void onFragmentObtained(Player player, String godType, int fragmentNumber) {
        UUID playerId = player.getUniqueId();
        PlayerTestamentData data = dataStore.get(playerId);
        
        // Add the specific fragment
        boolean wasNew = data.addFragment(godType, fragmentNumber);
        
        if (wasNew) {
            // Update cache
            updateFragmentCountCache(playerId, godType, data.getFragmentCount(godType));
            
            // Check for completion
            checkAndCompleteTestament(player, godType);
            
            // Notify player
            int currentCount = data.getFragmentCount(godType);
            player.sendMessage(String.format("§6Fragment obtained! %s Testament: §e%d/7 fragments", 
                capitalizeFirst(godType), currentCount));
            
            logger.info(String.format("Player %s obtained %s fragment %d (%d/7 total)", 
                player.getName(), godType, fragmentNumber, currentCount));
        }
    }
    
    /**
     * Checks if player has completed a testament and handles completion
     */
    public void checkAndCompleteTestament(Player player, String godType) {
        PlayerTestamentData data = dataStore.get(player.getUniqueId());
        
        // Check if player has all 7 unique fragments
        Set<Integer> playerFragments = data.getFragmentsFound(godType);
        boolean hasAllFragments = playerFragments.size() == 7;
        
        for (int i = 1; i <= 7; i++) {
            if (!playerFragments.contains(i)) {
                hasAllFragments = false;
                break;
            }
        }
        
        if (hasAllFragments && !data.isTestamentCompleted(godType)) {
            // Remove fragments from inventory
            removeFragmentsFromInventory(player, godType);
            
            // Clear fragments from data (they've been consumed)
            data.clearFragments(godType);
            
            // Mark as completed
            data.completeTestament(godType);
            
            // Update cache
            updateFragmentCountCache(player.getUniqueId(), godType, 0);
            
            // Save progress
            try {
                dataStore.save();
            } catch (Exception e) {
                logger.warning("Failed to save testament completion for " + player.getName() + ": " + e.getMessage());
            }
            
            // Grant rewards
            plugin.getRewardService().grantGodReward(player, godType);
            
            // Notify player
            player.sendMessage("§6§lYou have completed the " + godType + " testament and received your rewards!");
            
            logger.info("TESTAMENT COMPLETED: " + player.getName() + " completed " + godType + " testament");
        }
    }
    
    /**
     * Removes all fragments of a specific god type from player inventory
     */
    private void removeFragmentsFromInventory(Player player, String godType) {
        for (int i = 1; i <= 7; i++) {
            ItemStack fragment = fragmentManager.createFragment(godType, i);
            player.getInventory().removeItem(fragment);
        }
    }
    
    /**
     * Updates player fragment counts based on their current inventory
     */
    public void updatePlayerFragmentCounts(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerTestamentData data = dataStore.get(playerId);
        
        // Clear current data and rebuild from inventory
        for (String godType : fragmentManager.getValidGodTypes()) {
            data.clearFragments(godType);
            
            // Scan inventory for fragments
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && fragmentManager.isTestamentFragment(item)) {
                    String fragmentGodType = fragmentManager.getGodTypeFromFragment(item);
                    int fragmentNumber = fragmentManager.getFragmentNumber(item);
                    
                    if (godType.equals(fragmentGodType) && fragmentNumber >= 1 && fragmentNumber <= 7) {
                        data.addFragment(godType, fragmentNumber);
                    }
                }
            }
            
            // Update cache
            updateFragmentCountCache(playerId, godType, data.getFragmentCount(godType));
        }
    }
    
    /**
     * Gets the number of unique fragments a player has for a god
     */
    public int getFragmentCount(Player player, String godType) {
        UUID playerId = player.getUniqueId();
        
        // Try cache first
        Map<String, Integer> playerCache = fragmentCountCache.get(playerId);
        if (playerCache != null && playerCache.containsKey(godType)) {
            return playerCache.get(godType);
        }
        
        // Fallback to data store
        PlayerTestamentData data = dataStore.get(playerId);
        int count = data.getFragmentCount(godType);
        
        // Update cache
        updateFragmentCountCache(playerId, godType, count);
        
        return count;
    }
    
    /**
     * Gets all fragment counts for a player
     */
    public Map<String, Integer> getAllFragmentCounts(Player player) {
        Map<String, Integer> counts = new HashMap<>();
        
        for (String godType : fragmentManager.getValidGodTypes()) {
            counts.put(godType, getFragmentCount(player, godType));
        }
        
        return counts;
    }
    
    /**
     * Checks if a player has completed a specific testament
     */
    public boolean isTestamentCompleted(Player player, String godType) {
        return dataStore.get(player.getUniqueId()).isTestamentCompleted(godType);
    }
    
    /**
     * Gets the specific fragments a player has found for a god
     */
    public Set<Integer> getFragmentsFound(Player player, String godType) {
        return dataStore.get(player.getUniqueId()).getFragmentsFound(godType);
    }
    
    /**
     * Updates the fragment count cache for performance
     */
    private void updateFragmentCountCache(UUID playerId, String godType, int count) {
        fragmentCountCache.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>()).put(godType, count);
    }
    
    /**
     * Clears cache for a player (call on logout)
     */
    public void clearPlayerCache(UUID playerId) {
        fragmentCountCache.remove(playerId);
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}