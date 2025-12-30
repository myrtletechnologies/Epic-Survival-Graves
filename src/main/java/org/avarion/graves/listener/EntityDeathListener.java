package org.avarion.graves.listener;

import org.avarion.graves.Graves;
import org.avarion.graves.config.Settings;
import org.avarion.graves.type.Grave;
import org.avarion.graves.util.ExperienceUtil;
import org.avarion.graves.util.LocationUtil;
import org.avarion.graves.util.SkinUtil;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class EntityDeathListener implements EventExecutor {

    private final Graves plugin;

    public EntityDeathListener(Graves plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Listener listener, Event initEvent) throws EventException {
        if (!(initEvent instanceof EntityDeathEvent event)) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        List<ItemStack> drops = new ArrayList<>(event.getDrops());
        drops.removeIf(itemStack -> itemStack == null || itemStack.getType().isAir());

        if (drops.isEmpty()) {
            plugin.debugMessage("Skipping grave creation for " + player.getName() + " because they had no drops.", 2);
            return;
        }

        Grave grave = createGrave(player, event);

        Location deathLocation = LocationUtil.roundLocation(player.getLocation());
        Location placement = plugin.getLocationManager().getSafeGraveLocation(player, deathLocation, grave);

        if (placement == null) {
            preserveInventory(event, player, deathLocation, grave);
            return;
        }

        event.getDrops().clear();
        event.setDroppedExp(0);

        grave.setLocationDeath(placement);
        grave.getLocationDeath().setYaw(grave.getYaw());
        grave.getLocationDeath().setPitch(grave.getPitch());
        grave.setInventory(plugin.getGraveManager()
                                         .getGraveInventory(grave, player, drops, Collections.emptyList(), Collections.emptyList()));
        grave.setEquipmentMap(plugin.getEntityManager().getEquipmentMap(player, grave));

        plugin.getDataManager().addGrave(grave);
        plugin.getGraveManager().placeGrave(placement, grave);
        plugin.getEntityManager().sendMessage("message.block", player, placement, grave);
    }

    private Grave createGrave(Player player, EntityDeathEvent event) {
        Grave grave = new Grave(UUID.randomUUID());
        grave.setOwnerType(EntityType.PLAYER);
        grave.setOwnerName(player.getName());
        grave.setOwnerNameDisplay(player.getDisplayName());
        grave.setOwnerUUID(player.getUniqueId());
        grave.setYaw(player.getLocation().getYaw());
        grave.setPitch(player.getLocation().getPitch());
        grave.setPermissionList(Collections.emptyList());
        grave.setTimeAlive(Settings.GRAVE_LIFETIME_MILLIS);
        grave.setOwnerTexture(SkinUtil.getTexture(player));
        grave.setOwnerTextureSignature(SkinUtil.getSignature(player));
        grave.setStoredInventoryLayout(captureStorageContents(player));

        int totalExperience = ExperienceUtil.getPlayerExperience(player);
        grave.setExperience(ExperienceUtil.getDropPercent(totalExperience, Settings.EXPERIENCE_RETURN_RATIO));

        if (event instanceof PlayerDeathEvent deathEvent) {
            deathEvent.setKeepLevel(false);
        }

        if (player.getKiller() != null) {
            grave.setKillerType(EntityType.PLAYER);
            grave.setKillerName(player.getKiller().getName());
            grave.setKillerNameDisplay(player.getKiller().getDisplayName());
            grave.setKillerUUID(player.getKiller().getUniqueId());
        }
        else if (player.getLastDamageCause() != null) {
            EntityDamageEvent damageEvent = player.getLastDamageCause();
            if (damageEvent instanceof EntityDamageByEntityEvent byEntityEvent) {
                grave.setKillerUUID(byEntityEvent.getDamager().getUniqueId());
                grave.setKillerType(byEntityEvent.getDamager().getType());
                grave.setKillerName(plugin.getEntityManager().getEntityName(byEntityEvent.getDamager()));
                grave.setKillerNameDisplay(grave.getKillerName());
            }
            else {
                grave.setKillerName(plugin.getGraveManager().getDamageReason(damageEvent.getCause(), grave));
            }
        }

        return grave;
    }

    private void preserveInventory(EntityDeathEvent event, Player player, Location location, Grave grave) {
        if (event instanceof PlayerDeathEvent deathEvent) {
            deathEvent.setKeepInventory(true);
            deathEvent.setKeepLevel(true);
        }

        plugin.getEntityManager().sendMessage("message.failure-keep-inventory", player, location, grave);
    }

    private ItemStack[] captureStorageContents(Player player) {
        ItemStack[] storageContents = player.getInventory().getStorageContents();
        ItemStack[] clone = new ItemStack[storageContents.length];

        for (int i = 0; i < storageContents.length; i++) {
            ItemStack itemStack = storageContents[i];
            clone[i] = itemStack != null ? itemStack.clone() : null;
        }

        return clone;
    }
}
