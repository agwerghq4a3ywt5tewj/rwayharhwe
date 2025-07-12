package com.fallengod.testament.services;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.fallengod.testament.FallenGodPlugin;
import com.fallengod.testament.items.HeartOfFallenGod;

/**
 * Manages epic rewards for completing god testaments
 * Centralized reward system with proper validation and error handling
 */
public class RewardService {
    
    private final FallenGodPlugin plugin;
    private final HeartOfFallenGod heartOfFallenGod;
    private final Logger logger;
    
    public RewardService(FallenGodPlugin plugin, HeartOfFallenGod heartOfFallenGod) {
        this.plugin = plugin;
        this.heartOfFallenGod = heartOfFallenGod;
        this.logger = plugin.getLogger();
    }
    
    /**
     * Grants the epic reward for completing a god's testament
     */
    public void grantGodReward(Player player, String godType) {
        if (player == null || godType == null) {
            logger.warning("Invalid parameters for reward granting");
            return;
        }
        
        try {
            switch (godType.toLowerCase()) {
                case "fallen":
                    grantFallenGodReward(player);
                    break;
                case "banishment":
                    grantBanishmentGodReward(player);
                    break;
                case "abyssal":
                    grantAbyssalGodReward(player);
                    break;
                case "sylvan":
                    grantSylvanGodReward(player);
                    break;
                case "tempest":
                    grantTempestGodReward(player);
                    break;
                case "veil":
                    grantVeilGodReward(player);
                    break;
                default:
                    logger.warning("Unknown god type for reward: " + godType);
                    player.sendMessage("§cError: Unknown god type. Please contact an administrator.");
            }
        } catch (Exception e) {
            logger.severe("Error granting reward for " + godType + " to " + player.getName() + ": " + e.getMessage());
            player.sendMessage("§cAn error occurred while granting your reward. Please contact an administrator.");
        }
    }
    
    /**
     * FALLEN GOD REWARD: Ultimate Protection Set + Heart of the Fallen God
     */
    private void grantFallenGodReward(Player player) {
        // Create armor set
        ItemStack helmet = createArmorPiece(Material.NETHERITE_HELMET, 
            "§4§lCrown of the Fallen God", Arrays.asList(
                "§7Forged from the essence of death itself",
                "§7Grants protection against all forms of harm",
                "§8Testament of the Fallen God"
            ));
        addArmorEnchantments(helmet, true);
        
        ItemStack chestplate = createArmorPiece(Material.NETHERITE_CHESTPLATE,
            "§4§lArmor of Eternal Defiance", Arrays.asList(
                "§7Wrought from the will of the fallen",
                "§7No force can pierce this divine protection",
                "§8Testament of the Fallen God"
            ));
        addArmorEnchantments(chestplate, false);
        chestplate.addUnsafeEnchantment(Enchantment.THORNS, 3);
        
        ItemStack leggings = createArmorPiece(Material.NETHERITE_LEGGINGS,
            "§4§lGreaves of Undying Resolve", Arrays.asList(
                "§7Legs that have walked through death",
                "§7And emerged stronger than before",
                "§8Testament of the Fallen God"
            ));
        addArmorEnchantments(leggings, false);
        leggings.addUnsafeEnchantment(Enchantment.SWIFT_SNEAK, 3);
        
        ItemStack boots = createArmorPiece(Material.NETHERITE_BOOTS,
            "§4§lBoots of the Deathwalker", Arrays.asList(
                "§7Steps that echo through eternity",
                "§7Carrying the power of the fallen",
                "§8Testament of the Fallen God"
            ));
        addArmorEnchantments(boots, false);
        boots.addUnsafeEnchantment(Enchantment.FEATHER_FALLING, 4);
        boots.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 3);
        boots.addUnsafeEnchantment(Enchantment.SOUL_SPEED, 3);
        
        // The legendary Heart of the Fallen God
        ItemStack heartOfFallenGod = this.heartOfFallenGod.createHeartOfFallenGod();
        
        // Give items
        giveItems(player, Arrays.asList(helmet, chestplate, leggings, boots, heartOfFallenGod));
        
        // Permanent effect
        addPermanentEffect(player, PotionEffectType.RESISTANCE, 0);
        
