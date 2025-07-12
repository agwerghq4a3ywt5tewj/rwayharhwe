package com.fallengod.testament.items;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.fallengod.testament.FallenGodPlugin;

/**
 * Manages the creation and validation of testament fragments
 */
public class FragmentManager {
    
    private final Map<String, String> godColors;
    private final Map<String, String> godThemes;
    
    public FragmentManager(FallenGodPlugin plugin) {
        this.godColors = new HashMap<>();
        this.godThemes = new HashMap<>();
        
        initializeGodData();
    }
    
    private void initializeGodData() {
        // God color codes for fragments
        godColors.put("fallen", "§4");      // Dark Red
        godColors.put("banishment", "§c");  // Red
        godColors.put("abyssal", "§1");     // Dark Blue
        godColors.put("sylvan", "§2");      // Dark Green
        godColors.put("tempest", "§e");     // Yellow
        godColors.put("veil", "§5");        // Dark Purple
        
        // God themes for lore
        godThemes.put("fallen", "Death and Undeath");
        godThemes.put("banishment", "Fire and Exile");
        godThemes.put("abyssal", "Ocean Depths");
        godThemes.put("sylvan", "Nature and Growth");
        godThemes.put("tempest", "Storm and Sky");
        godThemes.put("veil", "Reality and Void");
    }
    
    /**
     * Creates a testament fragment for a specific god and fragment number
     */
    public ItemStack createFragment(String godType, int fragmentNumber) {
        if (fragmentNumber < 1 || fragmentNumber > 7) {
            return null;
        }
        
        if (!godColors.containsKey(godType.toLowerCase())) {
            return null;
        }
        
        String normalizedGodType = godType.toLowerCase();
        String color = godColors.get(normalizedGodType);
        String theme = godThemes.get(normalizedGodType);
        
        // Create the fragment item
        ItemStack fragment = new ItemStack(Material.ECHO_SHARD);
        ItemMeta meta = fragment.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(color + "§lFragment of " + capitalizeFirst(normalizedGodType) + " Testament");
            meta.setLore(Arrays.asList(
                "§7A shard of divine power from the",
                "§7" + capitalizeFirst(normalizedGodType) + " God's testament",
                "",
                "§8Theme: " + theme,
                "§8Fragment " + fragmentNumber + " of 7",
                "",
                color + "§lCollect all 7 fragments to unlock",
                color + "§lepic rewards and divine power!"
            ));
            
            // Add enchantment glint for rarity
            meta.addEnchant(Enchantment.UNBREAKING, fragmentNumber, true);
            
            // Set custom model data for resource packs
            meta.setCustomModelData(1000 + (getGodIndex(normalizedGodType) * 10) + fragmentNumber);
            
            fragment.setItemMeta(meta);
        }
        
        return fragment;
    }
    
    /**
     * Validates if an item is a testament fragment
     */
    public boolean isTestamentFragment(ItemStack item) {
        if (item == null || item.getType() != Material.ECHO_SHARD) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        
        String displayName = meta.getDisplayName();
        return displayName.contains("Fragment of") && displayName.contains("Testament");
    }
    
    /**
     * Gets the god type from a fragment item
     */
    public String getGodTypeFromFragment(ItemStack fragment) {
        if (!isTestamentFragment(fragment)) {
            return null;
        }
        
        ItemMeta meta = fragment.getItemMeta();
        String displayName = meta.getDisplayName();
        
        for (String godType : godColors.keySet()) {
            if (displayName.toLowerCase().contains(godType)) {
                return godType;
            }
        }
        
        return null;
    }
    
    /**
     * Gets the fragment number from a fragment item
     */
    public int getFragmentNumber(ItemStack fragment) {
        if (!isTestamentFragment(fragment)) {
            return -1;
        }
        
        ItemMeta meta = fragment.getItemMeta();
        if (meta == null || !meta.hasEnchants()) {
            return -1;
        }
        
        return meta.getEnchantLevel(Enchantment.UNBREAKING);
    }
    
    /**
     * Gets the god index for custom model data calculation
     */
    private int getGodIndex(String godType) {
        switch (godType) {
            case "fallen": return 1;
            case "banishment": return 2;
            case "abyssal": return 3;
            case "sylvan": return 4;
            case "tempest": return 5;
            case "veil": return 6;
            default: return 0;
        }
    }
    
    /**
     * Capitalizes the first letter of a string
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * Gets all valid god types
     */
    public List<String> getValidGodTypes() {
        return Arrays.asList("fallen", "banishment", "abyssal", "sylvan", "tempest", "veil");
    }
}