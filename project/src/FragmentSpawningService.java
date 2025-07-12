package com.fallengod.testament.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.fallengod.testament.FallenGodPlugin;
import com.fallengod.testament.items.FragmentManager;

/**
 * Unified service for handling fragment spawning from both chests and mob drops
 * Thread-safe implementation with proper cleanup and rate limiting
 */
public class FragmentSpawningService {
    
    private final FallenGodPlugin plugin;
    private final FragmentManager fragmentManager;
    private final TestamentService testamentService;
    private final Logger logger;
    private final FileConfiguration config;
    
    // Configuration
    private final double chestSpawnChance;
    private final double baseMobDropChance;
    private final int minDistanceBetweenFragments;
    private final long playerChestCooldown;
    private final long playerMobCooldown;
    private final int minChestsForFragments;
    
    // Thread-safe tracking
    private final Set<Location> processedChests = ConcurrentHashMap.newKeySet();
    private final Map<String, AtomicLong> lastSpawnTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> playerChestOpens = new ConcurrentHashMap<>();
    private final Map<EntityType, AtomicLong> mobKillCounts = new ConcurrentHashMap<>();
    
    // Mob configuration
    private final Map<EntityType, Double> mobDropMultipliers;
    private final Map<World.Environment, Double> environmentMultipliers;
    private final Map<String, Set<EntityType>> godMobAssociations;
    private final Set<EntityType> bossOnlyMobs;
    private final Set<Material> validChestTypes;
    private final Set<Material> rareChestTypes;
    
    public FragmentSpawningService(FallenGodPlugin plugin, FragmentManager fragmentManager, TestamentService testamentService) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        this.testamentService = testamentService;
        this.logger = plugin.getLogger();
        this.config = plugin.getConfig();
        
        // Load configuration
        this.chestSpawnChance = config.getDouble("testament.fragments.chest_spawn_chance", 0.02);
        this.baseMobDropChance = config.getDouble("testament.fragments.mob_drop_chance", 0.001);
        this.minDistanceBetweenFragments = config.getInt("testament.fragments.min_distance", 2000);
        this.playerChestCooldown = config.getLong("testament.fragments.player_chest_cooldown", 7200000); // 2 hours
        this.playerMobCooldown = config.getLong("testament.fragments.player_cooldown", 3600000); // 1 hour
        this.minChestsForFragments = config.getInt("testament.fragments.min_chests_for_fragments", 50);
        
        // Initialize collections
        this.mobDropMultipliers = initializeMobMultipliers();
        this.environmentMultipliers = initializeEnvironmentMultipliers();
        this.godMobAssociations = initializeGodMobAssociations();
        this.bossOnlyMobs = initializeBossOnlyMobs();
        this.validChestTypes = initializeValidChestTypes();
        this.rareChestTypes = initializeRareChestTypes();
        
