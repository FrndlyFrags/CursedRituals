package com.cursedrituals.ritual;

import com.cursedrituals.cage.ObsidianCageManager;
import com.cursedrituals.location.LocationFinder;
import com.cursedrituals.plugin.CursedRitualsPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class RitualManager {

    private final CursedRitualsPlugin plugin;
    private Ritual activeRitual;
    private BukkitRunnable ritualTask;
    private BukkitRunnable effectsTask;
    private BukkitRunnable lightningTask;
    private BukkitRunnable dayNightTask;
    private boolean dayNightActive = false;

    public RitualManager(CursedRitualsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isActive() {
        return activeRitual != null && !activeRitual.isCompleted() && !activeRitual.isCancelled();
    }

    public Ritual getActiveRitual() {
        return activeRitual;
    }

    public boolean startRitual() {
        if (isActive()) return false;
        if (plugin.getItemPoolManager().isEmpty()) return false;

        ItemStack selectedItem = plugin.getItemPoolManager().selectItem();
        if (selectedItem == null) return false;

        Location location;
        if (plugin.getConfigManager().isRandomLocation()) {
            LocationFinder.LocationSearchSession session = plugin.getLocationFinder().startSearch();
            location = session.findNext(plugin.getConfigManager().getMaxAttempts());
        } else {
            location = plugin.getServer().getWorlds().get(0).getSpawnLocation();
        }

        if (location == null) return false;

        return startRitual(selectedItem, location);
    }

    public boolean startRitual(ItemStack selectedItem, Location location) {
        if (isActive()) return false;

        plugin.getItemPoolManager().reserveItem(selectedItem);

        int duration = plugin.getConfigManager().getRitualDuration();
        int radius = plugin.getConfigManager().getRitualRadius();
        activeRitual = new Ritual(selectedItem, location, duration, radius);

        // Keep the chunk loaded while the ritual is active to prevent lag from repeated loads
        if (location.getWorld() != null) {
            location.getChunk().setForceLoaded(true);
        }

        spawnRewardItem();
        startTasks();
        plugin.getObsidianCageManager().startCage(activeRitual);

        if (plugin.getConfigManager().isAnnounceStart()) {
            plugin.getAnnouncementManager().announceStart(activeRitual);
            plugin.getAnnouncementManager().sendTitleNotification(location, "Ritual Started", 10, 70, 20);
        }

        return true;
    }

    private void spawnRewardItem() {
        Location loc = activeRitual.getCenterLocation().clone().add(0, 1.5, 0);
        World world = loc.getWorld();
        if (world == null) return;

        ItemStack displayItem = activeRitual.getRewardItem().clone();
        Item dropped = world.dropItem(loc, displayItem);
        dropped.setPickupDelay(Integer.MAX_VALUE);
        dropped.setGravity(false);
        dropped.setVelocity(dropped.getVelocity().zero());
        dropped.setGlowing(plugin.getConfigManager().isGlowEffect());
        dropped.setInvulnerable(true);
        dropped.setMetadata("ritual_item", new FixedMetadataValue(plugin, true));
        dropped.setMetadata("ritual_reward", new FixedMetadataValue(plugin, true));
        dropped.addScoreboardTag("ritual_item");
        dropped.addScoreboardTag("ritual_reward");

        if (plugin.getConfigManager().isPreventDespawn()) {
            dropped.setUnlimitedLifetime(true);
        }

        activeRitual.setDroppedItem(dropped);
    }

    private void startTasks() {
        // Main ritual timer
        ritualTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (activeRitual == null) {
                    cancel();
                    return;
                }

                activeRitual.updatePhase();
                long remaining = activeRitual.getRemainingTime();

                if (remaining <= 0) {
                    completeRitual();
                    cancel();
                    return;
                }

                // Check for warning announcements
                double progress = activeRitual.getProgress();
                int expectedWarnings = (int) (progress * plugin.getConfigManager().getWarningCount());
                if (expectedWarnings > activeRitual.getWarningsSent() && plugin.getConfigManager().isAnnounceWarning()) {
                    activeRitual.incrementWarnings();
                    plugin.getAnnouncementManager().announceWarning(activeRitual);
                }

                // Keep item floating at center
                if (activeRitual.getDroppedItem() != null && activeRitual.getDroppedItem().isValid()) {
                    Location center = activeRitual.getCenterLocation().clone().add(0, 1.5, 0);
                    activeRitual.getDroppedItem().teleport(center);
                }
            }
        };
        ritualTask.runTaskTimer(plugin, 20L, 20L);

        // Effects task
        effectsTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (activeRitual == null || activeRitual.isCompleted() || activeRitual.isCancelled()) {
                    cancel();
                    return;
                }
                plugin.getEffectsManager().playEffects(activeRitual);
            }
        };
        effectsTask.runTaskTimer(plugin, 0L, plugin.getConfigManager().getParticleInterval());

        // Lightning task
        if (plugin.getConfigManager().isLightningEnabled()) {
            lightningTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (activeRitual == null || activeRitual.isCompleted() || activeRitual.isCancelled()) {
                        cancel();
                        return;
                    }
                    plugin.getEffectsManager().playLightning(activeRitual);
                }
            };
            long interval = calculateLightningInterval();
            lightningTask.runTaskTimer(plugin, interval * 20L, interval * 20L);
        }

        // BossBar
        if (plugin.getConfigManager().isBossBarEnabled()) {
            plugin.getBossBarManager().createBossBar(activeRitual);
        }
    }

    private long calculateLightningInterval() {
        if (!plugin.getConfigManager().isLightningIntensityScaling()) {
            return plugin.getConfigManager().getLightningBaseInterval();
        }
        double progress = activeRitual.getProgress();
        int base = plugin.getConfigManager().getLightningBaseInterval();
        int min = plugin.getConfigManager().getLightningMinInterval();
        return Math.max(min, (long) (base * (1.0 - progress * 0.8)));
    }

    public void completeRitual() {
        if (activeRitual == null) return;
        activeRitual.setCompleted(true);

        Location center = activeRitual.getCenterLocation();
        if (center != null && center.getWorld() != null) {
            Item droppedItem = activeRitual.getDroppedItem();
            if (droppedItem != null) {
                // Apply "Cursed " prefix to the reward item upon ritual completion
                ItemStack itemStack = droppedItem.getItemStack();
                org.bukkit.inventory.meta.ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    String existingName = meta.hasDisplayName()
                            ? meta.getDisplayName()
                            : itemStack.getType().name().replace('_', ' ').toLowerCase();
                    meta.setDisplayName("§c☠ Cursed " + existingName);
                    itemStack.setItemMeta(meta);
                    droppedItem.setItemStack(itemStack);
                }

                droppedItem.setPickupDelay(0);
                droppedItem.setGravity(true);
                droppedItem.setInvulnerable(false);
                droppedItem.setGlowing(false);
                droppedItem.removeMetadata("ritual_reward", plugin);
            }
        }

        if (plugin.getConfigManager().isRemoveOnCompletion()) {
            plugin.getItemPoolManager().removeReservedItem(activeRitual.getRewardItem());
        } else {
            plugin.getItemPoolManager().unreserveItem(activeRitual.getRewardItem());
        }

        if (plugin.getConfigManager().isAnnounceComplete()) {
            plugin.getAnnouncementManager().announceComplete(activeRitual);
        }

        if (plugin.getConfigManager().isSoundsEnabled()) {
            plugin.getEffectsManager().playCompletionSound(activeRitual.getCenterLocation());
        }

        plugin.getEffectsManager().playCompletionEffects(activeRitual);
        plugin.getBossBarManager().removeBossBar();
        plugin.getObsidianCageManager().stopCage();
        restoreWorldTime();

        final Ritual completedRitual = activeRitual;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeRitual == completedRitual) {
                    cleanup();
                }
            }
        }.runTaskLater(plugin, 200L);
    }

    public void stopRitual(boolean cancelled) {
        if (activeRitual == null) return;
        activeRitual.setCancelled(true);

        Location center = activeRitual.getCenterLocation();
        if (center != null && center.getWorld() != null) {
            Item droppedItem = activeRitual.getDroppedItem();
            if (droppedItem != null) {
                droppedItem.remove();
            }
        }

        if (cancelled && plugin.getConfigManager().isReturnIfCancelled()) {
            plugin.getItemPoolManager().unreserveItem(activeRitual.getRewardItem());
        } else if (cancelled) {
            plugin.getItemPoolManager().removeReservedItem(activeRitual.getRewardItem());
        }

        if (plugin.getConfigManager().isAnnounceCancel()) {
            plugin.getAnnouncementManager().announceCancel(activeRitual);
        }

        if (plugin.getConfigManager().isSoundsEnabled()) {
            plugin.getEffectsManager().playCancellationSound(activeRitual.getCenterLocation());
        }

        plugin.getBossBarManager().removeBossBar();
        plugin.getObsidianCageManager().stopCage();
        restoreWorldTime();
        cleanup();
    }

    private void restoreWorldTime() {
        if (activeRitual != null) {
            World world = activeRitual.getCenterLocation().getWorld();
            if (world != null) {
                world.setTime(6000L);
            }
        }
        dayNightActive = false;
    }

    private void cleanup() {
        if (activeRitual != null) {
            Location loc = activeRitual.getCenterLocation();
            if (loc != null && loc.getWorld() != null) {
                loc.getChunk().setForceLoaded(false);
            }
        }
        if (ritualTask != null) { ritualTask.cancel(); ritualTask = null; }
        if (effectsTask != null) { effectsTask.cancel(); effectsTask = null; }
        if (lightningTask != null) { lightningTask.cancel(); lightningTask = null; }
        if (dayNightTask != null) { dayNightTask.cancel(); dayNightTask = null; }
        dayNightActive = false;
        activeRitual = null;
    }
}
