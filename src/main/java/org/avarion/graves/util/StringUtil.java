package org.avarion.graves.util;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.avarion.graves.Graves;
import org.avarion.graves.config.Settings;
import org.avarion.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtil {

    private static final Pattern hexColorPattern = Pattern.compile("&#[a-fA-F0-9]{6}");

    private StringUtil() {
        // Don't do anything here
    }

    public static String format(@NotNull String string) {
        return capitalizeFully(string.replace("_", " "));
    }

    public static @NotNull String parseString(String string, Graves plugin) {
        return parseString(string, null, null, null, null, plugin);
    }

    public static @NotNull String parseString(String string, Entity entity, Graves plugin) {
        return parseString(string, entity, null, null, null, plugin);
    }

    public static @NotNull String parseString(String string, String name, Graves plugin) {
        return parseString(string, null, name, null, null, plugin);
    }

    public static @NotNull String parseString(String string, Grave grave, Graves plugin) {
        return parseString(string, null, null, null, grave, plugin);
    }

    public static @NotNull String parseString(String string, Location location, Grave grave, Graves plugin) {
        return parseString(string, null, null, location, grave, plugin);
    }

    public static @NotNull String parseString(String string, Entity entity, Location location, Grave grave, Graves plugin) {
        return parseString(string, entity, plugin.getEntityManager().getEntityName(entity), location, grave, plugin);
    }

    public static @NotNull String parseString(String string, Entity entity, String name, Location location, Grave grave, Graves plugin) {
        if (location != null) {
            String worldName = location.getWorld() != null ? location.getWorld().getName() : "";
            string = string.replace("%world%", worldName)
                           .replace("%world_formatted%", format(worldName))
                           .replace("%x%", String.valueOf(location.getBlockX() + 0.5))
                           .replace("%y%", String.valueOf(location.getBlockY() + 0.5))
                           .replace("%z%", String.valueOf(location.getBlockZ() + 0.5));

            if (string.contains("%distance%")
                && entity != null
                && entity.getWorld() != null
                && location.getWorld() != null
                && entity.getWorld().equals(location.getWorld())) {
                string = string.replace("%distance%", String.valueOf(Math.round(entity.getLocation().distance(location))));
            }
        }
        else {
            string = string.replace("%world%", "").replace("%world_formatted%", "");
        }

        if (grave != null) {
            string = string.replace("%uuid%", grave.getUUID().toString())
                           .replace("%owner_name%", grave.getOwnerName() != null ? grave.getOwnerName() : "")
                           .replace("%owner_name_display%", grave.getOwnerNameDisplay() != null
                                                            ? grave.getOwnerNameDisplay()
                                                            : (grave.getOwnerName() != null
                                                               ? grave.getOwnerName()
                                                               : ""))
                           .replace("%owner_type%", grave.getOwnerType() != null ? grave.getOwnerType().name() : "")
                           .replace("%owner_uuid%", grave.getOwnerUUID() != null ? grave.getOwnerUUID().toString() : "")
                           .replace("%killer_name%", grave.getKillerName() != null ? grave.getKillerName() : "")
                           .replace("%killer_name_display%", grave.getKillerNameDisplay() != null
                                                             ? grave.getKillerNameDisplay()
                                                             : (grave.getKillerName() != null
                                                                ? grave.getKillerName()
                                                                : ""))
                           .replace("%killer_type%", grave.getKillerType() != null ? grave.getKillerType().name() : "")
                           .replace("%killer_uuid%", grave.getKillerUUID() != null
                                                     ? grave.getKillerUUID().toString()
                                                     : "")
                           .replace("%time_creation%", String.valueOf(grave.getTimeCreation()))
                           .replace("%time_creation_formatted%", getDateString(grave, grave.getTimeCreation(), plugin))
                           .replace("%time_alive_remaining%", String.valueOf(grave.getTimeAliveRemaining()))
                           .replace("%time_alive_remaining_formatted%", getTimeString(grave, grave.getTimeAliveRemaining(), plugin))
                           .replace("%time_protection_remaining%", String.valueOf(grave.getTimeProtectionRemaining()))
                           .replace("%time_protection_remaining_formatted%", getTimeString(grave, grave.getTimeProtectionRemaining(), plugin))
                           .replace("%time_lived%", String.valueOf(grave.getLivedTime()))
                           .replace("%time_lived_formatted%", getTimeString(grave, grave.getLivedTime(), plugin))
                           .replace("%state_protection%", grave.getProtection() ? "Protected" : "Unprotected")
                           .replace("%item%", String.valueOf(grave.getItemAmount()));
            if (grave.getExperience() > 0) {
                string = string.replace("%level%", String.valueOf(ExperienceUtil.getLevelFromExperience(grave.getExperience())))
                               .replace("%experience%", String.valueOf(grave.getExperience()));
            }
            else {
                string = string.replace("%level%", "0").replace("%experience%", "0");
            }

            if (grave.getOwnerType() == EntityType.PLAYER && plugin.getIntegrationManager().hasPlaceholderAPI()) {
                string = PlaceholderAPI.setPlaceholders(plugin.getServer()
                                                              .getOfflinePlayer(grave.getOwnerUUID()), string);
            }
        }


        if (name != null) {
            string = string.replace("%name%", name)
                           .replace("%interact_name%", name)
                           .replace("%interact_type%", "null")
                           .replace("%interact_uuid%", "null");
        }

        if (entity != null) {
            string = string.replace("%interact_name%", plugin.getEntityManager().getEntityName(entity))
                           .replace("%interact_type%", entity.getType().name())
                           .replace("%interact_uuid%", entity.getUniqueId().toString());
        }


        Matcher matcher = hexColorPattern.matcher(string);

        while (matcher.find()) {
            String colorHex = string.substring(matcher.start() + 1, matcher.end());
            string = plugin.getVersionManager().hasHexColors
                     ? string.replace("&" + colorHex, ChatColor.of(colorHex)
                                                               .toString())
                     : string.replace(colorHex, "");
            matcher = hexColorPattern.matcher(string);
        }


        return string.replace("&", "§");
    }

    public static String parseTime(String string, @NotNull Grave grave) {
        long time = grave.getTimeCreation() - grave.getTimeAlive();
        int day = (int) TimeUnit.SECONDS.toDays(time);
        long hour = TimeUnit.SECONDS.toHours(time) - (day * 24L);
        long minute = TimeUnit.SECONDS.toMinutes(time) - (TimeUnit.SECONDS.toHours(time) * 60);
        long second = TimeUnit.SECONDS.toSeconds(time) - (TimeUnit.SECONDS.toMinutes(time) * 60);

        if (day > 0) {
            string = string.replace("%day%", String.valueOf(day));
        }

        if (hour > 0) {
            string = string.replace("%hour%", String.valueOf(hour));
        }

        if (minute > 0) {
            string = string.replace("%minute%", String.valueOf(minute));
        }

        if (second > 0) {
            string = string.replace("%second%", String.valueOf(second));
        }

        return string;
    }

    public static String getDateString(Grave grave, long time, Graves plugin) {
        if (time > 0) {
            return new SimpleDateFormat(Settings.DATE_FORMAT).format(new Date(time));
        }

        return Settings.INFINITE_TIME_TEXT;
    }

    public static String getTimeString(Grave grave, long time, Graves plugin) {
        if (time < 0) {
            return Settings.INFINITE_TIME_TEXT;
        }

        long seconds = time / 1000;
        long days = TimeUnit.SECONDS.toDays(seconds);
        seconds -= TimeUnit.DAYS.toSeconds(days);
        long hours = TimeUnit.SECONDS.toHours(seconds);
        seconds -= TimeUnit.HOURS.toSeconds(hours);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        seconds -= TimeUnit.MINUTES.toSeconds(minutes);

        StringBuilder builder = new StringBuilder();
        if (days > 0) {
            builder.append(days).append("d ");
        }
        if (hours > 0 || builder.length() > 0) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0 || builder.length() > 0) {
            builder.append(minutes).append("m ");
        }

        builder.append(seconds).append("s");
        return builder.toString().trim();
    }

    private static String capitalizeFully(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : string.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }

        return result.toString();
    }

}
