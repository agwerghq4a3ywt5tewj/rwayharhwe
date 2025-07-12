package com.fallengod.testament.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import com.fallengod.testament.FallenGodPlugin;

/**
 * Manages automatic altar placement in the world with natural generation
 * Enhanced for new world generation support
 */
public class AltarPlacementManager {
    
    private final FallenGodPlugin plugin;
    private final Logger logger;
    private final FileConfiguration config;
    private final Set<String> generatedAltars;
    private final Map<String, Location> altarLocations;
    
    // Generation settings
    private final boolean autoGenerate;
    private final int minDistance;
    private final int maxAttempts;
    private final boolean forceGeneration;
    
    public AltarPlacementManager(FallenGodPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = plugin.getConfig();
        this.generatedAltars = new HashSet<>();
        this.altarLocations = new HashMap<>();
        
        // Load configuration
        this.autoGenerate = config.getBoolean("forge.auto_generate", true);
        this.minDistance = config.getInt("forge.min_distance", 5000);
        this.maxAttempts = config.getInt("forge.max_attempts", 100);
        this.forceGeneration = config.getBoolean("forge.force_generation_new_worlds", true);
        
        logger.info(String.format("Altar generation configured - Auto: %b, Min Distance: %d, Max Attempts: %d", 
            autoGenerate, minDistance, maxAttempts));
    }
    
