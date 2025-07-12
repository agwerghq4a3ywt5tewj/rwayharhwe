package com.fallengod.testament.commands;

import com.fallengod.testament.FallenGodPlugin;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Marker;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Commands for managing datapack-generated altars
 */
public class DatapackCommand implements CommandExecutor {
    
    private final FallenGodPlugin plugin;
    
    public DatapackCommand(FallenGodPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "scan":
                return handleScanCommand(sender, args);
            case "locate":
                return handleLocateCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender);
            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleScanCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fallengod.admin.scan")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        World world = player.getWorld();
        int radius = 10; // chunks
        
        if (args.length > 1) {
            try {
                radius = Integer.parseInt(args[1]);
                radius = Math.min(radius, 50); // Max 50 chunks
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid radius. Using default of 10 chunks.");
            }
        }
        
        sender.sendMessage("§eScanning " + radius + " chunks around you for datapack altars...");
        
        int found = 0;
        Chunk playerChunk = player.getLocation().getChunk();
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Chunk chunk = world.getChunkAt(playerChunk.getX() + x, playerChunk.getZ() + z);
                
                for (Entity entity : chunk.getEntities()) {
                    if (entity.getType() == EntityType.MARKER) {
                        Marker marker = (Marker) entity;
                        Set<String> tags = marker.getScoreboardTags();
                        
                        for (String tag : tags) {
                            if (tag.endsWith("_altar")) {
                                String godType = tag.replace("_altar", "");
                                Location loc = marker.getLocation();
                                
                                sender.sendMessage(String.format("§a%s altar found at %d, %d, %d", 
                                    capitalizeFirst(godType), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                                found++;
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        sender.sendMessage("§eFound " + found + " datapack altars in the scanned area.");
        return true;
    }
    
    private boolean handleLocateCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fallengod.admin.locate")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /datapack locate <god_type>");
            return true;
        }
        
        Player player = (Player) sender;
        String godType = args[1].toLowerCase();
        
        if (!plugin.getFragmentManager().getValidGodTypes().contains(godType)) {
            sender.sendMessage("§cInvalid god type. Valid types: " + String.join(", ", plugin.getFragmentManager().getValidGodTypes()));
            return true;
        }
        
        Location altarLocation = plugin.getAltarService().getAltarLocation(godType);
        if (altarLocation != null) {
            sender.sendMessage(String.format("§a%s altar is at %d, %d, %d", 
                capitalizeFirst(godType), altarLocation.getBlockX(), altarLocation.getBlockY(), altarLocation.getBlockZ()));
        } else {
            sender.sendMessage("§c" + capitalizeFirst(godType) + " altar not found or not yet discovered.");
        }
        
        return true;
    }
    
    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("fallengod.admin.reload")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        
        sender.sendMessage("§eReloading datapack altar registrations...");
        
        // Force re-scan of all loaded chunks
        for (World world : plugin.getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity entity : chunk.getEntities()) {
                    if (entity.getType() == EntityType.MARKER) {
                        Marker marker = (Marker) entity;
                        Set<String> tags = marker.getScoreboardTags();
                        
                        // Remove registered tag to force re-registration
                        if (tags.contains("registered")) {
                            marker.removeScoreboardTag("registered");
                        }
                    }
                }
            }
        }
        
        sender.sendMessage("§aDatapack altar reload complete!");
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== Datapack Commands ===");
        sender.sendMessage("§e/datapack scan [radius] §7- Scan for datapack altars");
        sender.sendMessage("§e/datapack locate <god> §7- Locate a specific altar");
        sender.sendMessage("§e/datapack reload §7- Reload altar registrations");
        sender.sendMessage("§e/datapack help §7- Show this help");
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}