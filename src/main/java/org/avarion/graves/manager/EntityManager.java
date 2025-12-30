package org.avarion.graves.manager;

import org.avarion.graves.Graves;
import org.avarion.graves.config.Settings;
import org.avarion.graves.data.EntityData;
import org.avarion.graves.type.Grave;
import org.avarion.graves.util.*;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class EntityManager extends EntityDataManager {

    private final Graves plugin;

    public EntityManager(Graves plugin) {
        super(plugin);

        this.plugin = plugin;
    }



    public @NotNull Map<ItemStack, UUID> getCompassesFromInventory(@NotNull HumanEntity player) {
        Map<ItemStack, UUID> itemStackUUIDMap = new HashMap<>();

        for (ItemStack itemStack : player.getInventory().getContents()) {
            UUID uuid = getGraveUUIDFromItemStack(itemStack);

            if (uuid != null) {
                itemStackUUIDMap.put(itemStack, uuid);
            }
        }

        return itemStackUUIDMap;
    }

    public UUID getGraveUUIDFromItemStack(ItemStack itemStack) {
        if (itemStack != null && itemStack.getItemMeta() != null && itemStack.getItemMeta()
                                                                             .getPersistentDataContainer()
                                                                             .has(new NamespacedKey(plugin, "graveUUID"), PersistentDataType.STRING)) {
            return UUIDUtil.getUUID(itemStack.getItemMeta()
                                             .getPersistentDataContainer()
                                             .get(new NamespacedKey(plugin, "graveUUID"), PersistentDataType.STRING));
        }

        return null;
    }
    private Sound resolveSound(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }

        return switch (key) {
            case "sound.open" -> Settings.SOUND_OPEN;
            case "sound.close" -> Settings.SOUND_CLOSE;
            case "sound.loot" -> Settings.SOUND_LOOT;
            case "sound.protection" -> Settings.SOUND_PROTECTION;
            case "sound.experience" -> Settings.SOUND_EXPERIENCE;
            default -> {
                try {
                    yield Sound.valueOf(key.toUpperCase(Locale.ROOT));
                }
                catch (IllegalArgumentException exception) {
                    plugin.debugMessage(key.toUpperCase(Locale.ROOT) + " is not a Sound ENUM", 1);
                    yield null;
                }
            }
        };
    }

    private @Nullable String resolveMessage(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }

        return switch (key) {
            case "message.block" -> Settings.Messages.GRAVE_CREATED;
            case "message.timeout" -> Settings.Messages.GRAVE_TIMEOUT;
            case "message.failure-keep-inventory" -> Settings.Messages.FAILURE_KEEP;
            case "message.failure" -> Settings.Messages.FAILURE;
            case "message.permission-denied" -> Settings.Messages.PERMISSION_DENIED;
            case "message.protection" -> Settings.Messages.PROTECTION;
            case "message.distance" -> Settings.Messages.DISTANCE;
            case "message.distance-world" -> Settings.Messages.DISTANCE_WORLD;
            case "message.empty" -> Settings.Messages.EMPTY;
            default -> "";
        };
    }


    public void playWorldSound(String string, @NotNull Player player) {
        playWorldSound(string, player.getLocation(), null);
    }

    public void playWorldSound(String string, @NotNull Player player, Grave grave) {
        playWorldSound(string, player.getLocation(), grave);
    }

    public void playWorldSound(String string, Location location, Grave grave) {
        playWorldSound(string, location, 1, 1);
    }

    public void playWorldSound(String string, @NotNull Location location, float volume, float pitch) {
        if (location.getWorld() == null) {
            return;
        }

        Sound sound = resolveSound(string);
        if (sound == null) {
            return;
        }

        location.getWorld().playSound(location, sound, volume, pitch);
    }

    public void playPlayerSound(String string, Entity entity, @NotNull Grave grave) {
        playPlayerSound(string, entity, entity.getLocation(), 1, 1);
    }

    public void playPlayerSound(String string, Entity entity, Location location, @NotNull Grave grave) {
        playPlayerSound(string, entity, location, 1, 1);
    }

    public void playPlayerSound(String string, Entity entity, Location location) {
        playPlayerSound(string, entity, location, 1, 1);
    }

    public void playPlayerSound(String string, Entity entity, Location location, float volume, float pitch) {
        if (!(entity instanceof Player player) || location.getWorld() == null) {
            return;
        }

        Sound sound = resolveSound(string);
        if (sound == null) {
            return;
        }

        player.playSound(location, sound, volume, pitch);
    }

    public void sendMessage(String string, CommandSender commandSender) {
        if (commandSender instanceof Player player) {
            sendMessage(string, player, player.getLocation(), null);
        }
    }

    public void sendMessage(String string, Entity entity) {
        sendMessage(string, entity, entity.getLocation(), null);
    }

    public void sendMessage(String string, Entity entity, Location location, Grave grave) {
        sendMessage(string, entity, getEntityName(entity), location, grave);
    }

    public void sendMessage(String string, Entity entity, String name, Location location, Grave grave) {
        if (!(entity instanceof Player player)) {
            return;
        }

        String template = resolveMessage(string);
        if (template == null || template.isEmpty()) {
            return;
        }

        String decorated = Settings.Messages.PREFIX + template;
        player.sendMessage(StringUtil.parseString(decorated, entity, name, location, grave, plugin));
    }



    public boolean canOpenGrave(Player player, @NotNull Grave grave) {
        if (player.hasPermission("graves.bypass")) {
            return true;
        }

        UUID ownerUUID = grave.getOwnerUUID();
        return ownerUUID == null || ownerUUID.equals(player.getUniqueId());
    }

    public void spawnZombie(Location location, Entity entity, LivingEntity targetEntity, Grave grave) {
        // Zombies disabled in barebones build
    }

    public void spawnZombie(Location location, Grave grave) {
        // Zombies disabled in barebones build
    }


    public void removeEntity(Grave grave) {
        // No additional entities to clean up
    }

    public @NotNull Map<EquipmentSlot, ItemStack> getEquipmentMap(@NotNull LivingEntity livingEntity, Grave grave) {
        Map<EquipmentSlot, ItemStack> equipmentSlotItemStackMap = new HashMap<>();

        if (livingEntity.getEquipment() != null) {
            EntityEquipment entityEquipment = livingEntity.getEquipment();

            if (entityEquipment.getHelmet() != null && grave.getInventory().contains(entityEquipment.getHelmet())) {
                equipmentSlotItemStackMap.put(EquipmentSlot.HEAD, entityEquipment.getHelmet());
            }

            if (entityEquipment.getChestplate() != null && grave.getInventory()
                                                                .contains(entityEquipment.getChestplate())) {
                equipmentSlotItemStackMap.put(EquipmentSlot.CHEST, entityEquipment.getChestplate());
            }

            if (entityEquipment.getLeggings() != null && grave.getInventory().contains(entityEquipment.getLeggings())) {
                equipmentSlotItemStackMap.put(EquipmentSlot.LEGS, entityEquipment.getLeggings());
            }

            if (entityEquipment.getBoots() != null && grave.getInventory().contains(entityEquipment.getBoots())) {
                equipmentSlotItemStackMap.put(EquipmentSlot.FEET, entityEquipment.getBoots());
            }

            if (entityEquipment.getItemInMainHand().getType() != Material.AIR && grave.getInventory()
                                                                                      .contains(entityEquipment.getItemInMainHand())) {
                equipmentSlotItemStackMap.put(EquipmentSlot.HAND, entityEquipment.getItemInMainHand());
            }

            if (entityEquipment.getItemInOffHand().getType() != Material.AIR && grave.getInventory()
                                                                                     .contains(entityEquipment.getItemInOffHand())) {
                equipmentSlotItemStackMap.put(EquipmentSlot.OFF_HAND, entityEquipment.getItemInOffHand());
            }
        }

        return equipmentSlotItemStackMap;
    }

    public @NotNull String getEntityName(Entity entity) {
        return entity != null ? entity.getName() : "null";
    }

    public boolean hasDataString(@NotNull Entity entity, String string) {
        return entity.getPersistentDataContainer().has(new NamespacedKey(plugin, string), PersistentDataType.STRING);
    }

    public boolean hasDataByte(@NotNull Entity entity, String string) {
        return entity.getPersistentDataContainer().has(new NamespacedKey(plugin, string), PersistentDataType.BYTE);
    }

    public String getDataString(@NotNull Entity entity, String key) {
        if (entity.getPersistentDataContainer().has(new NamespacedKey(plugin, key), PersistentDataType.STRING)) {
            return entity.getPersistentDataContainer().get(new NamespacedKey(plugin, key), PersistentDataType.STRING);
        }
        else {
            return entity.getMetadata(key).toString();
        }
    }

    public void setDataString(@NotNull Entity entity, String key, String string) {
        entity.getPersistentDataContainer().set(new NamespacedKey(plugin, key), PersistentDataType.STRING, string);
    }

    public void setDataByte(@NotNull Entity entity, String key) {
        entity.getPersistentDataContainer().set(new NamespacedKey(plugin, key), PersistentDataType.BYTE, (byte) 1);
    }

    public @Nullable Grave getGraveFromEntityData(@NotNull Entity entity) {
        if (entity.getPersistentDataContainer()
                  .has(new NamespacedKey(plugin, "graveUUID"), PersistentDataType.STRING)) {
            return CacheManager.graveMap.get(UUIDUtil.getUUID(entity.getPersistentDataContainer()
                                                                    .get(new NamespacedKey(plugin, "graveUUID"), PersistentDataType.STRING)));
        }
        else if (entity.hasMetadata("graveUUID")) {
            List<MetadataValue> metadataValue = entity.getMetadata("graveUUID");

            if (!metadataValue.isEmpty()) {
                return CacheManager.graveMap.get(UUIDUtil.getUUID(metadataValue.get(0).asString()));
            }
        }

        return null;
    }

}
