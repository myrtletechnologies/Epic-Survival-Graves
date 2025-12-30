package org.avarion.graves.config;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

import java.time.Duration;

public final class Settings {

    private Settings() {
    }

    public static final long GRAVE_LIFETIME_MILLIS = Duration.ofMinutes(30).toMillis();
    public static final double EXPERIENCE_RETURN_RATIO = 0.90;
    public static final Material GRAVE_BLOCK_MATERIAL = Material.PLAYER_HEAD;

    public static final Particle GRAVE_PARTICLE = Particle.ENCHANTMENT_TABLE;
    public static final int GRAVE_PARTICLE_COUNT = 8;
    public static final double GRAVE_PARTICLE_SPREAD = 0.25;

    public static final Sound SOUND_OPEN = Sound.BLOCK_FENCE_GATE_OPEN;
    public static final Sound SOUND_CLOSE = Sound.BLOCK_FENCE_GATE_CLOSE;
    public static final Sound SOUND_LOOT = Sound.ITEM_FIRECHARGE_USE;
    public static final Sound SOUND_PROTECTION = Sound.BLOCK_CHEST_LOCKED;
    public static final Sound SOUND_EXPERIENCE = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

    public static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String INFINITE_TIME_TEXT = "Forever";

    public static final class Messages {
        private Messages() {
        }

        public static final String PREFIX = ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET;
        public static final String PERMISSION_DENIED = "Permission denied.";
        public static final String EMPTY = "You have no active graves.";
        public static final String GRAVE_CREATED = "Grave created at &c%world_formatted%&r @ &c%x%&rx, &c%y%&ry, &c%z%&rz. Expires in &c%time_alive_remaining_formatted%&r.";
        public static final String GRAVE_TIMEOUT = "Your grave at &c%world_formatted%&r @ &c%x%&rx, &c%y%&ry, &c%z%&rz timed out with &c%item%&r items and &c%experience%&r XP.";
        public static final String FAILURE_KEEP = "A grave was unable to find a suitable location; preserving inventory.";
        public static final String FAILURE = "A grave was unable to find a suitable location; items dropped on the ground.";
        public static final String PROTECTION = "You cannot access that grave.";
        public static final String DISTANCE = "You are &c%distance%&r blocks away.";
        public static final String DISTANCE_WORLD = "Can't measure distance while in a different world.";
    }

    public static final class Titles {
        private Titles() {
        }

        public static final String GRAVE_INVENTORY = "%owner_name%'s Grave";
    }

    public static final class Head {
        private Head() {
        }

        public static final String NAME = "%owner_name%'s Head";
        public static final String[] LORE = {"Killed by %killer_name%"};
    }
}
