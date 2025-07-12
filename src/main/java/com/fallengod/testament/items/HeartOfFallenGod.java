package com.fallengod.testament.items;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.fallengod.testament.FallenGodPlugin;

/**
 * Manages the Heart of Fallen God and Veil of Nullification items
 */
public class HeartOfFallenGod {
    
    private final FallenGodPlugin plugin;
    
    public HeartOfFallenGod(FallenGodPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Creates the Heart of the Fallen God item
     */
    public ItemStack createHeartOfFallenGod() {
        ItemStack heart = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = heart.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§4§l❤ Heart of the Fallen God ❤");
            meta.setLore(Arrays.asList(
                "§7The ultimate source of divine power",
                "§7Grants the wielder immense strength",
                "",
                "§c§lEffects:",
                "§c+15 Hearts (25 total health)",
                "§cStrength I (permanent)",
                "§cRegeneration II (permanent)",
                "",
                "§8§lWarning: Can be nullified by the Veil",
                "§8Testament of the Fallen God"
            ));
            
            meta.addEnchant(Enchantment.UNBREAKING, 10, true);
            meta.setCustomModelData(9999);
            
            heart.setItemMeta(meta);
        }
        
        return heart;
    }
    
    /**
     * Creates the Veil of Nullification item
     */
    public ItemStack createVeilOfNullification() {
        ItemStack veil = new ItemStack(Material.PHANTOM_MEMBRANE);
        ItemMeta meta = veil.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§5§l◊ Veil of Nullification ◊");
            meta.setLore(Arrays.asList(
                "§7A mystical veil that nullifies divine power",
                "§7The perfect counter to ultimate strength",
                "",
                "§5§lEffects:",
                "§5Nullifies Heart of Fallen God within 16 blocks",
                "§5Slow Falling (permanent)",
                "§5Night Vision (permanent)",
                "",
                "§8§lStrategic: Creates tactical positioning",
                "§8Testament of the Veil God"
            ));
            
            meta.addEnchant(Enchantment.UNBREAKING, 10, true);
            meta.setCustomModelData(9998);
            
            veil.setItemMeta(meta);
        }
        
        return veil;
    }
    
    /**
     * Checks if an item is the Heart of Fallen God
     */
    public boolean isHeartOfFallenGod(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && 
               meta.getDisplayName().contains("Heart of the Fallen God");
    }
    
    /**
     * Checks if an item is the Veil of Nullification
     */
    public boolean isVeilOfNullification(ItemStack item) {
        if (item == null || item.getType() != Material.PHANTOM_MEMBRANE) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && 
               meta.getDisplayName().contains("Veil of Nullification");
    }
}