    /**
     * Generates altars in the world naturally - ASYNC to prevent crashes
     * Enhanced for new world generation
     */
    public void generateAltars(World world) {
        if (!autoGenerate) {
            logger.info(String.format("Auto-generation disabled for world: %s", world.getName()));
            return;
        }
        
        // Run altar generation async to prevent main thread blocking
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            generateAltarsAsync(world);
        });
    }
    
    /**
     * Generates altars specifically for new worlds with enhanced placement logic
     */
    public void generateAltarsForNewWorld(World world) {
        if (!autoGenerate && !forceGeneration) {
            logger.info(String.format("Altar generation disabled for new world: %s", world.getName()));
            return;
        }
        
        logger.info(String.format("Generating altars for NEW WORLD: %s with enhanced placement", world.getName()));
        
        // Run with higher priority for new worlds
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            generateAltarsAsyncNewWorld(world);
        });
    }
    
    private void generateAltarsAsync(World world) {
        logger.info(String.format("Generating altars in world: %s", world.getName()));
        
        String[] godTypes = {"fallen", "banishment", "abyssal", "sylvan", "tempest", "veil"};
        
        for (String godType : godTypes) {
            String altarKey = world.getName() + "_" + godType;
            
            // Skip if already generated
            if (altarLocations.containsKey(altarKey)) {
                continue;
            }
            
            try {
                Location altarLocation = findSuitableLocation(world, godType);
                if (altarLocation != null) {
                    // Build altar on main thread
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        buildAltar(altarLocation, godType);
                        plugin.getAltarService().registerAltar(godType, altarLocation);
                        generatedAltars.add(altarKey);
                        altarLocations.put(altarKey, altarLocation);
                        logger.info(String.format("Generated %s altar at %s", godType, formatLocation(altarLocation)));
                    });
                } else {
                    logger.warning(String.format("Could not find suitable location for %s altar in world %s", godType, world.getName()));
                }
            } catch (Exception e) {
                logger.severe(String.format("Error generating %s altar: %s", godType, e.getMessage()));
                e.printStackTrace();
            }
            
            // Small delay between altar generations to prevent overload
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Enhanced altar generation for new worlds with better placement logic
     */
    private void generateAltarsAsyncNewWorld(World world) {
        logger.info(String.format("Enhanced altar generation for NEW WORLD: %s", world.getName()));
        
        String[] godTypes = {"fallen", "banishment", "abyssal", "sylvan", "tempest", "veil"};
        List<Location> chosenLocations = new ArrayList<>();
        
        // Pre-select all locations to ensure good distribution
        for (String godType : godTypes) {
            String altarKey = world.getName() + "_" + godType;
            
            // Skip if already generated
            if (altarLocations.containsKey(altarKey)) {
                continue;
            }
            
            try {
                Location altarLocation = findSuitableLocationNewWorld(world, godType, chosenLocations);
                if (altarLocation != null) {
                    chosenLocations.add(altarLocation);
                    
                    // Build altar on main thread
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        buildAltar(altarLocation, godType);
                        plugin.getAltarService().registerAltar(godType, altarLocation);
                        generatedAltars.add(altarKey);
                        altarLocations.put(altarKey, altarLocation);
                        logger.info(String.format("Generated %s altar at %s (NEW WORLD)", godType, formatLocation(altarLocation)));
                        
                        // Send server-wide announcement for new world altars
                        org.bukkit.Bukkit.broadcastMessage(String.format("§6§l⚡ %s Altar has been discovered in the new world %s! ⚡", 
                                capitalizeFirst(godType), world.getName()));
                    });
                } else {
                    logger.warning(String.format("Could not find suitable location for %s altar in NEW WORLD %s after %d attempts", 
                        godType, world.getName(), maxAttempts));
                }
            } catch (Exception e) {
                logger.severe(String.format("Error generating %s altar in NEW WORLD: %s", godType, e.getMessage()));
                e.printStackTrace();
            }
            
            // Shorter delay for new world generation
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Final announcement
        Bukkit.getScheduler().runTask(plugin, () -> {
            org.bukkit.Bukkit.broadcastMessage(String.format("§a§lAll divine altars have been established in the new world: %s!", world.getName()));
            org.bukkit.Bukkit.broadcastMessage("§7Seek out these sacred sites to complete your testament quests!");
        });
    }
    
    private Location findSuitableLocation(World world, String godType) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        for (int attempts = 0; attempts < 50; attempts++) {
            int x = random.nextInt(-1000, 1000);
            int z = random.nextInt(-1000, 1000);
            
            Location surface = world.getHighestBlockAt(x, z).getLocation().add(0, 1, 0);
            
            if (isValidAltarLocation(surface, godType)) {
                return surface;
            }
        }
        
        return null;
    }
    
    /**
     * Enhanced location finding for new worlds with better distribution
     */
    private Location findSuitableLocationNewWorld(World world, String godType, List<Location> existingLocations) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        // Larger search area for new worlds
        int searchRadius = config.getInt("forge.new_world_search_radius", 2000);
        
        for (int attempts = 0; attempts < maxAttempts; attempts++) {
            int x = random.nextInt(-searchRadius, searchRadius);
            int z = random.nextInt(-searchRadius, searchRadius);
            
            // Get surface location
            Location surface = world.getHighestBlockAt(x, z).getLocation().add(0, 1, 0);
            
            // Enhanced validation for new worlds
            if (isValidAltarLocationNewWorld(surface, godType, existingLocations)) {
                return surface;
            }
        }
        
        // If we can't find a perfect location, try with relaxed constraints
        logger.warning(String.format("Relaxing constraints for %s altar in new world %s", godType, world.getName()));
        
        for (int attempts = 0; attempts < maxAttempts / 2; attempts++) {
            int x = random.nextInt(-searchRadius / 2, searchRadius / 2);
            int z = random.nextInt(-searchRadius / 2, searchRadius / 2);
            
            Location surface = world.getHighestBlockAt(x, z).getLocation().add(0, 1, 0);
            
            if (isValidAltarLocationRelaxed(surface, godType, existingLocations)) {
                return surface;
            }
        }
        
        return null;
    }
    
    private boolean isValidAltarLocation(Location location, String godType) {
        // Check minimum distance from other altars
        for (Location existingAltar : altarLocations.values()) {
            if (existingAltar.getWorld().equals(location.getWorld()) &&
                existingAltar.distance(location) < minDistance) {
                return false;
            }
        }
        
        // Check if there's enough space
        return hasEnoughSpace(location, 7, 4, 7);
    }
    
    /**
     * Enhanced validation for new world altar placement
     */
    private boolean isValidAltarLocationNewWorld(Location location, String godType, List<Location> existingLocations) {
        // Check distance from other altars in this generation session
        for (Location existingLocation : existingLocations) {
            if (existingLocation.distance(location) < minDistance) {
                return false;
            }
        }
        
        // Check distance from previously generated altars
        for (Location existingAltar : altarLocations.values()) {
            if (existingAltar.getWorld().equals(location.getWorld()) &&
                existingAltar.distance(location) < minDistance) {
                return false;
            }
        }
        
        // Enhanced space checking
        if (!hasEnoughSpace(location, 7, 4, 7)) {
            return false;
        }
        
        // Biome-based placement preferences for new worlds
        return isSuitableBiomeForGod(location, godType);
    }
    
    /**
     * Relaxed validation when strict validation fails
     */
    private boolean isValidAltarLocationRelaxed(Location location, String godType, List<Location> existingLocations) {
        // Reduced minimum distance
        int relaxedDistance = minDistance / 2;
        
        for (Location existingLocation : existingLocations) {
            if (existingLocation.distance(location) < relaxedDistance) {
                return false;
            }
        }
        
        // Basic space checking
        return hasEnoughSpace(location, 7, 4, 7);
    }
    
    /**
     * Checks if a biome is suitable for a specific god type
     */
    private boolean isSuitableBiomeForGod(Location location, String godType) {
        org.bukkit.block.Biome biome = location.getWorld().getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        
        switch (godType.toLowerCase()) {
            case "fallen":
                // Prefer dark/spooky biomes
                return biome.name().contains("SWAMP") || biome.name().contains("DARK") || 
                       biome.name().contains("SOUL") || biome == org.bukkit.block.Biome.DEEP_DARK;
                
            case "banishment":
                // Prefer hot/desert biomes
                return biome.name().contains("DESERT") || biome.name().contains("BADLANDS") || 
                       biome.name().contains("SAVANNA");
                
            case "abyssal":
                // Prefer ocean/water biomes
                return biome.name().contains("OCEAN") || biome.name().contains("RIVER") || 
                       biome.name().contains("BEACH");
                
            case "sylvan":
                // Prefer forest biomes
                return biome.name().contains("FOREST") || biome.name().contains("JUNGLE") || 
                       biome.name().contains("TAIGA");
                
            case "tempest":
                // Prefer mountain/hill biomes
                return biome.name().contains("MOUNTAIN") || biome.name().contains("HILL") || 
                       biome.name().contains("PEAK") || biome.name().contains("PLATEAU");
                
            case "veil":
                // Prefer rare/end-like biomes, but accept any since it's rare
                return true; // Veil god is mysterious and can appear anywhere
                
            default:
                return true;
        }
    }
    
    private boolean hasEnoughSpace(Location center, int width, int height, int depth) {
        World world = center.getWorld();
        
        for (int x = -width/2; x <= width/2; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = -depth/2; z <= depth/2; z++) {
                    Location checkLoc = center.clone().add(x, y, z);
                    Block block = world.getBlockAt(checkLoc);
                    
                    if (y == 0) {
                        if (!block.getType().isSolid()) {
                            return false;
                        }
                    } else {
                        if (block.getType().isSolid() && !isReplaceable(block.getType())) {
                            return false;
                        }
                    }
                }
            }
        }
        
        return true;
    }
    
    private boolean isReplaceable(Material material) {
        return material == Material.AIR || 
               material == Material.WATER ||
               material == Material.LAVA ||
               material == Material.SHORT_GRASS ||
               material == Material.TALL_GRASS ||
               material == Material.FERN ||
               material == Material.LARGE_FERN;
    }
    
    /**
     * Builds an altar structure at the specified location
     */
    public void buildAltar(Location location, String godType) {
        try {
            logger.info(String.format("Building %s altar at %s", godType, formatLocation(location)));
            
            World world = location.getWorld();
            
            // Build altar based on god type
            switch (godType.toLowerCase()) {
                case "fallen":
                    buildFallenAltar(world, location);
                    break;
                case "banishment":
                    buildBanishmentAltar(world, location);
                    break;
                case "abyssal":
                    buildAbyssalAltar(world, location);
                    break;
                case "sylvan":
                    buildSylvanAltar(world, location);
                    break;
                case "tempest":
                    buildTempestAltar(world, location);
                    break;
                case "veil":
                    buildVeilAltar(world, location);
                    break;
            }
        } catch (Exception e) {
            logger.severe("Error building altar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Builds Fallen God altar (dark, underground theme)
     */
    private void buildFallenAltar(World world, Location center) {
        buildPlatform(world, center, Material.BLACKSTONE, 7);
        world.getBlockAt(center).setType(Material.CRYING_OBSIDIAN);
        world.getBlockAt(center.clone().add(0, 1, 0)).setType(Material.SOUL_FIRE);
        
        buildPillar(world, center.clone().add(3, 0, 3), Material.BLACKSTONE, 3);
        buildPillar(world, center.clone().add(-3, 0, 3), Material.BLACKSTONE, 3);
        buildPillar(world, center.clone().add(3, 0, -3), Material.BLACKSTONE, 3);
        buildPillar(world, center.clone().add(-3, 0, -3), Material.BLACKSTONE, 3);
        
        world.getBlockAt(center.clone().add(3, 3, 3)).setType(Material.SOUL_FIRE);
        world.getBlockAt(center.clone().add(-3, 3, 3)).setType(Material.SOUL_FIRE);
        world.getBlockAt(center.clone().add(3, 3, -3)).setType(Material.SOUL_FIRE);
        world.getBlockAt(center.clone().add(-3, 3, -3)).setType(Material.SOUL_FIRE);
    }
    
    /**
     * Builds Banishment God altar (fire theme)
     */
    private void buildBanishmentAltar(World world, Location center) {
        buildPlatform(world, center, Material.NETHER_BRICKS, 7);
        world.getBlockAt(center).setType(Material.MAGMA_BLOCK);
        world.getBlockAt(center.clone().add(0, 1, 0)).setType(Material.FIRE);
        
        world.getBlockAt(center.clone().add(2, 0, 2)).setType(Material.LAVA);
        world.getBlockAt(center.clone().add(-2, 0, 2)).setType(Material.LAVA);
        world.getBlockAt(center.clone().add(2, 0, -2)).setType(Material.LAVA);
        world.getBlockAt(center.clone().add(-2, 0, -2)).setType(Material.LAVA);
    }
    
    /**
     * Builds Abyssal God altar (water theme)
     */
    private void buildAbyssalAltar(World world, Location center) {
        buildPlatform(world, center, Material.PRISMARINE, 7);
        world.getBlockAt(center).setType(Material.DARK_PRISMARINE);
        world.getBlockAt(center.clone().add(0, 1, 0)).setType(Material.SEA_LANTERN);
        
        world.getBlockAt(center.clone().add(2, 1, 2)).setType(Material.WATER);
        world.getBlockAt(center.clone().add(-2, 1, 2)).setType(Material.WATER);
        world.getBlockAt(center.clone().add(2, 1, -2)).setType(Material.WATER);
        world.getBlockAt(center.clone().add(-2, 1, -2)).setType(Material.WATER);
    }
    
    /**
     * Builds Sylvan God altar (nature theme)
     */
    private void buildSylvanAltar(World world, Location center) {
        buildPlatform(world, center, Material.MOSS_BLOCK, 7);
        world.getBlockAt(center).setType(Material.OAK_LOG);
        world.getBlockAt(center.clone().add(0, 1, 0)).setType(Material.OAK_LEAVES);
        
        world.getBlockAt(center.clone().add(2, 1, 2)).setType(Material.OAK_SAPLING);
        world.getBlockAt(center.clone().add(-2, 1, 2)).setType(Material.BIRCH_SAPLING);
        world.getBlockAt(center.clone().add(2, 1, -2)).setType(Material.SPRUCE_SAPLING);
        world.getBlockAt(center.clone().add(-2, 1, -2)).setType(Material.JUNGLE_SAPLING);
    }
    
    /**
     * Builds Tempest God altar (sky/storm theme)
     */
    private void buildTempestAltar(World world, Location center) {
        buildPlatform(world, center, Material.QUARTZ_BLOCK, 7);
        world.getBlockAt(center).setType(Material.LIGHTNING_ROD);
        
        world.getBlockAt(center.clone().add(2, 1, 0)).setType(Material.WHITE_WOOL);
        world.getBlockAt(center.clone().add(-2, 1, 0)).setType(Material.WHITE_WOOL);
        world.getBlockAt(center.clone().add(0, 1, 2)).setType(Material.WHITE_WOOL);
        world.getBlockAt(center.clone().add(0, 1, -2)).setType(Material.WHITE_WOOL);
    }
    
    /**
     * Builds Veil God altar (void/end theme)
     */
    private void buildVeilAltar(World world, Location center) {
        buildPlatform(world, center, Material.END_STONE, 7);
        world.getBlockAt(center).setType(Material.END_PORTAL_FRAME);
        world.getBlockAt(center.clone().add(0, 1, 0)).setType(Material.ENDER_CHEST);
        
        world.getBlockAt(center.clone().add(3, 1, 3)).setType(Material.END_ROD);
        world.getBlockAt(center.clone().add(-3, 1, 3)).setType(Material.END_ROD);
        world.getBlockAt(center.clone().add(3, 1, -3)).setType(Material.END_ROD);
        world.getBlockAt(center.clone().add(-3, 1, -3)).setType(Material.END_ROD);
    }
    
    /**
     * Builds a platform of specified material and radius
     */
    private void buildPlatform(World world, Location center, Material material, int radius) {
        for (int x = -radius/2; x <= radius/2; x++) {
            for (int z = -radius/2; z <= radius/2; z++) {
                Location blockLoc = center.clone().add(x, -1, z);
                world.getBlockAt(blockLoc).setType(material);
            }
        }
    }
    
    /**
     * Builds a pillar of specified material and height
     */
    private void buildPillar(World world, Location base, Material material, int height) {
        for (int y = 0; y < height; y++) {
            world.getBlockAt(base.clone().add(0, y, 0)).setType(material);
        }
    }
    
    /**
     * Gets the location of a generated altar
     */
    public Location getAltarLocation(String godType) {
        for (Map.Entry<String, Location> entry : altarLocations.entrySet()) {
            if (entry.getKey().endsWith("_" + godType)) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    /**
     * Manually triggers altar generation for a world
     */
    public void forceGenerateAltars(World world) {
        generatedAltars.clear();
        generateAltars(world);
    }
    
    /**
     * Triggers enhanced altar generation for new worlds
     */
    public void generateAltarsForNewWorld(World world, boolean announce) {
        if (announce) {
            generateAltarsForNewWorld(world);
        } else {
            generateAltars(world);
        }
    }
    
    private String formatLocation(Location loc) {
        return loc.getWorld().getName() + " (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}