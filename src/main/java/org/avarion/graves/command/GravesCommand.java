package org.avarion.graves.command;

import org.avarion.graves.Graves;
import org.avarion.graves.type.Grave;
import org.avarion.graves.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class GravesCommand implements CommandExecutor, TabCompleter {

    private final Graves plugin;

    public GravesCommand(Graves plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String string, String @NotNull [] args) {
        if (args.length > 0) {
            sendBarebonesInfo(commandSender);
            return true;
        }

        if (commandSender instanceof Player player) {
            sendPlayerGraveList(player);
        }
        else {
            sendBarebonesInfo(commandSender);
        }
        return true;
    }

    private void sendBarebonesInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.RED
                           + "☠"
                           + ChatColor.DARK_GRAY
                           + " » "
                           + ChatColor.RESET
                           + "Use /graves in-game to list your graves and coordinates.");
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String string, @NotNull String @NotNull [] args) {
        List<String> options = new ArrayList<>();


        return options;
    }

    private void sendPlayerGraveList(@NotNull Player player) {
        List<Grave> graves = plugin.getGraveManager().getGraveList(player);
        String prefix = ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET;

        if (graves.isEmpty()) {
            player.sendMessage(prefix + "You have no active graves.");
            return;
        }

        player.sendMessage(prefix
                           + "You have "
                           + graves.size()
                           + " active grave"
                           + (graves.size() == 1 ? "" : "s")
                           + ":");

        int counter = 1;
        for (Grave grave : graves) {
            Location location = resolveGraveLocation(grave);
            String timeRemaining = StringUtil.getTimeString(grave, grave.getTimeAliveRemaining(), plugin);

            if (location == null) {
                player.sendMessage(ChatColor.RED
                                   + "  #"
                                   + counter
                                   + ChatColor.DARK_GRAY
                                   + " » "
                                   + ChatColor.RESET
                                   + "Unknown location"
                                   + ChatColor.DARK_GRAY
                                   + " – "
                                   + ChatColor.RESET
                                   + "expires in "
                                   + ChatColor.RED
                                   + timeRemaining
                                   + ChatColor.RESET);
            }
            else {
                String world = location.getWorld() != null ? location.getWorld().getName() : "Unknown";
                player.sendMessage(ChatColor.RED
                                   + "  #"
                                   + counter
                                   + ChatColor.DARK_GRAY
                                   + " » "
                                   + ChatColor.RESET
                                   + world
                                   + " @ "
                                   + location.getBlockX()
                                   + ", "
                                   + location.getBlockY()
                                   + ", "
                                   + location.getBlockZ()
                                   + ChatColor.DARK_GRAY
                                   + " – "
                                   + ChatColor.RESET
                                   + "expires in "
                                   + ChatColor.RED
                                   + timeRemaining
                                   + ChatColor.RESET);
            }

            counter++;
        }
    }

    private Location resolveGraveLocation(Grave grave) {
        List<Location> blockLocations = plugin.getBlockManager().getBlockList(grave);
        if (!blockLocations.isEmpty()) {
            return blockLocations.get(0);
        }

        return grave.getLocationDeath();
    }
}
