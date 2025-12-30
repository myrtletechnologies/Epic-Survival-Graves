package org.avarion.graves.manager;

import org.avarion.graves.Graves;
import org.avarion.graves.config.Settings;
import org.avarion.graves.data.BlockData;
import org.avarion.graves.data.ChunkData;
import org.avarion.graves.data.EntityData;
import org.avarion.graves.type.Grave;
import org.avarion.graves.util.InventoryUtil;
import org.avarion.graves.util.StringUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class GraveManager {
    private final Graves plugin;

    public GraveManager(Graves plugin) {
        this.plugin = plugin;

        startGraveTimer();
    }

    private void startGraveTimer() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            List<Grave> graveRemoveList = new ArrayList<>();
            List<EntityData> entityDataRemoveList = new ArrayList<>();
            List<BlockData> blockDataRemoveList = new ArrayList<>();

            // Graves
            for (Map.Entry<UUID, Grave> entry : CacheManager.graveMap.entrySet()) {
                Grave grave = entry.getValue();

                if (grave.getTimeAliveRemaining() >= 0 && grave.getTimeAliveRemaining() <= 1000) {
                    Location dropLocation = getPrimaryGraveLocation(grave, grave.getLocationDeath());
                    if (dropLocation != null) {
                        dropGraveItems(dropLocation, grave);
                        dropGraveExperience(dropLocation, grave);
                    }

                    if (grave.getOwnerType() == EntityType.PLAYER && grave.getOwnerUUID() != null) {
                        Player player = plugin.getServer().getPlayer(grave.getOwnerUUID());

                        if (player != null) {
                            plugin.getEntityManager().sendMessage("message.timeout", player, dropLocation, grave);
                        }
                    }

                    graveRemoveList.add(grave);
                }

                // Protection
                if (grave.getProtection() && grave.getTimeProtectionRemaining() == 0) {
                    toggleGraveProtection(grave);
                }
            }

            // Chunks
            for (Map.Entry<String, ChunkData> entry : CacheManager.chunkMap.entrySet()) {
                ChunkData chunkData = entry.getValue();

                if (chunkData.isLoaded()) {
                    Location location = new Location(chunkData.getWorld(), chunkData.getX() << 4, 0, chunkData.getZ()
                                                                                                     << 4);

                    // Entity data
                    for (EntityData entityData : new ArrayList<>(chunkData.getEntityDataMap().values())) {
                        if (!CacheManager.graveMap.containsKey(entityData.getUUIDGrave())) {
                            entityDataRemoveList.add(entityData);
                        }
                    }

                    // Blocks
                    for (BlockData blockData : new ArrayList<>(chunkData.getBlockDataMap().values())) {
                        if (blockData.location().getWorld() != null) {
                            if (CacheManager.graveMap.containsKey(blockData.graveUUID())) {
                                graveParticle(blockData.location(), CacheManager.graveMap.get(blockData.graveUUID()));
                            }
                            else {
                                blockDataRemoveList.add(blockData);
                            }
                        }
                    }
                }
            }

            if (plugin.isEnabled()) {
                graveRemoveList.forEach(GraveManager.this::removeGrave);
                entityDataRemoveList.forEach(GraveManager.this::removeEntityData);
                blockDataRemoveList.forEach(blockData -> plugin.getBlockManager().removeBlock(blockData));
                graveRemoveList.clear();
                blockDataRemoveList.clear();
                entityDataRemoveList.clear();
            }
        }, 10L, 20L);
    }

    @SuppressWarnings("ConstantConditions")
    public void unload() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory() != null
                && player.getOpenInventory().getTopInventory()
                   != null) { // Mohist, might return null even when Bukkit shouldn't.
                InventoryHolder inventoryHolder = player.getOpenInventory().getTopInventory().getHolder();

                if (inventoryHolder instanceof Grave) {
                    player.closeInventory();
                }
            }
        }
    }

    public void toggleGraveProtection(@NotNull Grave grave) {
        grave.setProtection(!grave.getProtection());
        plugin.getDataManager().updateGrave(grave, "protection", String.valueOf(grave.getProtection() ? 1 : 0));
    }

    public void graveParticle(@NotNull Location location, Grave grave) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        location = location.clone().add(0.5, 0.5, 0.5);
        world.spawnParticle(Particle.ENCHANTMENT_TABLE, location, 8, 0.25, 0.5, 0.25, 0.025);
    }

    public void removeGrave(Grave grave) {
        closeGrave(grave);
        plugin.getBlockManager().removeBlock(grave);
        plugin.getEntityManager().removeEntity(grave);
        plugin.getDataManager().removeGrave(grave);

        plugin.debugMessage("Removing grave " + grave.getUUID(), 1);
    }

    public void removeEntityData(@NotNull EntityData entityData) {
        // No entities to remove
    }

    @SuppressWarnings("ConstantConditions")
    public void closeGrave(@NotNull Grave grave) {
        List<HumanEntity> inventoryViewers = grave.getInventory().getViewers();

        for (HumanEntity humanEntity : new ArrayList<>(inventoryViewers)) {
            grave.getInventory().getViewers().remove(humanEntity);
            humanEntity.closeInventory();
            plugin.debugMessage("Closing grave " + grave.getUUID() + " for " + humanEntity.getName(), 1);
        }

    }

    public Grave.StorageMode getStorageMode(String string) {
        return Grave.StorageMode.COMPACT;
    }

    public void placeGrave(Location location, Grave grave) {
        plugin.getBlockManager().createBlock(location, grave);
    }

    public Inventory getGraveInventory(Grave grave, LivingEntity livingEntity, List<ItemStack> graveItemStackList, List<ItemStack> removedItemStackList, List<String> permissionList) {
        List<ItemStack> filterGraveItemStackList = filterGraveItemStackList(graveItemStackList, removedItemStackList, livingEntity, permissionList);
        String title = StringUtil.parseString("%owner_name%'s Grave", livingEntity, grave.getLocationDeath(), grave, plugin);
        Grave.StorageMode storageMode = Grave.StorageMode.COMPACT;

        return plugin.getGraveManager()
                     .createGraveInventory(grave, grave.getLocationDeath(), filterGraveItemStackList, title, storageMode);
    }

    public @Nullable Inventory createGraveInventory(InventoryHolder inventoryHolder, Location location, @NotNull List<ItemStack> itemStackList, String title, Grave.StorageMode storageMode) {
        itemStackList.removeIf(itemStack -> itemStack != null
                                            && itemStack.containsEnchantment(Enchantment.VANISHING_CURSE));

        if (storageMode == Grave.StorageMode.COMPACT) {
            Inventory tempInventory = plugin.getServer().createInventory(null, 54);
            int counter = 0;

            for (ItemStack itemStack : itemStackList) {
                if (getItemStacksSize(tempInventory.getContents()) < tempInventory.getSize()) {
                    if (itemStack != null && !itemStack.getType().isAir()) {
                        tempInventory.addItem(itemStack);
                        counter++;
                    }
                }
                else if (itemStack != null && location != null && location.getWorld() != null) {
                    location.getWorld().dropItem(location, itemStack);
                }
            }

            counter = 0;

            for (ItemStack itemStack : tempInventory.getContents()) {
                if (itemStack != null) {
                    counter++;
                }
            }

            Inventory inventory = plugin.getServer()
                                        .createInventory(inventoryHolder, InventoryUtil.getInventorySize(counter), title);

            for (ItemStack itemStack : tempInventory.getContents()) {
                if (itemStack != null && location != null && location.getWorld() != null) {
                    inventory.addItem(itemStack).forEach((key, value) -> location.getWorld().dropItem(location, value));
                }
            }

            return inventory;
        }

        if (storageMode == Grave.StorageMode.EXACT) {
            ItemStack itemStackAir = new ItemStack(Material.AIR);
            Inventory inventory = plugin.getServer()
                                        .createInventory(inventoryHolder, InventoryUtil.getInventorySize(itemStackList.size()), title);

            int counter = 0;
            for (ItemStack itemStack : itemStackList) {
                if (counter < inventory.getSize()) {
                    inventory.setItem(counter, itemStack != null ? itemStack : itemStackAir);
                }
                else if (itemStack != null && location != null && location.getWorld() != null) {
                    location.getWorld().dropItem(location, itemStack);
                }

                counter++;
            }

            return inventory;
        }

        return null;
    }

    @Contract(pure = true)
    public int getItemStacksSize(ItemStack @NotNull [] itemStacks) {
        int counter = 0;

        for (ItemStack itemStack : itemStacks) {
            if (itemStack != null) {
                counter++;
            }
        }

        return counter;
    }

    public @NotNull List<ItemStack> filterGraveItemStackList(List<ItemStack> itemStackList, List<ItemStack> removedItemStackList, LivingEntity livingEntity, List<String> permissionList) {
        itemStackList = new ArrayList<>(itemStackList);

        return itemStackList;
    }

    public void breakGrave(Location location, Grave grave) {
        dropGraveItems(location, grave);
        dropGraveExperience(location, grave);
        removeGrave(grave);
        plugin.debugMessage("Grave " + grave.getUUID() + " broken", 1);
    }

    public void dropGraveItems(Location location, Grave grave) {
        if (grave != null && location.getWorld() != null) {
            for (ItemStack itemStack : grave.getInventory()) {
                if (itemStack != null) {
                    location.getWorld().dropItemNaturally(location, itemStack);
                }
            }

            grave.getInventory().clear();
        }
    }

    public void giveGraveExperience(Player player, @NotNull Grave grave) {
        if (grave.getExperience() > 0) {
            player.giveExp(grave.getExperience());
            grave.setExperience(0);
            plugin.getEntityManager().playWorldSound("ENTITY_EXPERIENCE_ORB_PICKUP", player);
        }
    }

    public void dropGraveExperience(Location location, @NotNull Grave grave) {
        if (grave.getExperience() > 0 && location.getWorld() != null) {
            ExperienceOrb experienceOrb = (ExperienceOrb) location.getWorld()
                                                                  .spawnEntity(location, EntityType.EXPERIENCE_ORB);

            experienceOrb.setExperience(grave.getExperience());
            grave.setExperience(0);
        }
    }

    public @NotNull List<Grave> getGraveList(@NotNull Player player) {
        return getGraveList(player.getUniqueId());
    }

    public @NotNull List<Grave> getGraveList(@NotNull OfflinePlayer player) {
        return getGraveList(player.getUniqueId());
    }

    public @NotNull List<Grave> getGraveList(@NotNull Entity entity) {
        return getGraveList(entity.getUniqueId());
    }

    public @NotNull List<Grave> getGraveList(UUID uuid) {
        List<Grave> graveList = new ArrayList<>();

        CacheManager.graveMap.forEach((key, value) -> {
            if (value.getOwnerUUID() != null && value.getOwnerUUID().equals(uuid)) {
                graveList.add(value);
            }
        });

        return graveList;
    }

    public int getGraveCount(Entity entity) {
        return getGraveList(entity).size();
    }


    public void cleanupCompasses(Player player, Grave grave) {
        for (Map.Entry<ItemStack, UUID> entry : plugin.getEntityManager()
                                                      .getCompassesFromInventory(player)
                                                      .entrySet()) {
            if (grave.getUUID().equals(entry.getValue())) {
                player.getInventory().remove(entry.getKey());
            }
        }
    }

    private Location getPrimaryGraveLocation(Grave grave, Location fallback) {
        List<Location> blockLocations = plugin.getBlockManager().getBlockList(grave);
        if (!blockLocations.isEmpty()) {
            return blockLocations.get(0).clone();
        }

        if (fallback != null) {
            return fallback.clone();
        }

        return grave.getLocationDeath() != null ? grave.getLocationDeath().clone() : null;
    }

    public List<Location> getGraveLocationList(@NotNull Location baseLocation, Grave grave) {
        List<Location> locationList = new ArrayList<>(plugin.getBlockManager().getBlockList(grave));
        Map<Double, Location> locationMap = new HashMap<>();
        List<Location> otherWorldLocationList = new ArrayList<>();

        if (baseLocation.getWorld() != null) {
            if (!locationList.contains(grave.getLocationDeath())) {
                locationList.add(grave.getLocationDeath());
            }

            for (Location location : locationList) {
                if (location.getWorld() != null && baseLocation.getWorld().equals(location.getWorld())) {
                    locationMap.put(location.distanceSquared(baseLocation), location);
                }
                else {
                    otherWorldLocationList.add(location);
                }
            }

            locationList = new ArrayList<>(new TreeMap<>(locationMap).values());

            locationList.addAll(otherWorldLocationList);
        }

        return locationList;
    }

    public @Nullable Location getGraveLocation(Location location, Grave grave) {
        List<Location> locationList = plugin.getGraveManager().getGraveLocationList(location, grave);

        return !locationList.isEmpty() ? locationList.get(0) : null;
    }

    public void autoLootGrave(Entity entity, Location location, Grave grave) {
        if (!(entity instanceof Player player)) {
            return;
        }
        Location dropLocation = location != null ? location : player.getLocation();
        cleanupCompasses(player, grave);
        equipStoredEquipment(player, grave, dropLocation);

        InventoryUtil.equipArmor(grave.getInventory(), player);
        restoreStoredInventory(player, grave, dropLocation);
        InventoryUtil.equipItems(grave.getInventory(), player);

        player.updateInventory();
        giveGraveExperience(player, grave);

        playEffect("effect.loot", dropLocation, grave);
        plugin.getEntityManager().spawnZombie(dropLocation, player, player, grave);
        closeGrave(grave);
        removeGrave(grave);

        plugin.debugMessage("Grave " + grave.getUUID() + " auto-equipped for " + player.getName(), 1);
    }

    private void equipStoredEquipment(Player player, Grave grave, Location dropLocation) {
        Map<EquipmentSlot, ItemStack> equipmentMap = grave.getEquipmentMap();
        if (equipmentMap == null || equipmentMap.isEmpty()) {
            return;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack storedItem = equipmentMap.get(slot);
            if (storedItem == null || storedItem.getType().isAir()) {
                continue;
            }

            ItemStack copy = storedItem.clone();
            grave.getInventory().removeItem(copy);

            switch (slot) {
                case HEAD, CHEST, LEGS, FEET -> equipArmorPiece(player, copy, slot, dropLocation);
                case HAND -> equipHandItem(player, copy, true, dropLocation);
                case OFF_HAND -> equipHandItem(player, copy, false, dropLocation);
                default -> {
                }
            }
        }
    }

    private void equipArmorPiece(Player player, ItemStack newItem, EquipmentSlot slot, Location dropLocation) {
        PlayerInventory inventory = player.getInventory();
        ItemStack previous;

        switch (slot) {
            case HEAD -> {
                previous = inventory.getHelmet();
                inventory.setHelmet(newItem);
            }
            case CHEST -> {
                previous = inventory.getChestplate();
                inventory.setChestplate(newItem);
            }
            case LEGS -> {
                previous = inventory.getLeggings();
                inventory.setLeggings(newItem);
            }
            case FEET -> {
                previous = inventory.getBoots();
                inventory.setBoots(newItem);
            }
            default -> {
                return;
            }
        }

        storeOrDropItem(player, previous, dropLocation);
        InventoryUtil.playArmorEquipSound(player, newItem);
    }

    private void equipHandItem(Player player, ItemStack newItem, boolean mainHand, Location dropLocation) {
        PlayerInventory inventory = player.getInventory();
        ItemStack previous = mainHand ? inventory.getItemInMainHand() : inventory.getItemInOffHand();
        storeOrDropItem(player, previous, dropLocation);

        if (mainHand) {
            inventory.setItemInMainHand(newItem);
        }
        else {
            inventory.setItemInOffHand(newItem);
        }
    }
    private void restoreStoredInventory(Player player, Grave grave, Location dropLocation) {
        ItemStack[] storedLayout = grave.getStoredInventoryLayout();
        if (storedLayout == null || storedLayout.length == 0) {
            return;
        }

        for (int slot = 0; slot < storedLayout.length; slot++) {
            ItemStack storedItem = storedLayout[slot];

            if (storedItem == null || storedItem.getType().isAir()) {
                continue;
            }

            ItemStack restoreCopy = storedItem.clone();
            Map<Integer, ItemStack> leftovers = grave.getInventory().removeItem(restoreCopy.clone());

            if (!leftovers.isEmpty()) {
                continue;
            }

            placeItemInSlot(player, slot, restoreCopy, dropLocation);
        }
    }

    private void placeItemInSlot(Player player, int slot, ItemStack newItem, Location dropLocation) {
        PlayerInventory inventory = player.getInventory();
        ItemStack current = inventory.getItem(slot);

        if (current != null
            && !current.getType().isAir()
            && current.isSimilar(newItem)
            && current.getAmount() < current.getMaxStackSize()) {
            int space = current.getMaxStackSize() - current.getAmount();
            int transfer = Math.min(space, newItem.getAmount());

            if (transfer > 0) {
                current.setAmount(current.getAmount() + transfer);
                newItem.setAmount(newItem.getAmount() - transfer);
            }

            if (newItem.getAmount() <= 0) {
                return;
            }
        }

        ItemStack displaced = current != null && !current.getType().isAir() ? current.clone() : null;
        inventory.setItem(slot, newItem);

        if (displaced != null) {
            storeOrDropItem(player, displaced, dropLocation);
        }
    }

    private void storeOrDropItem(Player player, ItemStack itemStack, Location dropLocation) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return;
        }

        Map<Integer, ItemStack> overflow = player.getInventory().addItem(itemStack);
        if (!overflow.isEmpty()) {
            Location drop = dropLocation != null ? dropLocation : player.getLocation();
            overflow.values().forEach(value -> player.getWorld().dropItemNaturally(drop, value));
        }
    }

    public String getDamageReason(EntityDamageEvent.@NotNull DamageCause damageCause, Grave grave) {
        return StringUtil.format(damageCause.name());
    }

    public void playEffect(String string, Location location, Grave grave) {
        playEffect(string, location, 0, grave);
    }

    public void playEffect(String string, @NotNull Location location, int data, Grave grave) {
        if (location.getWorld() == null) {
            return;
        }

        location.getWorld().spawnParticle(Settings.GRAVE_PARTICLE,
                                          location,
                                          Settings.GRAVE_PARTICLE_COUNT,
                                          Settings.GRAVE_PARTICLE_SPREAD,
                                          Settings.GRAVE_PARTICLE_SPREAD,
                                          Settings.GRAVE_PARTICLE_SPREAD,
                                          0);
    }

    public boolean shouldIgnoreItemStack(@NotNull ItemStack itemStack, Entity entity, List<String> permissionList) {
        return false;
    }

    public boolean shouldIgnoreBlock(Block block, Entity entity, @NotNull Grave grave) {
        return shouldIgnoreBlock(block, entity, grave.getPermissionList());
    }

    public boolean shouldIgnoreBlock(Block block, Entity entity, List<String> permissionList) {
        return false;
    }
}
