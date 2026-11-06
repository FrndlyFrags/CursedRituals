package com.itemrituals.cage;

import com.itemrituals.config.ConfigManager;
import com.itemrituals.plugin.ItemRitualsPlugin;
import com.itemrituals.ritual.Ritual;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class ObsidianCageManager {

    private final ItemRitualsPlugin plugin;
    private BukkitTask cageTask;
    private boolean cageActive;
    private final Map<Location, BlockData> originalBlocks = new HashMap<>();
    private final Set<Location> cageBlocks = new HashSet<>();
    private BukkitTask gradualCageTask;
    private BukkitTask gradualRemoveTask;
    private int totalBlocksToPlace = 0;
    private int blocksRemoved = 0;

    public ObsidianCageManager(ItemRitualsPlugin plugin) {
        this.plugin = plugin;
        this.cageActive = false;
    }

    public void startCage(Ritual ritual) {
        if (!plugin.getConfigManager().isObsidianCageEnabled()) return;
        if (cageActive) stopCage();

        ConfigManager config = plugin.getConfigManager();
        int warningTime = config.getObsidianCageWarningTime();
        int ritualDuration = ritual.getDuration();
        long delaySeconds = Math.max(0, ritualDuration - warningTime);

        preCalculateCageLocations(ritual);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!cageActive || ritual.isCompleted() || ritual.isCancelled()) return;
                createObsidianCage(ritual);
            }
        }.runTaskLater(plugin, delaySeconds * 20L);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!cageActive || ritual.isCompleted() || ritual.isCancelled()) return;
                plugin.getCageAnnouncementManager().announceCageFormation(ritual);
            }
        }.runTaskLater(plugin, (delaySeconds - config.getObsidianCageAnnouncementDelay()) * 20L);

        cageActive = true;
    }

    public void stopCage() {
        if (!cageActive) return;
        if (cageTask != null) cageTask.cancel();
        if (gradualCageTask != null) gradualCageTask.cancel();
        removeObsidianCage();
        cageActive = false;
        cageBlocks.clear();
        originalBlocks.clear();
    }

    private void preCalculateCageLocations(Ritual ritual) {
        Location center = ritual.getCenterLocation();
        if (center == null || center.getWorld() == null) return;
        int size = plugin.getConfigManager().getObsidianCageSize();
        int halfSize = size / 2;
        cageBlocks.clear();
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                if (x == -halfSize || x == halfSize || z == -halfSize || z == halfSize) {
                    for (int y = -3; y <= size; y++) cageBlocks.add(center.clone().add(x, y, z));
                }
            }
        }
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                cageBlocks.add(center.clone().add(x, -3, z));
                cageBlocks.add(center.clone().add(x, size, z));
            }
        }
        totalBlocksToPlace = cageBlocks.size();
    }

    private void createObsidianCage(Ritual ritual) {
        String materialStr = plugin.getConfigManager().getObsidianCageMaterial();
        Material material = Material.matchMaterial(materialStr.toUpperCase());
        if (material == null) material = Material.OBSIDIAN;

        gradualCageTask = new BukkitRunnable() {
            private final java.util.Iterator<Location> iterator = cageBlocks.iterator();
            private int blocksPlacedInCurrentTick = 0;

            @Override
            public void run() {
                blocksPlacedInCurrentTick = 0;
                while (iterator.hasNext() && blocksPlacedInCurrentTick < plugin.getConfigManager().getObsidianCageBlocksPerTick()) {
                    Location loc = iterator.next();
                    captureAndSetBlock(loc, material);
                    blocksPlacedInCurrentTick++;
                }
                if (!iterator.hasNext()) {
                    cancel();
                    startPeriodicRepair(ritual, material);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void captureAndSetBlock(Location loc, Material material) {
        Block block = loc.getBlock();
        originalBlocks.put(loc.clone(), block.getBlockData().clone());
        block.setType(material);
    }

    private void startPeriodicRepair(Ritual ritual, Material material) {
        cageTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!cageActive || ritual.isCompleted() || ritual.isCancelled()) {
                    cancel();
                    return;
                }
                for (Location loc : cageBlocks) {
                    if (loc.getWorld() == null) continue;
                    Block block = loc.getWorld().getBlockAt(loc);
                    if (!block.getType().equals(material)) block.setType(material);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void removeObsidianCage() {
        gradualRemoveTask = new BukkitRunnable() {
            private final java.util.Iterator<Map.Entry<Location, BlockData>> iterator = originalBlocks.entrySet().iterator();
            private int blocksRemovedInCurrentTick = 0;

            @Override
            public void run() {
                blocksRemovedInCurrentTick = 0;
                while (iterator.hasNext() && blocksRemovedInCurrentTick < plugin.getConfigManager().getObsidianCageBlocksPerTick()) {
                    Map.Entry<Location, BlockData> entry = iterator.next();
                    entry.getKey().getBlock().setBlockData(entry.getValue());
                    blocksRemovedInCurrentTick++;
                }
                if (!iterator.hasNext()) cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void shutdown() { stopCage(); }
    public boolean isCageActive() { return cageActive; }
    public boolean isCageBlock(Location location) { return cageActive && cageBlocks.contains(location); }
}