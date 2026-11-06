package com.itemrituals.cage;

import com.itemrituals.config.ConfigManager;
import com.itemrituals.plugin.ItemRitualsPlugin;
import com.itemrituals.ritual.Ritual;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.List;

public class CageAnnouncementManager {
    private final ItemRitualsPlugin plugin;

    public CageAnnouncementManager(ItemRitualsPlugin plugin) {
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