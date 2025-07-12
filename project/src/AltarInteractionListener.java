package com.fallengod.testament.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.fallengod.testament.FallenGodPlugin;
import com.fallengod.testament.services.AltarService;

/**
 * Handles altar interaction events with proper block detection
 */
public class AltarInteractionListener implements Listener {
    
    private final AltarService altarService;
    private final FallenGodPlugin plugin;
    
    // Map altar center blocks to god types for quick lookup
    private final Map<Material, String[]> altarCenterBlocks;
    
    public AltarInteractionListener(FallenGodPlugin plugin, AltarService altarService) {
        this.plugin = plugin;
        this.altarService = altarService;
        this.altarCenterBlocks = initializeAltarBlocks();
    }
    
    private Map<Material, String[]> initializeAltarBlocks() {
        Map<Material, String[]> blocks = new HashMap<>();
        
        // Map center block materials to possible god types
        blocks.put(Material.CRYING_OBSIDIAN, new String[]{"fallen"});
        blocks.put(Material.MAGMA_BLOCK, new String[]{"banishment"});
        blocks.put(Material.DARK_PRISMARINE, new String[]{"abyssal"});
        blocks.put(Material.OAK_LOG, new String[]{"sylvan"});
        blocks.put(Material.LIGHTNING_ROD, new String[]{"tempest"});
        blocks.put(Material.END_PORTAL_FRAME, new String[]{"veil"});
        
        return blocks;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click interactions
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // Must be a player
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        // Must have clicked a block
        if (event.getClickedBlock() == null) {
            return;
        }
        
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        
        try {
            // Check if this could be an altar center block
            String godType = detectAltarType(clickedBlock);
            if (godType != null) {
                // Verify this is actually a registered altar
                if (isRegisteredAltar(clickedBlock.getLocation(), godType)) {
                    // Handle the altar interaction
                    boolean success = altarService.handleAltarInteraction(player, godType, clickedBlock.getLocation());
                    
                    if (success) {
                        // Cancel the event to prevent other interactions
                        event.setCancelled(true);
                        
                        // Add visual and audio effects
                        addAltarEffects(player, clickedBlock.getLocation(), godType);
                        
                        plugin.getLogger().info(String.format("Player %s successfully interacted with %s altar at %s", 
                            player.getName(), godType, formatLocation(clickedBlock.getLocation())));
                    }
                } else {
                    // This looks like an altar block but isn't registered
                    // Could be a player-built structure that coincidentally matches
                    plugin.getLogger().fine(String.format("Player %s clicked potential %s altar block but it's not registered at %s", 
                        player.getName(), godType, formatLocation(clickedBlock.getLocation())));
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error handling altar interaction for " + player.getName() + ": " + e.getMessage());
            player.sendMessage("§cAn error occurred while interacting with the altar. Please try again.");
        }
    }
    
    /**
     * Detects if a block could be an altar center and returns the god type
     */
    private String detectAltarType(Block block) {
        Material blockType = block.getType();
        
        // Check if this material is used as an altar center
        String[] possibleGods = altarCenterBlocks.get(blockType);
        if (possibleGods == null || possibleGods.length == 0) {
            return null;
        }
        
        // For now, return the first possible god type
        // In a more complex system, you might need additional checks
        return possibleGods[0];
    }
    
    /**
     * Verifies that a location is actually a registered altar
     */
    private boolean isRegisteredAltar(Location clickedLocation, String godType) {
        // Get all registered altar locations
        Map<String, Location> altarLocations = altarService.getAllAltarLocations();
        
        // Check if there's a registered altar of this type nearby
        for (Map.Entry<String, Location> entry : altarLocations.entrySet()) {
            String registeredGodType = entry.getKey();
            Location altarCenter = entry.getValue();
            
            // Check if this is the right god type and location is close enough
            if (registeredGodType.equals(godType) && 
                altarCenter.getWorld().equals(clickedLocation.getWorld())) {
                
                double distance = altarCenter.distance(clickedLocation);
                
                // Allow some tolerance for clicking near the center (within 3 blocks)
                if (distance <= 3.0) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Adds visual and audio effects when a player interacts with an altar
     */
    private void addAltarEffects(Player player, Location altarLocation, String godType) {
        try {
            // Get configuration for effects
            boolean particlesEnabled = plugin.getConfig().getBoolean("altar.particles.enabled", true);
            boolean soundsEnabled = plugin.getConfig().getBoolean("altar.sounds.enabled", true);
            
            if (particlesEnabled) {
                addAltarParticles(player, altarLocation, godType);
            }
            
            if (soundsEnabled) {
                addAltarSounds(player, altarLocation, godType);
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error adding altar effects: " + e.getMessage());
        }
    }
    
    /**
     * Adds particle effects based on god type
     */
    private void addAltarParticles(Player player, Location altarLocation, String godType) {
        Location effectLocation = altarLocation.clone().add(0.5, 1.5, 0.5);
        
        switch (godType.toLowerCase()) {
            case "fallen":
                player.spawnParticle(org.bukkit.Particle.SOUL_FIRE_FLAME, effectLocation, 30, 1.0, 1.0, 1.0, 0.1);
                player.spawnParticle(org.bukkit.Particle.SMOKE, effectLocation, 20, 0.5, 0.5, 0.5, 0.05);
                break;
                
            case "banishment":
                player.spawnParticle(org.bukkit.Particle.FLAME, effectLocation, 25, 1.0, 1.0, 1.0, 0.1);
                player.spawnParticle(org.bukkit.Particle.LAVA, effectLocation, 10, 0.5, 0.5, 0.5, 0.0);
                break;
                
            case "abyssal":
                // WATER_BUBBLE may not exist in this version, fallback to BUBBLE
                player.spawnParticle(org.bukkit.Particle.BUBBLE, effectLocation, 30, 1.0, 1.0, 1.0, 0.1);
                player.spawnParticle(org.bukkit.Particle.DRIPPING_WATER, effectLocation, 15, 0.5, 0.5, 0.5, 0.0);
                break;
                
            case "sylvan":
                player.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, effectLocation, 20, 1.0, 1.0, 1.0, 0.0);
                player.spawnParticle(org.bukkit.Particle.COMPOSTER, effectLocation, 15, 0.5, 0.5, 0.5, 0.1);
                break;
                
            case "tempest":
                player.spawnParticle(org.bukkit.Particle.CLOUD, effectLocation, 25, 1.0, 1.0, 1.0, 0.1);
                player.spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, effectLocation, 15, 0.5, 0.5, 0.5, 0.1);
                break;
                
            case "veil":
                player.spawnParticle(org.bukkit.Particle.PORTAL, effectLocation, 30, 1.0, 1.0, 1.0, 0.1);
                player.spawnParticle(org.bukkit.Particle.END_ROD, effectLocation, 10, 0.5, 0.5, 0.5, 0.05);
                break;
                
            default:
                player.spawnParticle(org.bukkit.Particle.ENCHANT, effectLocation, 20, 1.0, 1.0, 1.0, 0.1);
                break;
        }
    }
    
    /**
     * Adds sound effects based on god type
     */
    private void addAltarSounds(Player player, Location altarLocation, String godType) {
        switch (godType.toLowerCase()) {
            case "fallen":
                // BLOCK_SOUL_FIRE_AMBIENT may not exist, fallback to BLOCK_FIRE_AMBIENT
                player.playSound(altarLocation, org.bukkit.Sound.BLOCK_FIRE_AMBIENT, 1.0f, 0.8f);
                player.playSound(altarLocation, org.bukkit.Sound.ENTITY_WITHER_AMBIENT, 0.5f, 1.2f);
                break;
                
            case "banishment":
                player.playSound(altarLocation, org.bukkit.Sound.BLOCK_FIRE_AMBIENT, 1.0f, 0.8f);
                player.playSound(altarLocation, org.bukkit.Sound.BLOCK_LAVA_POP, 0.7f, 1.0f);
                break;
                
            case "abyssal":
                player.playSound(altarLocation, org.bukkit.Sound.AMBIENT_UNDERWATER_ENTER, 1.0f, 0.8f);
                player.playSound(altarLocation, org.bukkit.Sound.ENTITY_GUARDIAN_AMBIENT, 0.5f, 1.2f);
                break;
                
            case "sylvan":
                player.playSound(altarLocation, org.bukkit.Sound.BLOCK_GRASS_BREAK, 1.0f, 0.8f);
                player.playSound(altarLocation, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.5f);
                break;
                
            case "tempest":
                player.playSound(altarLocation, org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.2f);
                player.playSound(altarLocation, org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.0f);
                break;
                
            case "veil":
                player.playSound(altarLocation, org.bukkit.Sound.BLOCK_PORTAL_AMBIENT, 1.0f, 0.8f);
                player.playSound(altarLocation, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.2f);
                break;
                
            default:
                player.playSound(altarLocation, org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
                break;
        }
    }
    
    /**
     * Formats a location for logging
     */
    private String formatLocation(Location loc) {
        return loc.getWorld().getName() + " (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }
}