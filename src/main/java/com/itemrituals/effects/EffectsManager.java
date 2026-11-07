package com.itemrituals.effects;

import com.itemrituals.config.ConfigManager;
import com.itemrituals.plugin.ItemRitualsPlugin;
import com.itemrituals.ritual.Ritual;
import com.itemrituals.ritual.RitualPhase;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class EffectsManager {

    private final ItemRitualsPlugin plugin;
    private final Random random;
    private final java.util.Map<Location, Double> groundLevelCache = new java.util.HashMap<>();
    private Ritual lastRitual;

    public EffectsManager(ItemRitualsPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    public void playEffects(Ritual ritual) {
        if (!plugin.getConfigManager().isParticlesEnabled())
            return;

        if (lastRitual != ritual) {
            groundLevelCache.clear();
            lastRitual = ritual;
        }

        Location center = ritual.getCenterLocation();
        World world = center.getWorld();
        if (world == null)
            return;

        double progress = ritual.getProgress();
        int radius = ritual.getRadius();

        // Check if ritual is in the final phase (last 25% of duration)
        boolean isFinalPhase = progress >= 0.75;

        // 1. Draw a beautiful, ground-conforming outer boundary ring
        if (!isFinalPhase) {
            double ringSpeedFactor = 1.0 + progress * 0.5;
            double ringTime = (System.currentTimeMillis() / 1000.0) * ringSpeedFactor;
            int boundaryPoints = 16; // Further reduced for performance
            int centerBlockY = center.getBlockY();
            for (int i = 0; i < boundaryPoints; i++) {
                double angle = (2.0 * Math.PI / boundaryPoints) * i + ringTime;
                double x = center.getX() + Math.cos(angle) * radius;
                double z = center.getZ() + Math.sin(angle) * radius;

                // Conforming to terrain with caching
                Location cacheKey = new Location(world, Math.floor(x), 0, Math.floor(z));
                double y;
                if (groundLevelCache.containsKey(cacheKey)) {
                    y = groundLevelCache.get(cacheKey);
                } else {
                    y = center.getY();
                    int intX = cacheKey.getBlockX();
                    int intZ = cacheKey.getBlockZ();
                    for (int offset = 3; offset >= -3; offset--) {
                        if (world.getBlockAt(intX, centerBlockY + offset, intZ).getType().isSolid()) {
                            y = centerBlockY + offset + 1.1;
                            break;
                        }
                    }
                    groundLevelCache.put(cacheKey, y);
                }
                world.spawnParticle(Particle.REDSTONE, x, y, z, 1, 0, 0, 0, 0, new Particle.DustOptions(org.bukkit.Color.PURPLE, 1.0f));
            }
        }

        // 2. Central double-helix vortex around the floating item (optimized - fewer steps)
        if (!isFinalPhase) {
            double vortexSpeedFactor = 1.0 + progress * 1.5;
            double vortexTime = (System.currentTimeMillis() / 200.0) * vortexSpeedFactor;
            int vortexSteps = 10; // Reduced from 15 for performance
            for (int hIndex = 0; hIndex < vortexSteps; hIndex++) {
                double h = (3.0 / vortexSteps) * hIndex;
                double angle = h * 2.0 + vortexTime;
                double r = 0.3 + (3.0 - h) * 0.15; // Narrows towards the top where the item floats
                double x1 = center.getX() + Math.cos(angle) * r;
                double z1 = center.getZ() + Math.sin(angle) * r;
                double x2 = center.getX() - Math.cos(angle) * r;
                double z2 = center.getZ() - Math.sin(angle) * r;

                world.spawnParticle(Particle.PORTAL, x1, center.getY() + h, z1, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.PORTAL, x2, center.getY() + h, z2, 1, 0, 0, 0, 0);

                if (hIndex % 4 == 0) { // Changed from % 3 to % 4 for less frequent spell particles
                    world.spawnParticle(Particle.SPELL_WITCH, x1, center.getY() + h, z1, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.SPELL_WITCH, x2, center.getY() + h, z2, 1, 0, 0, 0, 0);
                }
            }
        }

        // 3. Ambient particles inside the ritual area (optimized)
        List<String> particleTypes = plugin.getConfigManager().getParticleTypes();
        if (!particleTypes.isEmpty()) {
            int particleCount = plugin.getConfigManager().isParticleIntensityScaling() ? (int) (3 + progress * 10) : 8; // Reduced from 5+15 to 3+10 for performance

            for (int i = 0; i < particleCount; i++) {
                String typeName = particleTypes.get(random.nextInt(particleTypes.size()));
                try {
                    Particle particle = Particle.valueOf(typeName.toUpperCase());
                    double angle = random.nextDouble() * 2 * Math.PI;
                    double distance = random.nextDouble() * radius;
                    double x = center.getX() + Math.cos(angle) * distance;
                    double z = center.getZ() + Math.sin(angle) * distance;
                    double y = center.getY() + random.nextDouble() * 3;

                    world.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        // 4. Dangerous fire ring at 2-block radius around the item — burns players
        // inside (optimized)
        Location itemLoc = center.clone().add(0, 1.5, 0);
        int firePoints = 16; // Reduced from 24 for performance
        double fireRadius = 2.0;
        double fireTime = (System.currentTimeMillis() / 150.0);
        for (int i = 0; i < firePoints; i++) {
            double angle = (2.0 * Math.PI / firePoints) * i + fireTime;
            double fx = itemLoc.getX() + Math.cos(angle) * fireRadius;
            double fz = itemLoc.getZ() + Math.sin(angle) * fireRadius;
            world.spawnParticle(Particle.FLAME, fx, itemLoc.getY(), fz, 2, 0, 0.1, 0, 0.01);
            world.spawnParticle(Particle.LAVA, fx, itemLoc.getY() - 0.5, fz, 1, 0, 0, 0, 0);
        }

        // Burn any player who steps inside the 2-block fire ring (throttled to reduce server lag)
        // Check every 20 ticks (1 second) instead of every particle tick
        if (System.currentTimeMillis() % 1000 < 20) { // Only check once per second
            Collection<Entity> nearby = world.getNearbyEntities(itemLoc, fireRadius, 2.5, fireRadius);
            for (Entity entity : nearby) {
                if (entity instanceof Player player) {
                    if (player.getFireTicks() <= 0) {
                        player.setFireTicks(60); // 3 seconds of fire
                    }
                }
            }
        }

        // Ambient sounds (reduced frequency)
        if (plugin.getConfigManager().isSoundsEnabled() && random.nextInt(100) < 5) { // Reduced from 10 to 5
            ConfigManager.SoundData ambient = plugin.getConfigManager().getAmbientSound();
            if (ambient != null) {
                world.playSound(center, ambient.sound(), ambient.volume(), ambient.pitch());
            }
        }
    }

    public void playLightning(Ritual ritual) {
        if (!plugin.getConfigManager().isLightningEnabled())
            return;

        Location center = ritual.getCenterLocation();
        World world = center.getWorld();
        if (world == null)
            return;

        double progress = ritual.getProgress();
        int radius = ritual.getRadius();

        // Strike near the ritual area
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = random.nextDouble() * radius * 0.8;
        double x = center.getX() + Math.cos(angle) * distance;
        double z = center.getZ() + Math.sin(angle) * distance;
        Location strikeLoc = new Location(world, x, center.getY(), z);

        if (plugin.getConfigManager().isLightningVisualOnly()) {
            world.strikeLightningEffect(strikeLoc);
        } else {
            world.strikeLightning(strikeLoc);
        }

        if (plugin.getConfigManager().isSoundsEnabled()) {
            ConfigManager.SoundData lightning = plugin.getConfigManager().getLightningSound();
            if (lightning != null) {
                world.playSound(strikeLoc, lightning.sound(), lightning.volume(), lightning.pitch());
            }
        }
    }

    public void playAwakeningSound(Location loc) {
        World world = loc.getWorld();
        if (world == null)
            return;
        ConfigManager.SoundData sound = plugin.getConfigManager().getAwakeningSound();
        if (sound != null) {
            world.playSound(loc, sound.sound(), sound.volume(), sound.pitch());
        }
    }

    public void playCompletionSound(Location loc) {
        World world = loc.getWorld();
        if (world == null)
            return;
        ConfigManager.SoundData sound = plugin.getConfigManager().getCompletionSound();
        if (sound != null) {
            world.playSound(loc, sound.sound(), sound.volume(), sound.pitch());
        }
    }

    public void playCancellationSound(Location loc) {
        World world = loc.getWorld();
        if (world == null)
            return;
        ConfigManager.SoundData sound = plugin.getConfigManager().getCancellationSound();
        if (sound != null) {
            world.playSound(loc, sound.sound(), sound.volume(), sound.pitch());
        }
    }

    public void playCompletionEffects(Ritual ritual) {
        Location center = ritual.getCenterLocation();
        World world = center.getWorld();
        if (world == null)
            return;

        // Massive particle burst
        for (int i = 0; i < 100; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = random.nextDouble() * ritual.getRadius();
            double x = center.getX() + Math.cos(angle) * distance;
            double z = center.getZ() + Math.sin(angle) * distance;
            double y = center.getY() + random.nextDouble() * 5;
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, x, y, z, 2, 0.2, 0.2, 0.2, 0.05);
            world.spawnParticle(Particle.LAVA, x, y, z, 1, 0.1, 0.1, 0.1, 0);
        }

        // Lightning burst
        for (int i = 0; i < 5; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double dist = random.nextDouble() * 5;
            Location strike = center.clone().add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
            world.strikeLightningEffect(strike);
        }
    }
}
