package org.avarion.graves.listener;

import org.avarion.graves.Graves;
import org.avarion.graves.manager.CacheManager;
import org.avarion.graves.type.Grave;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class PlayerInteractListener implements Listener {

    private final Graves plugin;

    public PlayerInteractListener(Graves plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getHand() != null
            && event.getHand() == EquipmentSlot.HAND
            && player.getGameMode() != GameMode.SPECTATOR) {
            // Right-click grave opening disabled in build-3
            // Graveyard functionality removed in build-3

            // Compass
            if (event.getItem() != null
                && player.getInventory().getItem(player.getInventory().getHeldItemSlot()) == event.getItem()) {
                ItemStack itemStack = event.getItem();
                UUID uuid = plugin.getEntityManager().getGraveUUIDFromItemStack(itemStack);

                if (uuid != null) {
                    if (CacheManager.graveMap.containsKey(uuid)) {
                        Grave grave = CacheManager.graveMap.get(uuid);
                        List<Location> locationList = plugin.getGraveManager()
                                                            .getGraveLocationList(player.getLocation(), grave);

                        if (!locationList.isEmpty()) {
                            Location location = locationList.get(0);

                            player.getInventory()
                                  .setItem(player.getInventory().getHeldItemSlot(), plugin.getEntityManager()
                                                                                          .createGraveCompass(player, location, grave));

                            if (player.getWorld().equals(location.getWorld())) {
                                plugin.getEntityManager().sendMessage("message.distance", player, location, grave);
                            }
                            else {
                                plugin.getEntityManager()
                                      .sendMessage("message.distance-world", player, location, grave);
                            }
                        }
                        else {
                            player.getInventory().remove(itemStack);
                        }
                    }
                    else {
                        player.getInventory().remove(itemStack);
                    }
                }
            }
        }
    }

}