        logger.info("Fragment spawning service initialized - Chest: " + (chestSpawnChance * 100) + "%, Mob: " + (baseMobDropChance * 100) + "%");
    }
    
    /**
     * Handles chest opening for fragment spawning
     */
    public void handleChestOpen(Location chestLocation, Player player) {
        if (chestLocation == null || player == null) return;
        
        // Check if already processed
        if (processedChests.contains(chestLocation)) {
            return;
        }
        
        try {
            // Track player chest opens
            String playerKey = player.getUniqueId().toString();
            playerChestOpens.computeIfAbsent(playerKey, k -> new AtomicLong(0)).incrementAndGet();
            
            // Check rarity requirements
            if (!passesChestRarityCheck(player, chestLocation)) {
                processedChests.add(chestLocation);
                return;
            }
            
            // Calculate spawn chance
            double effectiveChance = calculateChestSpawnChance(chestLocation, player);
            
            if (ThreadLocalRandom.current().nextDouble() > effectiveChance) {
                processedChests.add(chestLocation);
                return;
            }
            
            // Check location validity
            if (!isValidSpawnLocation(chestLocation)) {
                processedChests.add(chestLocation);
                return;
            }
            
            // Additional rarity gate
            if (ThreadLocalRandom.current().nextDouble() > 0.33) {
                processedChests.add(chestLocation);
                return;
            }
            
            // Spawn fragment
            spawnFragmentInChest(chestLocation, player);
            processedChests.add(chestLocation);
            
        } catch (Exception e) {
            logger.warning("Error handling chest open: " + e.getMessage());
            processedChests.add(chestLocation);
        }
    }
    
    /**
     * Handles mob death for fragment drops
     */
    public void handleMobDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        
        if (!(entity instanceof LivingEntity) || event.getEntity().getKiller() == null) {
            return;
        }
        
        Player killer = event.getEntity().getKiller();
        EntityType entityType = entity.getType();
        
        try {
            // Track mob kills
            mobKillCounts.computeIfAbsent(entityType, k -> new AtomicLong(0)).incrementAndGet();
            
            // Check if mob can drop fragments
            if (!canMobDropFragment(entityType)) {
                return;
            }
            
            // Calculate drop chance
            double dropChance = calculateMobDropChance(entityType, entity.getWorld().getEnvironment(), killer);
            
            if (ThreadLocalRandom.current().nextDouble() > dropChance) {
                return;
            }
            
            // Check rarity requirements
            if (!passesMobRarityCheck(entityType, killer)) {
                return;
            }
            
            // Determine god type and fragment
            String godType = determineGodTypeForMob(entityType);
            if (godType == null) {
                // Random god for non-associated mobs (very rare)
                if (ThreadLocalRandom.current().nextDouble() > 0.1) {
                    return;
                }
                List<String> gods = fragmentManager.getValidGodTypes();
                godType = gods.get(ThreadLocalRandom.current().nextInt(gods.size()));
            }
            
            int fragmentNumber = getWeightedFragmentNumber();
            
            // Create and drop fragment
            ItemStack fragment = fragmentManager.createFragment(godType, fragmentNumber);
            if (fragment != null) {
                event.getDrops().add(fragment);
                
                // Record drop
                recordFragmentDrop(killer, "mob");
                
                // Notify player
                notifyFragmentDrop(killer, godType, fragmentNumber, entityType);
                
                // Update player progress
                testamentService.onFragmentObtained(killer, godType, fragmentNumber);
                
                logger.info(String.format("Fragment drop: %s fragment %d from %s killed by %s", 
                    godType, fragmentNumber, entityType, killer.getName()));
            }
            
        } catch (Exception e) {
            logger.warning("Error handling mob death: " + e.getMessage());
        }
    }
    
    private boolean passesChestRarityCheck(Player player, Location chestLocation) {
        String playerKey = player.getUniqueId().toString();
        
        // Check player cooldown
        AtomicLong lastSpawn = lastSpawnTimes.get(playerKey + "_chest");
        if (lastSpawn != null) {
            long timeSince = System.currentTimeMillis() - lastSpawn.get();
            if (timeSince < playerChestCooldown) {
                return false;
            }
        }
        
        // Check minimum exploration requirement
        AtomicLong chestsOpened = playerChestOpens.get(playerKey);
        return chestsOpened != null && chestsOpened.get() >= minChestsForFragments;
    }
    
    private boolean passesMobRarityCheck(EntityType entityType, Player killer) {
        String playerKey = killer.getUniqueId().toString();
        
        // Check player cooldown
        AtomicLong lastDrop = lastSpawnTimes.get(playerKey + "_mob");
        if (lastDrop != null) {
            long timeSince = System.currentTimeMillis() - lastDrop.get();
            if (timeSince < playerMobCooldown) {
                return false;
            }
        }
        
        // Boss mobs always pass
        if (bossOnlyMobs.contains(entityType)) {
            return true;
        }
        
        // Additional random check for regular mobs
        return ThreadLocalRandom.current().nextDouble() < 0.3;
    }
    
    private double calculateChestSpawnChance(Location chestLocation, Player player) {
        double chance = chestSpawnChance;
        World world = chestLocation.getWorld();
        
        // Environment multiplier
        chance *= environmentMultipliers.getOrDefault(world.getEnvironment(), 1.0);
        
        // Rare chest bonus
        Block block = chestLocation.getBlock();
        if (rareChestTypes.contains(block.getType())) {
            chance *= 2.0;
        }
        
        // Depth bonus
        if (world.getEnvironment() == World.Environment.NORMAL) {
            int y = chestLocation.getBlockY();
            if (y < 0) {
                double depthMultiplier = 1.0 + Math.abs(y) * 0.01;
                chance *= Math.min(depthMultiplier, 2.0);
            }
        }
        
        // Exploration bonus
        String playerKey = player.getUniqueId().toString();
        AtomicLong chestsOpened = playerChestOpens.get(playerKey);
        if (chestsOpened != null && chestsOpened.get() > 100) {
            double explorationBonus = 1.0 + Math.min((chestsOpened.get() - 100) * 0.001, 0.5);
            chance *= explorationBonus;
        }
        
        return Math.min(chance, 0.25); // Cap at 25%
    }
    
    private double calculateMobDropChance(EntityType entityType, World.Environment environment, Player killer) {
        double chance = baseMobDropChance;
        
        // Mob multiplier
        chance *= mobDropMultipliers.getOrDefault(entityType, 1.0);
        
        // Environment multiplier
        chance *= environmentMultipliers.getOrDefault(environment, 1.0);
        
        // Kill count bonus
        AtomicLong killCount = mobKillCounts.get(entityType);
        if (killCount != null && killCount.get() > 10) {
            double bonus = Math.min(1.5, 1.0 + (killCount.get() - 10) * 0.01);
            chance *= bonus;
        }
        
        return Math.min(chance, 0.1); // Cap at 10%
    }
    
    private void spawnFragmentInChest(Location chestLocation, Player player) {
        Block block = chestLocation.getBlock();
        if (!(block.getState() instanceof Chest)) {
            return;
        }
        
        Chest chest = (Chest) block.getState();
        Inventory inventory = chest.getInventory();
        
        // Determine god type and fragment
        String godType = determineGodTypeByLocation(chestLocation);
        int fragmentNumber = getWeightedFragmentNumber();
        
        // Create fragment
        ItemStack fragment = fragmentManager.createFragment(godType, fragmentNumber);
        if (fragment != null && addItemToChest(inventory, fragment)) {
            chest.update();
            
            // Record spawn
            recordFragmentSpawn(chestLocation, player);
            
            // Notify player
            notifyChestFragmentSpawn(player, godType, fragmentNumber, chestLocation);
            
            // Update player progress
            testamentService.onFragmentObtained(player, godType, fragmentNumber);
            
            logger.info(String.format("Chest fragment: %s fragment %d spawned at %s for %s", 
                godType, fragmentNumber, formatLocation(chestLocation), player.getName()));
        }
    }
    
    private void notifyChestFragmentSpawn(Player player, String godType, int fragmentNumber, Location location) {
        player.sendMessage("§6§l✦ DIVINE DISCOVERY ✦");
        player.sendMessage("§e§lAncient power resonates from within this chest!");
        player.sendMessage("§7A fragment of " + godType.toUpperCase() + " testament awaits...");
        player.sendMessage("§8Fragment " + fragmentNumber + " of 7");
        
        // Particle effects
        player.spawnParticle(org.bukkit.Particle.SOUL_FIRE_FLAME, 
            location.clone().add(0.5, 1, 0.5), 20, 0.5, 0.5, 0.5, 0.1);
    }
    
    private void notifyFragmentDrop(Player player, String godType, int fragmentNumber, EntityType mobType) {
        player.sendMessage("§6§l✦ DIVINE FRAGMENT DISCOVERED ✦");
        player.sendMessage("§e§lThe " + mobType.name().toLowerCase().replace("_", " ") + 
                         " releases a fragment of " + godType.toUpperCase() + " power!");
        player.sendMessage("§7Fragment " + fragmentNumber + " of 7 - A piece of divine testament!");
        
        // Server announcement for boss drops
        if (bossOnlyMobs.contains(mobType)) {
            org.bukkit.Bukkit.broadcastMessage("§6§l⚡ " + player.getName() + " has obtained a divine fragment from the " + 
                mobType.name().toLowerCase().replace("_", " ") + "! ⚡");
        }
    }
    
    // Cleanup and utility methods
    public void shutdown() {
        // Clear tracking data
        processedChests.clear();
        lastSpawnTimes.clear();
        playerChestOpens.clear();
        mobKillCounts.clear();
        
        logger.info("Fragment spawning service shut down");
    }
    
    public void clearProcessedChests() {
        processedChests.clear();
        lastSpawnTimes.clear();
        playerChestOpens.clear();
        logger.info("Cleared processed chest cache");
    }
    
    public Map<String, Object> getSpawningStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("processed_chests", processedChests.size());
        stats.put("recent_spawns", lastSpawnTimes.size());
        stats.put("chest_spawn_chance", chestSpawnChance);
        stats.put("mob_drop_chance", baseMobDropChance);
        stats.put("total_player_opens", playerChestOpens.values().stream().mapToLong(AtomicLong::get).sum());
        stats.put("total_mob_kills", mobKillCounts.values().stream().mapToLong(AtomicLong::get).sum());
        return stats;
    }
    
    // Helper methods for initialization
    private Map<EntityType, Double> initializeMobMultipliers() {
        Map<EntityType, Double> multipliers = new HashMap<>();
        multipliers.put(EntityType.ENDER_DRAGON, 25.0);
        multipliers.put(EntityType.WITHER, 20.0);
        multipliers.put(EntityType.ELDER_GUARDIAN, 15.0);
        multipliers.put(EntityType.WARDEN, 12.0);
        multipliers.put(EntityType.EVOKER, 10.0);
        multipliers.put(EntityType.WITHER_SKELETON, 8.0);
        multipliers.put(EntityType.SHULKER, 6.0);
        multipliers.put(EntityType.BLAZE, 4.0);
        multipliers.put(EntityType.GHAST, 3.0);
        multipliers.put(EntityType.ENDERMAN, 2.0);
        multipliers.put(EntityType.GUARDIAN, 2.0);
        multipliers.put(EntityType.PIGLIN_BRUTE, 2.0);
        return multipliers;
    }
    
    private Map<World.Environment, Double> initializeEnvironmentMultipliers() {
        Map<World.Environment, Double> multipliers = new HashMap<>();
        multipliers.put(World.Environment.NORMAL, 1.0);
        multipliers.put(World.Environment.NETHER, 2.0);
        multipliers.put(World.Environment.THE_END, 3.0);
        return multipliers;
    }
    
    private Set<EntityType> initializeBossOnlyMobs() {
        Set<EntityType> bosses = new HashSet<>();
        bosses.add(EntityType.ENDER_DRAGON);
        bosses.add(EntityType.WITHER);
        bosses.add(EntityType.ELDER_GUARDIAN);
        bosses.add(EntityType.WARDEN);
        return bosses;
    }
    
    private Set<Material> initializeValidChestTypes() {
        Set<Material> types = new HashSet<>();
        types.add(Material.CHEST);
        types.add(Material.TRAPPED_CHEST);
        types.add(Material.BARREL);
        types.add(Material.SHULKER_BOX);
        return types;
    }
    
    private Set<Material> initializeRareChestTypes() {
        Set<Material> types = new HashSet<>();
        types.add(Material.ENDER_CHEST);
        types.add(Material.SHULKER_BOX);
        return types;
    }
    
    private Map<String, Set<EntityType>> initializeGodMobAssociations() {
        Map<String, Set<EntityType>> associations = new HashMap<>();
        
        // Fallen God - Death and undead
        Set<EntityType> fallenMobs = new HashSet<>();
        fallenMobs.add(EntityType.WITHER);
        fallenMobs.add(EntityType.WITHER_SKELETON);
        fallenMobs.add(EntityType.SKELETON);
        fallenMobs.add(EntityType.ZOMBIE);
        fallenMobs.add(EntityType.PHANTOM);
        fallenMobs.add(EntityType.ZOMBIFIED_PIGLIN);
        associations.put("fallen", fallenMobs);
        
        // Banishment God - Nether entities
        Set<EntityType> banishmentMobs = new HashSet<>();
        banishmentMobs.add(EntityType.BLAZE);
        banishmentMobs.add(EntityType.GHAST);
        banishmentMobs.add(EntityType.MAGMA_CUBE);
        banishmentMobs.add(EntityType.HOGLIN);
        banishmentMobs.add(EntityType.PIGLIN_BRUTE);
        banishmentMobs.add(EntityType.WITHER);
        associations.put("banishment", banishmentMobs);
        
        // Abyssal God - Ocean depths
        Set<EntityType> abyssalMobs = new HashSet<>();
        abyssalMobs.add(EntityType.ELDER_GUARDIAN);
        abyssalMobs.add(EntityType.GUARDIAN);
        abyssalMobs.add(EntityType.DROWNED);
        abyssalMobs.add(EntityType.SQUID);
        abyssalMobs.add(EntityType.GLOW_SQUID);
        abyssalMobs.add(EntityType.WARDEN);
        associations.put("abyssal", abyssalMobs);
        
        // Sylvan God - Nature creatures
        Set<EntityType> sylvanMobs = new HashSet<>();
        sylvanMobs.add(EntityType.EVOKER);
        sylvanMobs.add(EntityType.VINDICATOR);
        sylvanMobs.add(EntityType.WITCH);
        sylvanMobs.add(EntityType.CREEPER);
        sylvanMobs.add(EntityType.SPIDER);
        sylvanMobs.add(EntityType.CAVE_SPIDER);
        associations.put("sylvan", sylvanMobs);
        
        // Tempest God - Sky creatures
        Set<EntityType> tempestMobs = new HashSet<>();
        tempestMobs.add(EntityType.PHANTOM);
        tempestMobs.add(EntityType.VEX);
        tempestMobs.add(EntityType.GHAST);
        tempestMobs.add(EntityType.BLAZE);
        tempestMobs.add(EntityType.EVOKER);
        associations.put("tempest", tempestMobs);
        
        // Veil God - End dimension
        Set<EntityType> veilMobs = new HashSet<>();
        veilMobs.add(EntityType.ENDER_DRAGON);
        veilMobs.add(EntityType.ENDERMAN);
        veilMobs.add(EntityType.ENDERMITE);
        veilMobs.add(EntityType.SHULKER);
        veilMobs.add(EntityType.SILVERFISH);
        associations.put("veil", veilMobs);
        
        return associations;
    }
    
    // Additional helper methods
    private boolean canMobDropFragment(EntityType entityType) {
        return mobDropMultipliers.containsKey(entityType) || 
               godMobAssociations.values().stream().anyMatch(set -> set.contains(entityType));
    }
    
    private String determineGodTypeForMob(EntityType entityType) {
        for (Map.Entry<String, Set<EntityType>> entry : godMobAssociations.entrySet()) {
            if (entry.getValue().contains(entityType)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    private String determineGodTypeByLocation(Location location) {
        World.Environment env = location.getWorld().getEnvironment();
        int y = location.getBlockY();
        
        switch (env) {
            case NETHER:
                return ThreadLocalRandom.current().nextBoolean() ? "banishment" : "fallen";
            case THE_END:
                return "veil";
            case NORMAL:
            default:
                if (y < -20) return "abyssal";
                if (y < 30) return "fallen";
                if (y > 100) return "tempest";
                return "sylvan";
        }
    }
    
    private int getWeightedFragmentNumber() {
        double rand = ThreadLocalRandom.current().nextDouble();
        
        if (rand < 0.30) return 1;  // 30%
        if (rand < 0.50) return 2;  // 20%
        if (rand < 0.65) return 3;  // 15%
        if (rand < 0.78) return 4;  // 13%
        if (rand < 0.88) return 5;  // 10%
        if (rand < 0.95) return 6;  // 7%
        return 7;                   // 5%
    }
    
    private boolean addItemToChest(Inventory inventory, ItemStack item) {
        // Try center slots first
        int[] preferredSlots = {13, 12, 14, 11, 15, 4, 22};
        
        for (int slot : preferredSlots) {
            if (slot < inventory.getSize()) {
                ItemStack existing = inventory.getItem(slot);
                if (existing == null || existing.getType() == Material.AIR) {
                    inventory.setItem(slot, item);
                    return true;
                }
            }
        }
        
        // Fallback to any empty slot
        HashMap<Integer, ItemStack> leftover = inventory.addItem(item);
        return leftover.isEmpty();
    }
    
    private boolean isValidSpawnLocation(Location location) {
        String worldName = location.getWorld().getName();
        String locationKey = worldName + ":" + location.getBlockX() + ":" + location.getBlockZ();
        
        // Check time cooldown
        AtomicLong lastSpawn = lastSpawnTimes.get(locationKey);
        if (lastSpawn != null) {
            long timeSince = System.currentTimeMillis() - lastSpawn.get();
            if (timeSince < 14400000) { // 4 hours
                return false;
            }
        }
        
        // Check distance from other spawns
        for (String key : lastSpawnTimes.keySet()) {
            if (key.startsWith(worldName + ":")) {
                String[] parts = key.split(":");
                if (parts.length >= 3) {
                    try {
                        int x = Integer.parseInt(parts[1]);
                        int z = Integer.parseInt(parts[2]);
                        double distance = Math.sqrt(Math.pow(location.getBlockX() - x, 2) + 
                                                  Math.pow(location.getBlockZ() - z, 2));
                        
                        if (distance < minDistanceBetweenFragments) {
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid entries
                    }
                }
            }
        }
        
        return true;
    }
    
    private void recordFragmentSpawn(Location location, Player player) {
        String worldName = location.getWorld().getName();
        String locationKey = worldName + ":" + location.getBlockX() + ":" + location.getBlockZ();
        String playerKey = player.getUniqueId().toString();
        
        long currentTime = System.currentTimeMillis();
        lastSpawnTimes.put(locationKey, new AtomicLong(currentTime));
        lastSpawnTimes.put(playerKey + "_chest", new AtomicLong(currentTime));
        
        // Cleanup old entries
        cleanupOldEntries();
    }
    
    private void recordFragmentDrop(Player player, String type) {
        String playerKey = player.getUniqueId().toString();
        lastSpawnTimes.put(playerKey + "_" + type, new AtomicLong(System.currentTimeMillis()));
        
        // Cleanup old entries
        cleanupOldEntries();
    }
    
    private void cleanupOldEntries() {
        long cutoff = System.currentTimeMillis() - (48 * 60 * 60 * 1000); // 48 hours
        lastSpawnTimes.entrySet().removeIf(entry -> entry.getValue().get() < cutoff);
    }
    
    private String formatLocation(Location loc) {
        return loc.getWorld().getName() + " (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }
}