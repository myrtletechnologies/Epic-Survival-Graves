package org.avarion.graves.manager;

import org.avarion.graves.Graves;
import org.avarion.graves.config.Settings;
import org.avarion.graves.type.Grave;
import org.avarion.graves.util.StringUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ItemStackManager extends EntityDataManager {
    private final Graves plugin;

    public ItemStackManager(Graves plugin) {
        super(plugin);

        this.plugin = plugin;
    }

    public @NotNull ItemStack getGraveHead(Grave grave) {
        ItemStack itemStack = plugin.getCompatibility().getSkullItemStack(grave, plugin);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            List<String> loreList = new ArrayList<>();
            for (String template : Settings.Head.LORE) {
                loreList.add(StringUtil.parseString(template, grave.getLocationDeath(), grave, plugin));
            }

            itemMeta.setLore(loreList);
            itemMeta.setDisplayName(StringUtil.parseString(Settings.Head.NAME, grave, plugin));
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }
}
