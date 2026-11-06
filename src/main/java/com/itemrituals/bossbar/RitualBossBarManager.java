package com.itemrituals.bossbar;

import com.itemrituals.plugin.ItemRitualsPlugin;
import com.itemrituals.ritual.Ritual;
import com.itemrituals.ritual.RitualPhase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;

public class RitualBossBarManager {

    private final ItemRitualsPlugin plugin;
    private BossBar activeBossBar;
    private BukkitRunnable updateTask;

    public RitualBossBarManager(ItemRitualsPlugin plugin) {
        this.plugin = plugin;
    }

    public void createBossBar(Ritual ritual) {
        removeBossBar();
        BarColor color = determineBossBarColor(ritual);
        BarStyle style = BarStyle.SEGMENTED_10;
        activeBossBar = Bukkit.createBossBar("☠ RITUAL ☠", color, style);
        activeBossBar.setVisible(true);
        Bukkit.getOnlinePlayers().forEach(activeBossBar::addPlayer);
        long interval = plugin.getConfigManager().getBossBarUpdateInterval();
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (activeBossBar == null || ritual == null || ritual.isCompleted() || ritual.isCancelled()) {
                    cancel(); return;
                }
                double progress = 1.0 - ritual.getProgress();
                activeBossBar.setProgress(Math.max(0, Math.min(1, progress)));
                BarColor newColor = determineBossBarColor(ritual);
                if (activeBossBar.getColor() != newColor) {
                    activeBossBar.setColor(newColor);
                }
                long remainingTime = ritual.getRemainingTime() / 1000;
                String time = String.format("%02d:%02d", remainingTime / 60, remainingTime % 60);
                String itemName = ritual.getRewardItem().getItemMeta().hasDisplayName()
                        ? ritual.getRewardItem().getItemMeta().getDisplayName()
                        : ritual.getRewardItem().getType().name();
                String phase = getPhaseIndicator(ritual.getPhase());

                // Get coordinates with validation for world border
                Location center = ritual.getCenterLocation();
                String coordinates = formatCoordinates(center);

                // Add color to the title based on phase
                String coloredTitle = "";
                switch (ritual.getPhase()) {
                    case AWAKENING:
                        coloredTitle = "§7☠ " + itemName + " §8| §7" + phase + " §8| §7" + time + " §8| §7" + coordinates;
                        break;
                    case GATHERING:
                        coloredTitle = "§a☠ " + itemName + " §8| §7" + phase + " §8| §7" + time + " §8| §7" + coordinates;
                        break;
                    case INTENSIFYING:
                        coloredTitle = "§e☠ " + itemName + " §8| §7" + phase + " §8| §7" + time + " §8| §7" + coordinates;
                        break;
                    case FINAL:
                        coloredTitle = "§c☠ " + itemName + " §8| §7" + phase + " §8| §7" + time + " §8| §7" + coordinates;
                        break;
                    case COMPLETION:
                        coloredTitle = "§5☠ " + itemName + " §8| §7" + phase + " §8| §7" + time + " §8| §7" + coordinates;
                        break;
                    default:
                        coloredTitle = "☠ " + itemName + " §8| §7" + phase + " §8| §7" + time + " §8| §7" + coordinates;
                }

                activeBossBar.setTitle(coloredTitle);
            }
        };
        updateTask.runTaskTimer(plugin, 0L, interval);
    }

    private String formatCoordinates(Location center) {
        if (center == null || center.getWorld() == null) {
            return "§8[§7Invalid§8]§r";
        }

        WorldBorder border = center.getWorld().getWorldBorder();
        Location displayCenter = center;

        // Check if location is within world border
        if (!border.isInside(center)) {
            // Fallback to spawn location if current location is invalid
            Location spawnLoc = center.getWorld().getSpawnLocation();
            if (border.isInside(spawnLoc)) {
                displayCenter = spawnLoc;
                plugin.getLogger().warning("Ritual location outside world border, using spawn: " + displayCenter);
            } else {
                // Use border center as last resort
                displayCenter = new Location(center.getWorld(), border.getCenter().getX(), border.getCenter().getY(), border.getCenter().getZ());
                plugin.getLogger().severe("Both ritual and spawn locations outside world border, using border center");
            }
        }

        return "§8[§7X:§r§f" + displayCenter.getBlockX() + "§8, §7Y:§r§f" + displayCenter.getBlockY() + "§8, §7Z:§r§f" + displayCenter.getBlockZ() + "§8]§r";
    }

    private BarColor determineBossBarColor(Ritual ritual) {
        RitualPhase phase = ritual.getPhase();
        switch (phase) {
            case AWAKENING:
                return BarColor.BLUE;
            case GATHERING:
                return BarColor.GREEN;
            case INTENSIFYING:
                return BarColor.YELLOW;
            case FINAL:
                return BarColor.RED;
            case COMPLETION:
                return BarColor.PURPLE;
            default:
                return BarColor.RED;
        }
    }

    private String getPhaseIndicator(RitualPhase phase) {
        switch (phase) {
            case AWAKENING:
                return "§7[Awakening]§r";
            case GATHERING:
                return "§a[Gathering]§r";
            case INTENSIFYING:
                return "§e[Intensifying]§r";
            case FINAL:
                return "§c[Final Phase]§r";
            case COMPLETION:
                return "§5[Completion]§r";
            default:
                return "";
        }
    }

    public void removeBossBar() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        if (activeBossBar != null) {
            activeBossBar.removeAll();
            activeBossBar = null;
        }
    }
}
