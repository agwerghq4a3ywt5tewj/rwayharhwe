package com.fallengod.testament.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.fallengod.testament.FallenGodPlugin;
import com.fallengod.testament.items.HeartOfFallenGod;

/**
 * Handles Heart of Fallen God effect events and Veil of Nullification interactions
 * Manages the complex interaction between Heart power and Veil nullification
 */
public class HeartEffectListener implements Listener {
    
    private final HeartOfFallenGod heartOfFallenGod;
    private final FallenGodPlugin plugin;
    private final Logger logger;
    
    // Track players with active Heart effects
    private final Map<UUID, Boolean> playersWithHeart = new HashMap<>();
    // Track players with active Veil effects
    private final Map<UUID, Boolean> playersWithVeil = new HashMap<>();
    // Track nullified players (Heart holders near Veil holders)
    private final Map<UUID, Boolean> nullifiedPlayers = new HashMap<>();
    
    // Configuration
    private final boolean heartEnabled;
    private final boolean nullificationEnabled;
    private final double nullificationRange;
    private final int extraHearts;
    private final int strengthLevel;
    private final int regenerationLevel;
    
    public HeartEffectListener(FallenGodPlugin plugin, HeartOfFallenGod heartOfFallenGod) {
        this.plugin = plugin;
        this.heartOfFallenGod = heartOfFallenGod;
        this.logger = plugin.getLogger();
        
        // Load configuration
        this.heartEnabled = plugin.getConfig().getBoolean("heart_of_fallen_god.enabled", true);
        this.nullificationEnabled = plugin.getConfig().getBoolean("heart_of_fallen_god.nullification.enabled", true);
        this.nullificationRange = plugin.getConfig().getDouble("heart_of_fallen_god.nullification.range", 16.0);
        this.extraHearts = plugin.getConfig().getInt("heart_of_fallen_god.extra_hearts", 15);
        this.strengthLevel = plugin.getConfig().getInt("heart_of_fallen_god.strength_level", 1);
        this.regenerationLevel = plugin.getConfig().getInt("heart_of_fallen_god.regeneration_level", 2);
        
        // Start the effect monitoring task
        startEffectMonitoringTask();
        
        logger.info("Heart Effect Listener initialized - Heart: " + heartEnabled + ", Nullification: " + nullificationEnabled);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check player's inventory for Heart or Veil items
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            updatePlayerEffects(player);
        }, 20L); // 1 second delay to ensure player is fully loaded
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        
        // Clean up tracking maps
        playersWithHeart.remove(playerId);
        playersWithVeil.remove(playerId);
        nullifiedPlayers.remove(playerId);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if nullification is enabled and player moved to a different block
        if (!nullificationEnabled || 
            event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Check if this player has Heart or Veil
        if (playersWithHeart.containsKey(playerId) || playersWithVeil.containsKey(playerId)) {
            // Update nullification status for nearby players
            updateNullificationForNearbyPlayers(player);
        }
    }
    
    /**
     * Starts the task that monitors and applies Heart/Veil effects
     */
    private void startEffectMonitoringTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!heartEnabled) return;
                
                try {
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        updatePlayerEffects(player);
                    }
                } catch (Exception e) {
                    logger.warning("Error in Heart effect monitoring task: " + e.getMessage());
                }
            }
        }.runTaskTimer(plugin, 100L, 100L); // Run every 5 seconds (100 ticks)
    }
    
    /**
     * Updates a player's effects based on their inventory
     */
    private void updatePlayerEffects(Player player) {
        UUID playerId = player.getUniqueId();
        
        boolean hasHeart = hasHeartOfFallenGod(player);
        boolean hasVeil = hasVeilOfNullification(player);
        
        // Update tracking
        if (hasHeart) {
            playersWithHeart.put(playerId, true);
        } else {
            playersWithHeart.remove(playerId);
        }
        
        if (hasVeil) {
            playersWithVeil.put(playerId, true);
        } else {
            playersWithVeil.remove(playerId);
        }
        
        // Apply or remove effects
        if (hasHeart) {
            applyHeartEffects(player);
        } else {
            removeHeartEffects(player);
        }
        
        if (hasVeil) {
            applyVeilEffects(player);
        } else {
            removeVeilEffects(player);
        }
        
        // Update nullification status
        if (nullificationEnabled) {
            updateNullificationStatus(player);
        }
    }
    
    /**
     * Checks if a player has the Heart of Fallen God in their inventory
     */
    private boolean hasHeartOfFallenGod(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (heartOfFallenGod.isHeartOfFallenGod(item)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if a player has the Veil of Nullification in their inventory
     */
    private boolean hasVeilOfNullification(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (heartOfFallenGod.isVeilOfNullification(item)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Applies Heart of Fallen God effects to a player
     */
    private void applyHeartEffects(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Check if player is nullified
        if (nullifiedPlayers.containsKey(playerId)) {
            // Heart effects are nullified
            removeHeartEffects(player);
            // Notify player they're being nullified (timing logic removed for compatibility)
            player.sendMessage("§5§l◊ Your divine power is being nullified by a nearby Veil! ◊");
            return;
        }
        
        try {
            // Set max health (20 base + extra hearts)
            double maxHealth = 20.0 + (extraHearts * 2.0); // Each heart = 2 health points
            if (player.getMaxHealth() != maxHealth) {
                player.setMaxHealth(maxHealth);
                player.setHealth(maxHealth); // Heal to full when first equipped
                
                player.sendMessage("§4§l❤ The Heart of the Fallen God pulses with divine power! ❤");
                player.sendMessage("§c§lYou now have " + (int)(maxHealth / 2) + " hearts!");
            }
            
            // Apply permanent effects
            applyPermanentEffect(player, PotionEffectType.STRENGTH, strengthLevel - 1);
            applyPermanentEffect(player, PotionEffectType.REGENERATION, regenerationLevel - 1);
            
        } catch (Exception e) {
            logger.warning("Error applying Heart effects to " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Removes Heart of Fallen God effects from a player
     */
    private void removeHeartEffects(Player player) {
        try {
            // Reset max health to normal
            if (player.getMaxHealth() > 20.0) {
                player.setMaxHealth(20.0);
                if (player.getHealth() > 20.0) {
                    player.setHealth(20.0);
                }
            }
            
            // Remove Heart-specific effects
            player.removePotionEffect(PotionEffectType.STRENGTH);
            player.removePotionEffect(PotionEffectType.REGENERATION);
            
        } catch (Exception e) {
            logger.warning("Error removing Heart effects from " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Applies Veil of Nullification effects to a player
     */
    private void applyVeilEffects(Player player) {
        try {
            // Apply Veil effects
            boolean slowFalling = plugin.getConfig().getBoolean("heart_of_fallen_god.nullification.veil_effects.slow_falling", true);
            boolean nightVision = plugin.getConfig().getBoolean("heart_of_fallen_god.nullification.veil_effects.night_vision", true);
            
            if (slowFalling) {
                applyPermanentEffect(player, PotionEffectType.SLOW_FALLING, 0);
            }
            
            if (nightVision) {
                applyPermanentEffect(player, PotionEffectType.NIGHT_VISION, 0);
            }
            
        } catch (Exception e) {
            logger.warning("Error applying Veil effects to " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Removes Veil of Nullification effects from a player
     */
    private void removeVeilEffects(Player player) {
        try {
            // Remove Veil-specific effects
            player.removePotionEffect(PotionEffectType.SLOW_FALLING);
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            
        } catch (Exception e) {
            logger.warning("Error removing Veil effects from " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Updates nullification status for a player
     */
    private void updateNullificationStatus(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Only Heart holders can be nullified
        if (!playersWithHeart.containsKey(playerId)) {
            nullifiedPlayers.remove(playerId);
            return;
        }
        
        boolean isNullified = isPlayerNullified(player);
        boolean wasNullified = nullifiedPlayers.containsKey(playerId);
        
        if (isNullified && !wasNullified) {
            // Player just became nullified
            nullifiedPlayers.put(playerId, true);
            player.sendMessage("§5§l◊ A Veil of Nullification suppresses your divine power! ◊");
            
            // Remove Heart effects
            removeHeartEffects(player);
            
        } else if (!isNullified && wasNullified) {
            // Player is no longer nullified
            nullifiedPlayers.remove(playerId);
            player.sendMessage("§4§l❤ Your divine power returns as the Veil's influence fades! ❤");
            
            // Reapply Heart effects
            applyHeartEffects(player);
        }
    }
    
    /**
     * Updates nullification for all players near the given player
     */
    private void updateNullificationForNearbyPlayers(Player player) {
        Location playerLoc = player.getLocation();
        
        for (Player nearbyPlayer : player.getWorld().getPlayers()) {
            if (nearbyPlayer.equals(player)) continue;
            
            double distance = playerLoc.distance(nearbyPlayer.getLocation());
            if (distance <= nullificationRange) {
                updateNullificationStatus(nearbyPlayer);
            }
        }
    }
    
    /**
     * Checks if a player is currently being nullified by nearby Veil holders
     */
    private boolean isPlayerNullified(Player player) {
        Location playerLoc = player.getLocation();
        
        // Check for nearby Veil holders
        for (Player otherPlayer : player.getWorld().getPlayers()) {
            if (otherPlayer.equals(player)) continue;
            
            // Check if other player has Veil
            if (playersWithVeil.containsKey(otherPlayer.getUniqueId())) {
                double distance = playerLoc.distance(otherPlayer.getLocation());
                if (distance <= nullificationRange) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Applies a permanent potion effect to a player
     */
    private void applyPermanentEffect(Player player, PotionEffectType effectType, int amplifier) {
        try {
            // Remove existing effect first
            player.removePotionEffect(effectType);
            
            // Add new permanent effect
            PotionEffect effect = new PotionEffect(effectType, Integer.MAX_VALUE, amplifier, false, false);
            player.addPotionEffect(effect);
            
        } catch (Exception e) {
            logger.warning("Failed to add permanent effect " + effectType.getName() + " to " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Gets statistics about Heart and Veil usage
     */
    public Map<String, Object> getHeartEffectStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("players_with_heart", playersWithHeart.size());
        stats.put("players_with_veil", playersWithVeil.size());
        stats.put("nullified_players", nullifiedPlayers.size());
        stats.put("heart_enabled", heartEnabled);
        stats.put("nullification_enabled", nullificationEnabled);
        stats.put("nullification_range", nullificationRange);
        return stats;
    }
    
    /**
     * Cleanup method for plugin disable
     */
    public void cleanup() {
        // Remove all Heart effects from players
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            try {
                removeHeartEffects(player);
                removeVeilEffects(player);
            } catch (Exception e) {
                logger.warning("Error cleaning up effects for " + player.getName() + ": " + e.getMessage());
            }
        }
        
        // Clear tracking maps
        playersWithHeart.clear();
        playersWithVeil.clear();
        nullifiedPlayers.clear();
        
        logger.info("Heart Effect Listener cleaned up successfully");
    }
}