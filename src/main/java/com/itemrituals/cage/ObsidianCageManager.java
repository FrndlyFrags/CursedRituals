package com.itemrituals.cage;

import com.itemrituals.config.ConfigManager;
import com.itemrituals.plugin.ItemRitualsPlugin;
import com.itemrituals.ritual.Ritual;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
        final int delaySeconds = (int) Math.max(0, ritualDuration - warningTime);

        preCalculateCageLocations(ritual);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!cageActive || ritual.isCompleted() || ritual.isCancelled()) return;
                createObsidianCage(ritual);
            }
        }.runTaskLater(plugin, delaySeconds * 20L);

        final int announcementDelay = delaySeconds - config.getObsidianCageAnnouncementDelay();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!cageActive || ritual.isCompleted() || ritual.isCancelled()) return;
                plugin.getCageAnnouncementManager().announceCageFormation(ritual);
            }
        }.runTaskLater(plugin, Math.max(0, announcementDelay) * 20L);

        cageActive = true;
    }

    public void stopCage() {
        if (!cageActive) return;
        if (cageTask != null) cageTask.cancel();
        if (gradualCageTask != null) gradualCageTask.cancel();
        if (gradualRemoveTask != null) gradualRemoveTask.cancel();

        removeObsidianCage();
        cageActive = false;
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
        Material matched = Material.matchMaterial(materialStr.toUpperCase());
        final Material cageMaterial = (matched != null) ? matched : Material.OBSIDIAN;
        final Ritual activeRitual = ritual;

        gradualCageTask = new BukkitRunnable() {
            private final Iterator<Location> iterator = cageBlocks.iterator();
            private int blocksPlacedInCurrentTick = 0;

            @Override
            public void run() {
                blocksPlacedInCurrentTick = 0;
                while (iterator.hasNext() && blocksPlacedInCurrentTick < plugin.getConfigManager().getObsidianCageBlocksPerTick()) {
                    Location loc = iterator.next();
                    captureAndSetBlock(loc, cageMaterial);
                    blocksPlacedInCurrentTick++;
                }
                if (!iterator.hasNext()) {
                    cancel();
                    startPeriodicRepair(activeRitual, cageMaterial);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void captureAndSetBlock(Location loc, Material material) {
        Block block = loc.getBlock();
        originalBlocks.put(loc.clone(), block.getBlockData().clone());
        block.setType(material);
    }

    private void startPeriodicRepair(Ritual ritual, final Material material) {
        final Ritual activeRitual = ritual;
        cageTask = new BukkitRunnable() {
            private Iterator<Location> iterator = cageBlocks.iterator();

            @Override
            public void run() {
                if (!cageActive || activeRitual.isCompleted() || activeRitual.isCancelled()) {
                    cancel();
                    return;
                }

                int blocksProcessed = 0;
                while (blocksProcessed < plugin.getConfigManager().getObsidianCageBlocksPerTick()) {
                    if (!iterator.hasNext()) {
                        iterator = cageBlocks.iterator();
                    }
                    Location loc = iterator.next();
                    if (loc.getWorld() != null) {
                        Block block = loc.getWorld().getBlockAt(loc);
                        if (block.getType() != material) block.setType(material);
                    }
                    blocksProcessed++;
                }
            }
        }.runTaskTimer(plugin, 20L, 1L);
    }

    private void removeObsidianCage() {
        if (originalBlocks.isEmpty()) return;
        final Map<Location, BlockData> blocksToRestore = new HashMap<>(originalBlocks);
        originalBlocks.clear();

        gradualRemoveTask = new BukkitRunnable() {
            private final Iterator<Map.Entry<Location, BlockData>> iterator = blocksToRestore.entrySet().iterator();
            private int blocksRemovedInCurrentTick = 0;

            @Override
            public void run() {
                blocksRemovedInCurrentTick = 0;
                while (iterator.hasNext() && blocksRemovedInCurrentTick < plugin.getConfigManager().getObsidianCageBlocksPerTick()) {
                    Map.Entry<Location, BlockData> entry = iterator.next();
                    entry.getKey().getBlock().setBlockData(entry.getValue());
                    blocksRemovedInCurrentTick++;
                }
                if (!iterator.hasNext()) {
                    cancel();
                    cageBlocks.clear();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void shutdown() { stopCage(); }
    public boolean isCageActive() { return cageActive; }
    public boolean isCageBlock(Location location) { return cageActive && cageBlocks.contains(location); }
}
