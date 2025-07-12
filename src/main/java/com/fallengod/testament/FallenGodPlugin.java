@@ .. @@
 import com.fallengod.testament.services.RewardService;
 import com.fallengod.testament.services.TestamentService;
+import com.fallengod.testament.world.AltarPlacementManager;

 /**
@@ .. @@
     // Core services
     private PlayerTestamentDataStore playerDataStore;
     private FragmentManager fragmentManager;
     private TestamentService testamentService;
     private AltarService altarService;
+    private AltarPlacementManager altarPlacementManager;
     private FragmentSpawningService fragmentSpawningService;
@@ .. @@
         // Services
         testamentService = new TestamentService(this, playerDataStore, fragmentManager);
         altarService = new AltarService(this, testamentService);
+        altarPlacementManager = new AltarPlacementManager(this);
         fragmentSpawningService = new FragmentSpawningService(this, fragmentManager, testamentService);
@@ .. @@
         getCommand("testament").setExecutor(
-            new TestamentCommand(this, fragmentManager, altarService));
+            new TestamentCommand(this, fragmentManager, altarService, altarPlacementManager));
         
         FragmentCommand fragmentCommand = new FragmentCommand(this, fragmentSpawningService);
@@ .. @@
     public AltarDetectionService getAltarDetectionService() {
         return altarDetectionService;
     }
+    
+    public AltarPlacementManager getAltarPlacementManager() {
+        return altarPlacementManager;
+    }
 }