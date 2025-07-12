@@ .. @@
 package com.fallengod.testament.commands;

 import com.fallengod.testament.FallenGodPlugin;
 import com.fallengod.testament.items.FragmentManager;
 import com.fallengod.testament.services.AltarService;
+import com.fallengod.testament.world.AltarPlacementManager;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
@@ .. @@
 public class TestamentCommand implements CommandExecutor {
     
     private final FallenGodPlugin plugin;
     private final FragmentManager fragmentManager;
     private final AltarService altarService;
+    private final AltarPlacementManager altarPlacementManager;
     
     public TestamentCommand(FallenGodPlugin plugin, FragmentManager fragmentManager, 
-                           AltarService altarService) {
+                           AltarService altarService, AltarPlacementManager altarPlacementManager) {
         this.plugin = plugin;
         this.fragmentManager = fragmentManager;
         this.altarService = altarService;
+        this.altarPlacementManager = altarPlacementManager;
     }