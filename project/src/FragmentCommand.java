package com.fallengod.testament.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.fallengod.testament.FallenGodPlugin;
import com.fallengod.testament.services.FragmentSpawningService;

/**
 * Command handler for fragment management and altar generation
 */
public class FragmentCommand implements CommandExecutor, TabCompleter {
    
    private final FallenGodPlugin plugin;
    private final FragmentSpawningService fragmentSpawningService;
    
    public FragmentCommand(FallenGodPlugin plugin, FragmentSpawningService fragmentSpawningService) {
        this.plugin = plugin;
        this.fragmentSpawningService = fragmentSpawningService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "spawn":
                return handleSpawnCommand(sender, args);
            case "heart":
                return handleHeartCommand(sender);
            case "veil":
                return handleVeilCommand(sender);
            case "stats":
                return handleStatsCommand(sender);
            case "clear":
                return handleClearCommand(sender);
            case "generatealtars":
                return handleGenerateAltarsCommand(sender);
            case "forcealtar":
                return handleForceAltarCommand(sender, args);
            case "giveall":
                return handleGiveAllFragmentsCommand(sender, args);
            case "progress":
                return handleProgressCommand(sender, args);
            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }

    /**
     * Shows detailed progress for a player
     */
    private boolean handleProgressCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fallengod.admin.stats")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        
        Player target;
        if (args.length > 1) {
            target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[1]);
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage("§cYou must specify a player name when using this command from console.");
            return true;
        }
        
        sender.sendMessage("§6=== Testament Progress for " + target.getName() + " ===");
        
        for (String godType : plugin.getFragmentManager().getValidGodTypes()) {
            var data = plugin.getPlayerDataStore().get(target.getUniqueId());
            String progress = data.getProgressSummary(godType);
            sender.sendMessage("§e" + capitalizeFirst(godType) + " God: " + progress);
        }
        
        return true;
    }

    /**
     * Gives all 7 fragments for a god to the player
     */
    private boolean handleGiveAllFragmentsCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fallengod.admin.spawn")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /fragment giveall <god>");
            return true;
        }
        
        String godType = args[1].toLowerCase();
        if (!plugin.getFragmentManager().getValidGodTypes().contains(godType)) {
            sender.sendMessage("§cInvalid god type. Valid types: " + String.join(", ", plugin.getFragmentManager().getValidGodTypes()));
            return true;
        }
        
        Player player = (Player) sender;
        
        try {
            // Give all fragments
            for (int i = 1; i <= 7; i++) {
                ItemStack fragment = plugin.getFragmentManager().createFragment(godType, i);
                player.getInventory().addItem(fragment);
                
                // Update progress
                plugin.getTestamentService().onFragmentObtained(player, godType, i);
            }
            
            player.sendMessage("§aGiven all 7 fragments for " + godType + ". Progress updated.");
            
        } catch (Exception e) {
            player.sendMessage("§cError giving fragments: " + e.getMessage());
            plugin.getLogger().warning("Error in giveall command: " + e.getMessage());
        }
        
        return true;
    }

    private boolean handleForceAltarCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fallengod.admin.generate")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /fragment forcealtar <god>");
            return true;
        }
        
        String godType = args[1].toLowerCase();
        if (!plugin.getFragmentManager().getValidGodTypes().contains(godType)) {
            sender.sendMessage("§cInvalid god type. Valid types: " + String.join(", ", plugin.getFragmentManager().getValidGodTypes()));
            return true;
        }
        
        Player player = (Player) sender;
        
        try {
            org.bukkit.Location loc = player.getLocation();
            plugin.getAltarPlacementManager().buildAltar(loc, godType);
            plugin.getAltarService().registerAltar(godType, loc);
            sender.sendMessage("§aForced altar for '" + godType + "' at your location.");
            
        } catch (Exception e) {
            sender.sendMessage("§cError creating altar: " + e.getMessage());
            plugin.getLogger().warning("Error in forcealtar command: " + e.getMessage());
        }
        
        return true;
    }
    
    private boolean handleSpawnCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fallengod.admin.spawn")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 4) {
            sender.sendMessage("§cUsage: /fragment spawn <god> <number> <chest|here>");
            return true;
        }
        
        String godType = args[1].toLowerCase();
        int fragmentNumber;
        String location = args[3].toLowerCase();
        
        try {
            fragmentNumber = Integer.parseInt(args[2]);
            if (fragmentNumber < 1 || fragmentNumber > 7) {
                sender.sendMessage("§cFragment number must be between 1 and 7.");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid fragment number. Must be 1-7.");
            return true;
        }
        
        if (!plugin.getFragmentManager().getValidGodTypes().contains(godType)) {
            sender.sendMessage("§cInvalid god type. Valid types: " + String.join(", ", plugin.getFragmentManager().getValidGodTypes()));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if ("chest".equals(location)) {
            return spawnInChest(player, godType, fragmentNumber);
        } else if ("here".equals(location)) {
            return spawnInInventory(player, godType, fragmentNumber);
        } else {
            sender.sendMessage("§cInvalid location. Use 'chest' or 'here'.");
            return true;
        }
    }
    
    private boolean spawnInChest(Player player, String godType, int fragmentNumber) {
        try {
            Block targetBlock = player.getTargetBlock(null, 10);
            
            if (!(targetBlock.getState() instanceof Chest)) {
                player.sendMessage("§cYou must be looking at a chest.");
                return true;
            }
            
            Chest chest = (Chest) targetBlock.getState();
            ItemStack fragment = plugin.getFragmentManager().createFragment(godType, fragmentNumber);
            
            if (fragment != null) {
                chest.getInventory().addItem(fragment);
                chest.update();
                player.sendMessage("§aSpawned " + godType + " fragment " + fragmentNumber + " in chest.");
            } else {
                player.sendMessage("§cFailed to create fragment.");
            }
            
        } catch (Exception e) {
            player.sendMessage("§cError spawning fragment: " + e.getMessage());
            plugin.getLogger().warning("Error in spawn chest command: " + e.getMessage());
        }
        
        return true;
    }
    
    private boolean spawnInInventory(Player player, String godType, int fragmentNumber) {
        try {
            ItemStack fragment = plugin.getFragmentManager().createFragment(godType, fragmentNumber);
            
            if (fragment != null) {
                player.getInventory().addItem(fragment);
                
                // Update player progress
                plugin.getTestamentService().onFragmentObtained(player, godType, fragmentNumber);
                
                player.sendMessage("§aGiven " + godType + " fragment " + fragmentNumber + ".");
            } else {
                player.sendMessage("§cFailed to create fragment.");
            }
            
        } catch (Exception e) {
            player.sendMessage("§cError creating fragment: " + e.getMessage());
            plugin.getLogger().warning("Error in spawn inventory command: " + e.getMessage());
        }
        
        return true;
    }
    
    private boolean handleHeartCommand(CommandSender sender) {
        if (!sender.hasPermission("fallengod.admin.spawn")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        try {
            player.getInventory().addItem(plugin.getHeartOfFallenGod().createHeartOfFallenGod());
            player.sendMessage("§4§l❤ You have been given the Heart of the Fallen God! ❤");
            
        } catch (Exception e) {
            player.sendMessage("§cError giving heart: " + e.getMessage());
            plugin.getLogger().warning("Error in heart command: " + e.getMessage());
        }
        
        return true;
    }
    
    private boolean handleVeilCommand(CommandSender sender) {
        if (!sender.hasPermission("fallengod.admin.spawn")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        try {
            player.getInventory().addItem(plugin.getHeartOfFallenGod().createVeilOfNullification());
            player.sendMessage("§5§l◊ You have been given the Veil of Nullification! ◊");
            
        } catch (Exception e) {
            player.sendMessage("§cError giving veil: " + e.getMessage());
            plugin.getLogger().warning("Error in veil command: " + e.getMessage());
        }
        
        return true;
    }
    
    private boolean handleStatsCommand(CommandSender sender) {
        if (!sender.hasPermission("fallengod.admin.stats")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        
        try {
            Map<String, Object> stats = fragmentSpawningService.getSpawningStats();
            
            sender.sendMessage("§6=== Fragment Spawning Statistics ===");
            sender.sendMessage("§eGeneral Statistics:");
            sender.sendMessage("  §7Processed chests: " + stats.get("processed_chests"));
            sender.sendMessage("  §7Recent spawns: " + stats.get("recent_spawns"));
            sender.sendMessage("  §7Chest spawn chance: " + String.format("%.2f%%", (Double) stats.get("chest_spawn_chance") * 100));
            sender.sendMessage("  §7Mob drop chance: " + String.format("%.3f%%", (Double) stats.get("mob_drop_chance") * 100));
            sender.sendMessage("  §7Total player opens: " + stats.get("total_player_opens"));
            sender.sendMessage("  §7Total mob kills: " + stats.get("total_mob_kills"));
            
        } catch (Exception e) {
            sender.sendMessage("§cError retrieving stats: " + e.getMessage());
            plugin.getLogger().warning("Error in stats command: " + e.getMessage());
        }
        
        return true;
    }
    
    private boolean handleClearCommand(CommandSender sender) {
        if (!sender.hasPermission("fallengod.admin.clear")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        
        try {
            fragmentSpawningService.clearProcessedChests();
            sender.sendMessage("§aCleared processed chest cache and player statistics.");
            
        } catch (Exception e) {
            sender.sendMessage("§cError clearing cache: " + e.getMessage());
            plugin.getLogger().warning("Error in clear command: " + e.getMessage());
        }
        
        return true;
    }
    
    private boolean handleGenerateAltarsCommand(CommandSender sender) {
        if (!sender.hasPermission("fallengod.admin.generate")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        World world = player.getWorld();
        
        try {
            sender.sendMessage("§eGenerating altars in world: " + world.getName());
            plugin.getAltarPlacementManager().forceGenerateAltars(world);
            sender.sendMessage("§aAltar generation complete!");
            
        } catch (Exception e) {
            sender.sendMessage("§cError generating altars: " + e.getMessage());
            plugin.getLogger().warning("Error in generatealtars command: " + e.getMessage());
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== Fragment Commands ===");
        sender.sendMessage("§e/fragment spawn <god> <number> <chest|here> §7- Spawn fragment");
        sender.sendMessage("§e/fragment heart §7- Give Heart of Fallen God");
        sender.sendMessage("§e/fragment veil §7- Give Veil of Nullification");
        sender.sendMessage("§e/fragment stats §7- View spawning statistics");
        sender.sendMessage("§e/fragment clear §7- Clear processed chest cache");
        sender.sendMessage("§e/fragment generatealtars §7- Generate altars in current world");
        sender.sendMessage("§e/fragment forcealtar <god> §7- Force altar at your location");
        sender.sendMessage("§e/fragment giveall <god> §7- Give all 7 fragments for a god");
        sender.sendMessage("§e/fragment progress [player] §7- Show detailed progress");
        sender.sendMessage("§e/fragment help §7- Show this help");
        
        sender.sendMessage("§7Available gods: " + String.join(", ", plugin.getFragmentManager().getValidGodTypes()));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        switch (args.length) {
            case 1:
                completions.addAll(Arrays.asList("spawn", "heart", "veil", "stats", "clear", "generatealtars", "forcealtar", "giveall", "progress", "help"));
                break;
            case 2:
                if (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("forcealtar") || args[0].equalsIgnoreCase("giveall")) {
                    completions.addAll(plugin.getFragmentManager().getValidGodTypes());
                } else if (args[0].equalsIgnoreCase("progress")) {
                    // Add online player names
                    plugin.getServer().getOnlinePlayers().forEach(p -> completions.add(p.getName()));
                }
                break;
            case 3:
                if (args[0].equalsIgnoreCase("spawn")) {
                    completions.addAll(Arrays.asList("1", "2", "3", "4", "5", "6", "7"));
                }
                break;
            case 4:
                if (args[0].equalsIgnoreCase("spawn")) {
                    completions.addAll(Arrays.asList("chest", "here"));
                }
                break;
        }
        
        return completions;
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}