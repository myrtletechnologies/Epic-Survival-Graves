package org.avarion.graves;

import org.avarion.graves.command.GravesCommand;
import org.avarion.graves.compatibility.Compatibility;
import org.avarion.graves.compatibility.CompatibilityBlockData;
import org.avarion.graves.listener.*;
import org.avarion.graves.manager.*;
import org.avarion.graves.util.ResourceUtil;
import org.avarion.graves.util.Version;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Graves extends JavaPlugin {
    private static final int DEBUG_LEVEL = 0;

    private VersionManager versionManager;
    private IntegrationManager integrationManager;
    private DataManager dataManager;
    private BlockManager blockManager;
    private ItemStackManager itemStackManager;
    private EntityDataManager entityDataManager;
    private EntityManager entityManager;
    private LocationManager locationManager;
    private GraveManager graveManager;
    private Compatibility compatibility;

    private Version myVersion = null;

    @Override
    public void onLoad() {
        myVersion = new Version(getDescription().getVersion());
        integrationManager = new IntegrationManager(this);
    }

    @Override
    public void onEnable() {
        integrationManager.load();

        versionManager = new VersionManager();
        dataManager = new DataManager(this);
        blockManager = new BlockManager(this);
        itemStackManager = new ItemStackManager(this);
        entityDataManager = new EntityDataManager(this);
        entityManager = new EntityManager(this);
        locationManager = new LocationManager(this);
        graveManager = new GraveManager(this);
        // Graveyard functionality removed in build-3

        registerCommands();
        registerListeners();
        saveTextFiles();
        getServer().getScheduler().runTask(this, this::compatibilityChecker);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.closeConnection();
            dataManager = null;
        }

        if (graveManager != null) {
            graveManager.unload();
            graveManager = null;
        }

        // Graveyard functionality removed in build-3

        if (integrationManager != null) {
            integrationManager.unload();
            integrationManager = null;
        }
    }

    public void saveTextFiles() {
        if (integrationManager != null && integrationManager.hasPlaceholderAPI()) {
            ResourceUtil.copyResources("data/text/placeholderapi.txt", getDataFolder().getPath()
                                                                       + "/placeholderapi.txt", this);
        }
    }


    public void registerListeners() {
        getServer().getPluginManager().registerEvent(EntityDeathEvent.class, new Listener() {
        }, EventPriority.MONITOR, new EntityDeathListener(this), this, true);
        getServer().getPluginManager().registerEvents(new PlayerBucketEmptyListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDropItemListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockFromToListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockPistonExtendListener(this), this);
        getServer().getPluginManager().registerEvents(new HangingBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new CreatureSpawnListener(this), this);
    }

    public void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }


    private void registerCommands() {
        PluginCommand gravesPluginCommand = getCommand("graves");

        if (gravesPluginCommand != null) {
            GravesCommand gravesCommand = new GravesCommand(this);

            gravesPluginCommand.setExecutor(gravesCommand);
            gravesPluginCommand.setTabCompleter(gravesCommand);
        }

        // Graveyard command removed in build-3
    }

    public void debugMessage(String string, int level) {
        if (level > DEBUG_LEVEL) {
            return;
        }

        getLogger().info("Debug: " + string);
    }

    public void warningMessage(String string) {
        getLogger().info("Warning: " + string);
    }

    public void compatibilityMessage(String string) {
        getLogger().info("Compatibility: " + string);
    }

    public void infoMessage(String string) {
        getLogger().info("Information: " + string);
    }


    public void integrationMessage(String string) {
        getLogger().info("Integration: " + string);
    }


    private void compatibilityChecker() {
        compatibility = new CompatibilityBlockData();

        if (versionManager.isBukkit) {
            infoMessage("Bukkit detected, some functions won't work on Bukkit, like hex codes.");
        }

        if (versionManager.isMohist) {
            infoMessage("Mohist detected, not injecting custom recipes.");
        }
    }


    public VersionManager getVersionManager() {
        return versionManager;
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public GraveManager getGraveManager() {
        return graveManager;
    }

    // Graveyard functionality removed in build-3

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public ItemStackManager getItemStackManager() {
        return itemStackManager;
    }

    public EntityDataManager getEntityDataManager() {
        return entityDataManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public Compatibility getCompatibility() {
        return compatibility;
    }


    public final File getPluginsFolder() {
        return getDataFolder().getParentFile();
    }

    public @NotNull Version getVersion() {
        return myVersion;
    }

}
