package com.fallengod.testament;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.fallengod.testament.commands.FragmentCommand;
import com.fallengod.testament.commands.TestamentCommand;
import com.fallengod.testament.data.PlayerTestamentDataStore;
import com.fallengod.testament.items.FragmentManager;
import com.fallengod.testament.items.HeartOfFallenGod;
import com.fallengod.testament.listeners.AltarInteractionListener;
import com.fallengod.testament.listeners.ChestInteractionListener;
import com.fallengod.testament.listeners.HeartEffectListener;
import com.fallengod.testament.listeners.MobDeathListener;
import com.fallengod.testament.listeners.PlayerDataSaveListener;
import com.fallengod.testament.listeners.PlayerEventListener;
import com.fallengod.testament.listeners.WorldGenerationListener;
import com.fallengod.testament.services.AltarDetectionService;
import com.fallengod.testament.services.AltarService;
import com.fallengod.testament.services.FragmentSpawningService;
import com.fallengod.testament.services.RewardService;
import com.fallengod.testament.services.TestamentService;
import com.fallengod.testament.world.AltarPlacementManager;

/**
 * Main plugin class for the Fallen God Testament system
 */
public class FallenGodPlugin extends JavaPlugin {
    
    // Core services
    private PlayerTestamentDataStore playerDataStore;
    private FragmentManager fragmentManager;
    private TestamentService testamentService;
    private AltarService altarService;
    private AltarPlacementManager altarPlacementManager;
    private FragmentSpawningService fragmentSpawningService;
    private RewardService rewardService;
    private HeartOfFallenGod heartOfFallenGod;
    private AltarDetectionService altarDetectionService;
    
    // Scheduled tasks
    private BukkitTask autoSaveTask;
    
    @Override
    public void onEnable() {
        try {
            // Save default configuration
            saveDefaultConfig();
            
            // Initialize core services
            initializeServices();
            
            // Register event listeners
            registerEventListeners();
            
            // Register commands
            registerCommands();
            
            // Start scheduled tasks
            startScheduledTasks();
            
            getLogger().info(String.format("Fallen God Testament Plugin v%s has been enabled!", 
                getDescription().getVersion()));
            getLogger().info("Fragment spawning system initialized with chest and mob drop support.");
            getLogger().info("Heart of the Fallen God system activated - Ultimate power awaits!");
            getLogger().info("Altar interaction system fully implemented with block detection!");
            getLogger().info("Automatic altar generation enabled for new worlds!");
            
        } catch (Exception e) {
            getLogger().severe("Failed to enable plugin: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        try {
            // Cancel scheduled tasks
            if (autoSaveTask != null && !autoSaveTask.isCancelled()) {
                autoSaveTask.cancel();
            }
            
            // Save player data
            if (playerDataStore != null) {
                playerDataStore.save();
                getLogger().info("Saved player testament progress.");
            }
            
            // Cleanup services
            if (fragmentSpawningService != null) {
                fragmentSpawningService.shutdown();
            }
            
            getLogger().info("Fallen God Testament Plugin has been disabled!");
            
        } catch (Exception e) {
            getLogger().warning("Error during plugin disable: " + e.getMessage());
        }
    }
    
    private void initializeServices() {
        getLogger().info("Initializing services...");
        
        // Initialize data store first
        playerDataStore = new PlayerTestamentDataStore(this);
        try {
            playerDataStore.load();
            getLogger().info("Loaded player testament progress.");
        } catch (Exception e) {
            getLogger().warning("Failed to load player testament progress: " + e.getMessage());
        }
        
        // Core managers
        fragmentManager = new FragmentManager(this);
        heartOfFallenGod = new HeartOfFallenGod(this);
        altarDetectionService = new AltarDetectionService(this);
        
        // Services
        testamentService = new TestamentService(this, playerDataStore, fragmentManager);
        altarService = new AltarService(this, testamentService);
        altarPlacementManager = new AltarPlacementManager(this);
        fragmentSpawningService = new FragmentSpawningService(this, fragmentManager, testamentService);
        rewardService = new RewardService(this, heartOfFallenGod);
        
        getLogger().info("All services initialized successfully.");
    }
    
    private void registerEventListeners() {
        getServer().getPluginManager().registerEvents(
            new PlayerEventListener(this, testamentService), this);
        getServer().getPluginManager().registerEvents(
            new AltarInteractionListener(this, altarService), this);
        getServer().getPluginManager().registerEvents(
            new ChestInteractionListener(this, fragmentSpawningService), this);
        getServer().getPluginManager().registerEvents(
            new MobDeathListener(this, fragmentSpawningService), this);
        getServer().getPluginManager().registerEvents(
            new HeartEffectListener(this, heartOfFallenGod), this);
        getServer().getPluginManager().registerEvents(
            new PlayerDataSaveListener(playerDataStore, this), this);
        getServer().getPluginManager().registerEvents(
            new WorldGenerationListener(this), this);
        
        getLogger().info("Event listeners registered successfully.");
    }
    
    private void registerCommands() {
        getCommand("testament").setExecutor(
            new TestamentCommand(this, fragmentManager, altarService, altarPlacementManager));
        
        FragmentCommand fragmentCommand = new FragmentCommand(this, fragmentSpawningService);
        getCommand("fragment").setExecutor(fragmentCommand);
        getCommand("fragment").setTabCompleter(fragmentCommand);
        
        getLogger().info("Commands registered successfully.");
    }
    
    private void startScheduledTasks() {
        // Auto-save every 2 minutes
        autoSaveTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                if (playerDataStore != null) {
                    playerDataStore.save();
                    getLogger().fine("[AutoSave] Player testament progress saved.");
                }
            } catch (Exception e) {
                getLogger().warning("Failed to autosave player testament progress: " + e.getMessage());
            }
        }, 2400L, 2400L); // 2 minutes in ticks
        
        getLogger().info("Scheduled tasks started successfully.");
    }
    
    // Getters for services
    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }
    
    public TestamentService getTestamentService() {
        return testamentService;
    }
    
    public AltarService getAltarService() {
        return altarService;
    }
    
    public AltarPlacementManager getAltarPlacementManager() {
        return altarPlacementManager;
    }
    
    public FragmentSpawningService getFragmentSpawningService() {
        return fragmentSpawningService;
    }
    
    public RewardService getRewardService() {
        return rewardService;
    }
    
    public HeartOfFallenGod getHeartOfFallenGod() {
        return heartOfFallenGod;
    }
    
    public PlayerTestamentDataStore getPlayerDataStore() {
        return playerDataStore;
    }
    
    public AltarDetectionService getAltarDetectionService() {
        return altarDetectionService;
    }
}