package org.avarion.graves.listener;

import org.avarion.graves.Graves;
import org.avarion.graves.type.Grave;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class BlockBreakListener implements Listener {

    private final Graves plugin;

    public BlockBreakListener(Graves plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Grave grave = plugin.getBlockManager().getGraveFromBlock(block);

        if (grave == null) {
            return;
        }


        if (!plugin.getEntityManager().canOpenGrave(player, grave)) {
            plugin.getEntityManager().sendMessage("message.protection", player, player.getLocation(), grave);
            event.setCancelled(true);
            return;
        }


        plugin.getGraveManager().autoLootGrave(player, block.getLocation(), grave);

        event.setDropItems(false);
    }
}
