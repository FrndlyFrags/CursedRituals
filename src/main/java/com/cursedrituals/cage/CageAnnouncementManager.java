package com.cursedrituals.cage;

import com.cursedrituals.config.ConfigManager;
import com.cursedrituals.plugin.CursedRitualsPlugin;
import com.cursedrituals.ritual.Ritual;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.List;

public class CageAnnouncementManager {
    private final CursedRitualsPlugin plugin;

    public CageAnnouncementManager(CursedRitualsPlugin plugin) {
        this.plugin = plugin;
    }

    public void announceCageFormation(Ritual ritual) {
        ConfigManager config = plugin.getConfigManager();
        if (!config.isObsidianCageAnnounce() || config.getObsidianCageAnnounceMsg().isEmpty()) {
            return;
        }

        Location center = ritual.getCenterLocation();
        if (center == null || center.getWorld() == null) {
            return;
        }

        World world = center.getWorld();
        double radius = config.getRitualRadius();
        int announceRadius = config.getAnnounceLocalRadius();

        // Send announcements to players within the ritual radius
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(center) <= radius) {
                for (String msg : config.getObsidianCageAnnounceMsg()) {
                    player.sendMessage(msg);
                }
            }
        }
    }
}