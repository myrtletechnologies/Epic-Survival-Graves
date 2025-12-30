package org.avarion.graves.manager;

import org.avarion.graves.Graves;
import org.avarion.graves.integration.PlaceholderAPI;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public final class IntegrationManager {

    private static final boolean COMPAT_WARNINGS_ENABLED = true;

    private final Graves plugin;
    private PlaceholderAPI placeholderAPI;

    public IntegrationManager(Graves plugin) {
        this.plugin = plugin;
    }

    public void load() {
        loadPlaceholderAPI();

        if (COMPAT_WARNINGS_ENABLED) {
            loadCompatibilityWarnings();
        }
    }

    public void unload() {
        if (placeholderAPI != null) {
            placeholderAPI.unregister();
            placeholderAPI = null;
        }
    }

    public boolean hasPlaceholderAPI() {
        return placeholderAPI != null;
    }

    public PlaceholderAPI getPlaceholderAPI() {
        return placeholderAPI;
    }

    private void loadPlaceholderAPI() {
        if (placeholderAPI != null) {
            placeholderAPI.unregister();
            placeholderAPI = null;
        }

        Plugin placeholderAPIPlugin = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (placeholderAPIPlugin != null && placeholderAPIPlugin.isEnabled()) {
            placeholderAPI = new PlaceholderAPI(plugin);
            placeholderAPI.register();

            plugin.integrationMessage("Hooked into "
                                      + placeholderAPIPlugin.getName()
                                      + " "
                                      + placeholderAPIPlugin.getDescription().getVersion()
                                      + ".");
        }
    }

    private void loadCompatibilityWarnings() {
        for (World world : plugin.getServer().getWorlds()) {
            if (Boolean.TRUE.equals(world.getGameRuleValue(org.bukkit.GameRule.KEEP_INVENTORY))) {
                plugin.compatibilityMessage("World \""
                                            + world.getName()
                                            + "\" has keepInventory set to true, Graves will not be created here.");
            }
        }

        Plugin essentialsPlugin = plugin.getServer().getPluginManager().getPlugin("Essentials");
        if (essentialsPlugin != null && essentialsPlugin.isEnabled()) {
            plugin.compatibilityMessage(essentialsPlugin.getName()
                                        + " Detected, make sure you don't have the essentials.keepinv or essentials.keepxp permissions.");
        }

        Plugin deluxeCombatPlugin = plugin.getServer().getPluginManager().getPlugin("DeluxeCombat");
        if (deluxeCombatPlugin != null && deluxeCombatPlugin.isEnabled()) {
            plugin.compatibilityMessage(deluxeCombatPlugin.getName()
                                        + " Detected, in order to work with graves you need to set disable-drop-handling to true in "
                                        + deluxeCombatPlugin.getName()
                                        + "'s data.yml file.");
        }

        similarPluginWarning("DeadChest");
        similarPluginWarning("DeathChest");
        similarPluginWarning("DeathChestPro");
        similarPluginWarning("SavageDeathChest");
        similarPluginWarning("AngelChest");
    }

    private void similarPluginWarning(String string) {
        Plugin similarPlugin = plugin.getServer().getPluginManager().getPlugin(string);

        if (similarPlugin != null && similarPlugin.isEnabled()) {
            plugin.compatibilityMessage(string
                                        + " Detected, Graves listens to the death event after "
                                        + string
                                        + ", and "
                                        + string
                                        + " clears the drop list. This means Graves will never be created for players "
                                        + "if "
                                        + string
                                        + " is enabled, only non-player entities will create Graves if configured to do so.");
        }
    }
}
