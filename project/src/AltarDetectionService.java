package com.fallengod.testament.services;

import com.fallengod.testament.FallenGodPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Service for detecting and validating altar structures
 * Provides advanced altar detection beyond just center blocks
 */
public class AltarDetectionService {
    
    private final FallenGodPlugin plugin;
    private final Logger logger;
    
    // Altar structure patterns for validation
    private final Map<String, AltarPattern> altarPatterns;
    
    public AltarDetectionService(FallenGodPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.altarPatterns = initializeAltarPatterns();
    }
    
    /**
     * Detects if a location contains a valid altar structure
     */
    public String detectAltarAtLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        
        // Check each altar pattern
        for (Map.Entry<String, AltarPattern> entry : altarPatterns.entrySet()) {
            String godType = entry.getKey();
            AltarPattern pattern = entry.getValue();
            
            if (matchesAltarPattern(location, pattern)) {
                return godType;
            }
        }
        
        return null;
    }
    
    /**
     * Validates that an altar structure is complete and correct
     */
    public boolean validateAltarStructure(Location center, String godType) {
        AltarPattern pattern = altarPatterns.get(godType);
        if (pattern == null) {
            return false;
        }
        
        return matchesAltarPattern(center, pattern);
    }
    
    /**
     * Gets the center location of an altar if the clicked block is part of one
     */
    public Location findAltarCenter(Location clickedLocation, String godType) {
        AltarPattern pattern = altarPatterns.get(godType);
        if (pattern == null) {
            return null;
        }
        
        // Search in a small area around the clicked location for the center
        World world = clickedLocation.getWorld();
        int clickedX = clickedLocation.getBlockX();
        int clickedY = clickedLocation.getBlockY();
        int clickedZ = clickedLocation.getBlockZ();
        
        // Check a 7x7x3 area around the clicked location
        for (int x = clickedX - 3; x <= clickedX + 3; x++) {
            for (int y = clickedY - 1; y <= clickedY + 1; y++) {
                for (int z = clickedZ - 3; z <= clickedZ + 3; z++) {
                    Location testCenter = new Location(world, x, y, z);
                    if (matchesAltarPattern(testCenter, pattern)) {
                        return testCenter;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Checks if a location matches a specific altar pattern
     */
    private boolean matchesAltarPattern(Location center, AltarPattern pattern) {
        World world = center.getWorld();
        
        // Check center block
        Block centerBlock = world.getBlockAt(center);
        if (centerBlock.getType() != pattern.centerBlock) {
            return false;
        }
        
        // Check platform blocks
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                Location platformLoc = center.clone().add(x, -1, z);
                Block platformBlock = world.getBlockAt(platformLoc);
                
                if (platformBlock.getType() != pattern.platformBlock) {
                    return false;
                }
            }
        }
        
        // Check specific pattern blocks if defined
        for (Map.Entry<Location, Material> entry : pattern.specificBlocks.entrySet()) {
            Location offset = entry.getKey();
            Material expectedMaterial = entry.getValue();
            
            Location checkLoc = center.clone().add(offset);
            Block checkBlock = world.getBlockAt(checkLoc);
            
            if (checkBlock.getType() != expectedMaterial) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Initializes altar patterns for each god type
     */
    private Map<String, AltarPattern> initializeAltarPatterns() {
        Map<String, AltarPattern> patterns = new HashMap<>();
        
        // Fallen God Altar Pattern
        AltarPattern fallenPattern = new AltarPattern(Material.CRYING_OBSIDIAN, Material.BLACKSTONE);
        fallenPattern.addSpecificBlock(new Location(null, 0, 1, 0), Material.SOUL_FIRE);
        fallenPattern.addSpecificBlock(new Location(null, 3, 0, 3), Material.BLACKSTONE);
        fallenPattern.addSpecificBlock(new Location(null, -3, 0, 3), Material.BLACKSTONE);
        fallenPattern.addSpecificBlock(new Location(null, 3, 0, -3), Material.BLACKSTONE);
        fallenPattern.addSpecificBlock(new Location(null, -3, 0, -3), Material.BLACKSTONE);
        patterns.put("fallen", fallenPattern);
        
        // Banishment God Altar Pattern
        AltarPattern banishmentPattern = new AltarPattern(Material.MAGMA_BLOCK, Material.NETHER_BRICKS);
        banishmentPattern.addSpecificBlock(new Location(null, 0, 1, 0), Material.FIRE);
        banishmentPattern.addSpecificBlock(new Location(null, 2, 0, 2), Material.LAVA);
        banishmentPattern.addSpecificBlock(new Location(null, -2, 0, 2), Material.LAVA);
        banishmentPattern.addSpecificBlock(new Location(null, 2, 0, -2), Material.LAVA);
        banishmentPattern.addSpecificBlock(new Location(null, -2, 0, -2), Material.LAVA);
        patterns.put("banishment", banishmentPattern);
        
        // Abyssal God Altar Pattern
        AltarPattern abyssalPattern = new AltarPattern(Material.DARK_PRISMARINE, Material.PRISMARINE);
        abyssalPattern.addSpecificBlock(new Location(null, 0, 1, 0), Material.SEA_LANTERN);
        abyssalPattern.addSpecificBlock(new Location(null, 2, 1, 2), Material.WATER);
        abyssalPattern.addSpecificBlock(new Location(null, -2, 1, 2), Material.WATER);
        abyssalPattern.addSpecificBlock(new Location(null, 2, 1, -2), Material.WATER);
        abyssalPattern.addSpecificBlock(new Location(null, -2, 1, -2), Material.WATER);
        patterns.put("abyssal", abyssalPattern);
        
        // Sylvan God Altar Pattern
        AltarPattern sylvanPattern = new AltarPattern(Material.OAK_LOG, Material.MOSS_BLOCK);
        sylvanPattern.addSpecificBlock(new Location(null, 0, 1, 0), Material.OAK_LEAVES);
        sylvanPattern.addSpecificBlock(new Location(null, 2, 1, 2), Material.OAK_SAPLING);
        sylvanPattern.addSpecificBlock(new Location(null, -2, 1, 2), Material.BIRCH_SAPLING);
        sylvanPattern.addSpecificBlock(new Location(null, 2, 1, -2), Material.SPRUCE_SAPLING);
        sylvanPattern.addSpecificBlock(new Location(null, -2, 1, -2), Material.JUNGLE_SAPLING);
        patterns.put("sylvan", sylvanPattern);
        
        // Tempest God Altar Pattern
        AltarPattern tempestPattern = new AltarPattern(Material.LIGHTNING_ROD, Material.QUARTZ_BLOCK);
        tempestPattern.addSpecificBlock(new Location(null, 2, 1, 0), Material.WHITE_WOOL);
        tempestPattern.addSpecificBlock(new Location(null, -2, 1, 0), Material.WHITE_WOOL);
        tempestPattern.addSpecificBlock(new Location(null, 0, 1, 2), Material.WHITE_WOOL);
        tempestPattern.addSpecificBlock(new Location(null, 0, 1, -2), Material.WHITE_WOOL);
        patterns.put("tempest", tempestPattern);
        
        // Veil God Altar Pattern
        AltarPattern veilPattern = new AltarPattern(Material.END_PORTAL_FRAME, Material.END_STONE);
        veilPattern.addSpecificBlock(new Location(null, 0, 1, 0), Material.ENDER_CHEST);
        veilPattern.addSpecificBlock(new Location(null, 3, 1, 3), Material.END_ROD);
        veilPattern.addSpecificBlock(new Location(null, -3, 1, 3), Material.END_ROD);
        veilPattern.addSpecificBlock(new Location(null, 3, 1, -3), Material.END_ROD);
        veilPattern.addSpecificBlock(new Location(null, -3, 1, -3), Material.END_ROD);
        patterns.put("veil", veilPattern);
        
        return patterns;
    }
    
    /**
     * Inner class representing an altar pattern
     */
    private static class AltarPattern {
        final Material centerBlock;
        final Material platformBlock;
        final Map<Location, Material> specificBlocks;
        
        public AltarPattern(Material centerBlock, Material platformBlock) {
            this.centerBlock = centerBlock;
            this.platformBlock = platformBlock;
            this.specificBlocks = new HashMap<>();
        }
        
        public void addSpecificBlock(Location offset, Material material) {
            specificBlocks.put(offset, material);
        }
    }
}