        // Announcements
        announceReward(player, "FALLEN GOD", "Ultimate Protection Armor Set + Heart of the Fallen God");
        
        org.bukkit.Bukkit.broadcastMessage("§4§l❤ " + player.getName() + " now possesses the HEART OF THE FALLEN GOD! ❤");
        org.bukkit.Bukkit.broadcastMessage("§c§lThey wield the power of 25 hearts and divine strength!");
    }
    
    /**
     * BANISHMENT GOD REWARD: Ultimate Weapon & Tool Set
     */
    private void grantBanishmentGodReward(Player player) {
        ItemStack sword = createWeapon(Material.NETHERITE_SWORD,
            "§c§lBlade of Eternal Banishment", Arrays.asList(
                "§7Forged in the fires of exile",
                "§7Cuts through reality itself",
                "§8Testament of the Banishment God"
            ));
        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 10);
        sword.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
        sword.addUnsafeEnchantment(Enchantment.LOOTING, 5);
        sword.addUnsafeEnchantment(Enchantment.SWEEPING_EDGE, 3);
        sword.addUnsafeEnchantment(Enchantment.UNBREAKING, 6);
        sword.addUnsafeEnchantment(Enchantment.MENDING, 1);
        sword.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
        
        giveItems(player, Arrays.asList(sword));
        addPermanentEffect(player, PotionEffectType.STRENGTH, 0);
        announceReward(player, "BANISHMENT GOD", "Ultimate Weapon & Tool Set");
    }
    
    /**
     * ABYSSAL GOD REWARD: Master of the Depths
     */
    private void grantAbyssalGodReward(Player player) {
        ItemStack trident = createWeapon(Material.TRIDENT,
            "§1§lTrident of the Endless Deep", Arrays.asList(
                "§7Commands the power of all oceans",
                "§7Strikes with the fury of the abyss",
                "§8Testament of the Abyssal God"
            ));
        trident.addUnsafeEnchantment(Enchantment.LOYALTY, 3);
        trident.addUnsafeEnchantment(Enchantment.CHANNELING, 1);
        trident.addUnsafeEnchantment(Enchantment.RIPTIDE, 3);
        trident.addUnsafeEnchantment(Enchantment.IMPALING, 5);
        trident.addUnsafeEnchantment(Enchantment.UNBREAKING, 6);
        trident.addUnsafeEnchantment(Enchantment.MENDING, 1);
        
        giveItems(player, Arrays.asList(trident));
        addPermanentEffect(player, PotionEffectType.WATER_BREATHING, 0);
        addPermanentEffect(player, PotionEffectType.DOLPHINS_GRACE, 0);
        announceReward(player, "ABYSSAL GOD", "Master of the Depths Equipment");
    }
    
    /**
     * SYLVAN GOD REWARD: Nature's Guardian Set
     */
    private void grantSylvanGodReward(Player player) {
        ItemStack bow = createWeapon(Material.BOW,
            "§2§lBow of the Ancient Forest", Arrays.asList(
                "§7Carved from the World Tree itself",
                "§7Never misses its intended target",
                "§8Testament of the Sylvan God"
            ));
        bow.addUnsafeEnchantment(Enchantment.POWER, 5);
        bow.addUnsafeEnchantment(Enchantment.PUNCH, 2);
        bow.addUnsafeEnchantment(Enchantment.FLAME, 1);
        bow.addUnsafeEnchantment(Enchantment.INFINITY, 1);
        bow.addUnsafeEnchantment(Enchantment.UNBREAKING, 6);
        bow.addUnsafeEnchantment(Enchantment.MENDING, 1);
        
        giveItems(player, Arrays.asList(bow));
        addPermanentEffect(player, PotionEffectType.REGENERATION, 0);
        addPermanentEffect(player, PotionEffectType.SATURATION, 0);
        announceReward(player, "SYLVAN GOD", "Nature's Guardian Equipment");
    }
    
    /**
     * TEMPEST GOD REWARD: Storm Lord's Arsenal
     */
    private void grantTempestGodReward(Player player) {
        ItemStack elytra = createItem(Material.ELYTRA,
            "§e§lWings of the Storm Lord", Arrays.asList(
                "§7Soar through the heavens themselves",
                "§7Command the winds and lightning",
                "§8Testament of the Tempest God"
            ));
        elytra.addUnsafeEnchantment(Enchantment.UNBREAKING, 6);
        elytra.addUnsafeEnchantment(Enchantment.MENDING, 1);
        
        giveItems(player, Arrays.asList(elytra));
        addPermanentEffect(player, PotionEffectType.SPEED, 1);
        addPermanentEffect(player, PotionEffectType.JUMP_BOOST, 1);
        announceReward(player, "TEMPEST GOD", "Storm Lord's Arsenal");
    }
    
    /**
     * VEIL GOD REWARD: Master of Dimensions + Veil of Nullification
     */
    private void grantVeilGodReward(Player player) {
        ItemStack veilOfNullification = heartOfFallenGod.createVeilOfNullification();
        
        giveItems(player, Arrays.asList(veilOfNullification));
        addPermanentEffect(player, PotionEffectType.NIGHT_VISION, 0);
        addPermanentEffect(player, PotionEffectType.SLOW_FALLING, 0);
        announceReward(player, "VEIL GOD", "Master of Dimensions Equipment + Veil of Nullification");
        
        org.bukkit.Bukkit.broadcastMessage("§5§l◊ " + player.getName() + " now wields the VEIL OF NULLIFICATION! ◊");
        org.bukkit.Bukkit.broadcastMessage("§8§lThey can nullify the Heart of the Fallen God's power!");
    }
    
    /**
     * Creates a custom item with name and lore
     */
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Creates an armor piece with standard properties
     */
    private ItemStack createArmorPiece(Material material, String name, List<String> lore) {
        return createItem(material, name, lore);
    }
    
    /**
     * Creates a weapon with standard properties
     */
    private ItemStack createWeapon(Material material, String name, List<String> lore) {
        return createItem(material, name, lore);
    }
    
    /**
     * Adds standard armor enchantments
     */
    private void addArmorEnchantments(ItemStack armor, boolean isHelmet) {
        armor.addUnsafeEnchantment(Enchantment.PROTECTION, 4);
        armor.addUnsafeEnchantment(Enchantment.BLAST_PROTECTION, 4);
        armor.addUnsafeEnchantment(Enchantment.PROJECTILE_PROTECTION, 4);
        armor.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        armor.addUnsafeEnchantment(Enchantment.MENDING, 1);
        
        if (isHelmet) {
            armor.addUnsafeEnchantment(Enchantment.RESPIRATION, 3);
            armor.addUnsafeEnchantment(Enchantment.AQUA_AFFINITY, 1);
        }
    }
    
    /**
     * Safely adds a permanent potion effect to a player
     */
    private void addPermanentEffect(Player player, PotionEffectType effectType, int amplifier) {
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
     * Gives items to player, dropping excess if inventory is full
     */
    private void giveItems(Player player, List<ItemStack> items) {
        for (ItemStack item : items) {
            try {
                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(item);
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                    player.sendMessage("§7Some items were dropped at your feet due to full inventory!");
                }
            } catch (Exception e) {
                logger.warning("Failed to give item to " + player.getName() + ": " + e.getMessage());
                // Try to drop the item as a last resort
                try {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                } catch (Exception dropError) {
                    logger.severe("Failed to drop item for " + player.getName() + ": " + dropError.getMessage());
                }
            }
        }
    }
    
    /**
     * Announces the epic reward to the server
     */
    private void announceReward(Player player, String godName, String rewardDescription) {
        try {
            // Server-wide announcement
            org.bukkit.Bukkit.broadcastMessage("§6§l⚡ DIVINE TESTAMENT COMPLETED ⚡");
            org.bukkit.Bukkit.broadcastMessage("§e§l" + player.getName() + " has completed the Testament of the " + godName + "!");
            org.bukkit.Bukkit.broadcastMessage("§7Reward: " + rewardDescription);
            
            // Player notification
            player.sendMessage("§6§l✦ TESTAMENT COMPLETED ✦");
            player.sendMessage("§e§lYou have been blessed by the " + godName + "!");
            player.sendMessage("§7Your dedication has been rewarded with divine power.");
            
            logger.info("EPIC REWARD GRANTED: " + player.getName() + " completed " + godName + " testament");
            
        } catch (Exception e) {
            logger.warning("Failed to announce reward for " + player.getName() + ": " + e.getMessage());
        }
    }
}