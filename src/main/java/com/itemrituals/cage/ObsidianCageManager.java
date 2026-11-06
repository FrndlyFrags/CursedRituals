package com.itemrituals.cage;

import com.itemrituals.config.ConfigManager;
import com.itemrituals.plugin.ItemRitualsPlugin;
import com.itemrituals.ritual.Ritual;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import org.bukkit.block.data.BlockData;

public class ObsidianCageManager {

    private final ItemRitualsPlugin plugin;
    private BukkitTask cageTask;
    private boolean cageActive;
    private final Map<Location, BlockData> originalBlocks = new HashMap<>();
    private final Set<Location> cageBlocks = new HashSet<>();

    public ObsidianCageManager(ItemRitualsPlugin plugin) {
        this.plugin = plugin;
        this.cageActive = false;
    }

    public void startCage(Ritual ritual) {
        if (!plugin.getConfigManager().isObsidianCageEnabled()) return;
        if (cageActive) stopCage();

        ConfigManager config = plugin.getConfigManager();
        int warningTime = config.getObsidianCageWarningTime(); // in seconds
        int ritualDuration = ritual.getDuration(); // total duration in seconds

        // Calculate delay: show cage warningTime seconds before ritual ends
        // If ritualDuration is less than warningTime, show cage immediately
        long delaySeconds = Math.max(0, ritualDuration - warningTime);

        plugin.getCageAnnouncementManager().announceCageFormation(ritual);

        // Schedule cage creation at the correct time (warningTime before end)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!cageActive || ritual.isCompleted() || ritual.isCancelled()) return;
                createObsidianCage(ritual);
            }
        }.runTaskLater(plugin, delaySeconds * 20L); // convert seconds to ticks

        cageActive = true;
    }

    public void stopCage() {
        if (!cageActive) return;

        if (cageTask != null) {
            cageTask.cancel();
            cageTask = null;
        }

        removeObsidianCage();

        cageActive = false;
        cageBlocks.clear();
    }

    // Called on server shutdown to ensure cage is cleaned up
    public void shutdown() {
        stopCage();
    }

    private void createObsidianCage(Ritual ritual) {
        Location center = ritual.getCenterLocation();
        if (center == null || center.getWorld() == null) return;

        World world = center.getWorld();
        int size = plugin.getConfigManager().getObsidianCageSize();
        String materialStr = plugin.getConfigManager().getObsidianCageMaterial();
        Material material = Material.matchMaterial(materialStr.toUpperCase());
        if (material == null) material = Material.OBSIDIAN;

        int halfSize = size / 2;

        // Clear previous cage blocks
        originalBlocks.clear();
        cageBlocks.clear();

        // Create a hollow obsidian cage (only outer walls)
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                // Only place blocks on the perimeter (outer walls)
                if (x == -halfSize || x == halfSize || z == -halfSize || z == halfSize) {
                    for (int y = -3; y <= size; y++) { // Extend 3 blocks below and size blocks above
                        Location blockLoc = center.clone().add(x, y, z);
                        captureAndSetBlock(blockLoc, material);
                    }
                }
            }
        }

        // Create a hollow floor (3 blocks below)
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                Location blockLoc = center.clone().add(x, -3, z);
                captureAndSetBlock(blockLoc, material);
            }
        }

        // Create a hollow ceiling (top)
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                Location blockLoc = center.clone().add(x, size, z);
                captureAndSetBlock(blockLoc, material);
            }
        }

        // Schedule periodic updates to replace any broken blocks
        final Material finalMaterial = material;
        cageTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!cageActive || ritual.isCompleted() || ritual.isCancelled()) {
                    cancel();
                    return;
                }

                // Check and replace any broken blocks
                for (Location loc : cageBlocks) {
                    if (loc.getWorld() == null) continue;
                    Block block = loc.getWorld().getBlockAt(loc);
                    if (!block.getType().equals(finalMaterial)) {
                        block.setType(finalMaterial);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check every second
    }

    private void captureAndSetBlock(Location loc, Material material) {
        Block block = loc.getBlock();
        if (cageBlocks.contains(loc)) return; // Don't capture twice

        originalBlocks.put(loc.clone(), block.getBlockData().clone());
        cageBlocks.add(loc.clone());
        block.setType(material);
    }

    private void removeObsidianCage() {
        // Restore all original blocks
        for (Map.Entry<Location, BlockData> entry : originalBlocks.entrySet()) {
            Location loc = entry.getKey();
            BlockData data = entry.getValue();
            if (loc.getWorld() == null) continue;
            loc.getBlock().setBlockData(data);
        }
        originalBlocks.clear();
    }

    public boolean isCageActive() {
        return cageActive;
    }

    public boolean isCageBlock(Location location) {
        if (!cageActive) return false;
        return cageBlocks.contains(location);
    }
}
