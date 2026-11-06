package com.itemrituals.announcement;

import com.itemrituals.plugin.ItemRitualsPlugin;
import com.itemrituals.ritual.Ritual;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.List;

public class AnnouncementManager {
    private final ItemRitualsPlugin plugin;
    public AnnouncementManager(ItemRitualsPlugin plugin) { this.plugin = plugin; }
    public void announceStart(Ritual ritual) { broadcast(plugin.getConfigManager().getAnnounceStartMsg(), ritual); }
    public void announceWarning(Ritual ritual) { broadcast(plugin.getConfigManager().getAnnounceWarningMsg(), ritual); }
    public void announceComplete(Ritual ritual) { broadcast(plugin.getConfigManager().getAnnounceCompleteMsg(), ritual); }
    public void announceCancel(Ritual ritual) { broadcast(plugin.getConfigManager().getAnnounceCancelMsg(), ritual); }
    private void broadcast(List<String> messages, Ritual ritual) {
        if (messages == null || messages.isEmpty()) return;
        Location loc = ritual.getCenterLocation();
        int radius = plugin.getConfigManager().getAnnounceLocalRadius();
        String itemName = ritual.getRewardItem().getItemMeta().hasDisplayName()
                ? ritual.getRewardItem().getItemMeta().getDisplayName()
                : ritual.getRewardItem().getType().name();
        for (String line : messages) {
            String formatted = line.replace("{x}", String.valueOf(loc.getBlockX()))
                    .replace("{y}", String.valueOf(loc.getBlockY()))
                    .replace("{z}", String.valueOf(loc.getBlockZ()))
                    .replace("{world}", loc.getWorld().getName())
                    .replace("{item}", itemName)
                    .replace("{radius}", String.valueOf(ritual.getRadius()))
                    .replace('&', '§');
            if (radius <= 0) Bukkit.broadcastMessage(formatted);
            else {
                double rSq = radius * radius;
                for (Player p : loc.getWorld().getPlayers())
                    if (p.getLocation().distanceSquared(loc) <= rSq) p.sendMessage(formatted);
            }
        }
    }

    public void sendTitleNotification(Location location, String title, int fadeIn, int stay, int fadeOut) {
        int radius = plugin.getConfigManager().getAnnounceLocalRadius();
        double rSq = radius * radius;

        for (Player player : location.getWorld().getPlayers()) {
            if (radius <= 0 || player.getLocation().distanceSquared(location) <= rSq) {
                player.sendTitle(
                    org.bukkit.ChatColor.DARK_RED + title,
                    "",
                    fadeIn,
                    stay,
                    fadeOut
                );
            }
        }
    }
}
