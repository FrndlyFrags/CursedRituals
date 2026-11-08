package com.cursedrituals.cage;

import com.cursedrituals.plugin.CursedRitualsPlugin;
import com.cursedrituals.ritual.Ritual;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;

public class ObsidianCageManager {

    private final CursedRitualsPlugin plugin;
    private BukkitTask cageTask;
    private boolean cageActive;
    private final Map<Location, BlockData> originalBlocks = new HashMap<>();
    private final Set<Location> cageBlocks = new HashSet<>();
    private BukkitTask gradualCageTask;
    private BukkitTask gradualRemoveTask;

    public ObsidianCageManager(CursedRitualsPlugin plugin) {
        this.plugin = plugin;
        this.cageActive = false;
    }

    public void startCage(Ritual ritual) {
        if (!plugin.getConfigManager().isObsidianCageEnabled()) return;
        if (cageActive) stopCage();

        preCalculateCageLocations(ritual);
        cageActive = true;

        int delay = Math.max(0, ritual.getDuration() - plugin.getConfigManager().getObsidianCageWarningTime());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!cageActive || ritual.isCompleted() || ritual.isCancelled()) return;
                createObsidianCage(ritual);
            }
        }.runTaskLater(plugin, delay * 20L);
    }

    public void stopCage() {
        if (cageTask != null) cageTask.cancel();
        if (gradualCageTask != null) gradualCageTask.cancel();
        if (gradualRemoveTask != null) gradualRemoveTask.cancel();

        removeObsidianCage();
        cageActive = false;
    }

    private void removeObsidianCage() {
        if (originalBlocks.isEmpty()) {
            cageBlocks.clear();
            return;
        }

        final Map<Location, BlockData> toRestore = new HashMap<>(originalBlocks);
        originalBlocks.clear();
        cageBlocks.clear();

        gradualRemoveTask = new BukkitRunnable() {
            private final Iterator<Map.Entry<Location, BlockData>> it = toRestore.entrySet().iterator();

            @Override
            public void run() {
                int perTick = plugin.getConfigManager().getObsidianCageBlocksPerTick();
                for (int i = 0; i < perTick && it.hasNext(); i++) {
                    Map.Entry<Location, BlockData> entry = it.next();
                    entry.getKey().getBlock().setBlockData(entry.getValue());
                }
                if (!it.hasNext()) cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void preCalculateCageLocations(Ritual ritual) {
        Location center = ritual.getCenterLocation();
        if (center == null || center.getWorld() == null) return;

        int size = plugin.getConfigManager().getObsidianCageSize();
        int h = size / 2;
        cageBlocks.clear();

        for (int x = -h; x <= h; x++) {
            for (int z = -h; z <= h; z++) {
                if (x == -h || x == h || z == -h || z == h) {
                    for (int y = -3; y <= size; y++) cageBlocks.add(center.clone().add(x, y, z));
                }
            }
        }
        for (int x = -h; x <= h; x++) {
            for (int z = -h; z <= h; z++) {
                cageBlocks.add(center.clone().add(x, -3, z));
                cageBlocks.add(center.clone().add(x, size, z));
            }
        }
    }

    private void createObsidianCage(Ritual ritual) {
        Material mat = Material.matchMaterial(plugin.getConfigManager().getObsidianCageMaterial().toUpperCase());
        if (mat == null) mat = Material.OBSIDIAN;
        final Material m = mat;

        gradualCageTask = new BukkitRunnable() {
            private final Iterator<Location> it = cageBlocks.iterator();

            @Override
            public void run() {
                int perTick = plugin.getConfigManager().getObsidianCageBlocksPerTick();
                for (int i = 0; i < perTick && it.hasNext(); i++) {
                    Location loc = it.next();
                    Block b = loc.getBlock();
                    originalBlocks.put(loc.clone(), b.getBlockData().clone());
                    b.setType(m);
                }
                if (!it.hasNext()) {
                    cancel();
                    startPeriodicRepair(ritual, m);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void startPeriodicRepair(Ritual ritual, Material m) {
        cageTask = new BukkitRunnable() {
            private Iterator<Location> it = cageBlocks.iterator();

            @Override
            public void run() {
                if (!cageActive || ritual.isCompleted() || ritual.isCancelled()) {
                    cancel();
                    return;
                }
                for (int i = 0; i < 5; i++) {
                    if (!it.hasNext()) it = cageBlocks.iterator();
                    Location loc = it.next();
                    if (loc.getBlock().getType() != m) loc.getBlock().setType(m);
                }
            }
        }.runTaskTimer(plugin, 20L, 5L);
    }

    public void shutdown() { stopCage(); }
    public boolean isCageActive() { return cageActive; }
    public boolean isCageBlock(Location l) { return cageActive && cageBlocks.contains(l); }
}
