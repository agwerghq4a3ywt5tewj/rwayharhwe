package com.fallengod.testament.commands;

import com.fallengod.testament.FallenGodPlugin;
import com.fallengod.testament.items.FragmentManager;
import com.fallengod.testament.services.AltarService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Main command handler for the testament system
 * Updated for datapack integration
 */
public class TestamentCommand implements CommandExecutor {
    
    private final FallenGodPlugin plugin;
    private final FragmentManager fragmentManager;
    private final AltarService altarService;
    
    public TestamentCommand(FallenGodPlugin plugin, FragmentManager fragmentManager, 
                           AltarService altarService) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        this.altarService = altarService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "status":
                return handleStatusCommand(sender);
            case "reunite":
                return handleReuniteCommand(sender, args);
            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleStatusCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        try {
            Map<String, Integer> fragmentCounts = plugin.getTestamentService().getAllFragmentCounts(player);
            
            sender.sendMessage("§6=== Testament Progress ===");
            
            for (String godType : fragmentManager.getValidGodTypes()) {
                var data = plugin.getPlayerDataStore().get(player.getUniqueId());
                String progress = data.getProgressSummary(godType);
                sender.sendMessage("§e" + capitalizeFirst(godType) + " God: " + progress);
            }
            
            sender.sendMessage("§7");
            sender.sendMessage("§7Tip: Use /datapack locate <god> to find altar locations!");
            
        } catch (Exception e) {
            sender.sendMessage("§cError retrieving progress: " + e.getMessage());
            plugin.getLogger().warning("Error in status command: " + e.getMessage());
        }
        
        return true;
    }
    
    private boolean handleReuniteCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /testament reunite <god_type>");
            return true;
        }
        
        Player player = (Player) sender;
        String godType = args[1].toLowerCase();
        
        if (!fragmentManager.getValidGodTypes().contains(godType)) {
            sender.sendMessage("§cInvalid god type. Valid types: " + String.join(", ", fragmentManager.getValidGodTypes()));
            return true;
        }
        
        try {
            return altarService.handleAltarInteraction(player, godType, player.getLocation());
            
        } catch (Exception e) {
            sender.sendMessage("§cError during reunification: " + e.getMessage());
            plugin.getLogger().warning("Error in reunite command: " + e.getMessage());
            return false;
        }
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== Testament Commands ===");
        sender.sendMessage("§e/testament status §7- View your fragment progress");
        sender.sendMessage("§e/testament reunite <god> §7- Reunite fragments at an altar");
        sender.sendMessage("§e/testament help §7- Show this help");
        sender.sendMessage("§7");
        sender.sendMessage("§7Available gods: " + String.join(", ", fragmentManager.getValidGodTypes()));
        sender.sendMessage("§7Use /datapack commands to manage altars!");
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}