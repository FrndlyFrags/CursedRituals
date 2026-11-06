package com.itemrituals.location;

import com.itemrituals.plugin.ItemRitualsPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class LocationFinder {

    private final ItemRitualsPlugin plugin;
    private final Random random;

    public LocationFinder(ItemRitualsPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    public LocationSearchSession startSearch() {
        return new LocationSearchSession();
    }

    public class LocationSearchSession {
        private int attempts = 0;
        private final int maxAttempts;
        private final World targetWorld;
        private final Location spawn;
        private final int minDist;
        private final int maxDist;
        private final int minPlayerDist;
        private final int minY;
        private final int maxY;

        public LocationSearchSession() {
            List<World> worlds = plugin.getServer().getWorlds();
            List<String> allowed = plugin.getConfigManager().getAllowedWorlds();
            this.targetWorld = allowed.isEmpty() ? worlds.get(0) : worlds.stream()
                    .filter(w -> allowed.contains(w.getName())).findFirst().orElse(worlds.get(0));
            this.maxAttempts = plugin.getConfigManager().getMaxAttempts();
            this.minDist = plugin.getConfigManager().getMinDistanceFromSpawn();
            this.maxDist = plugin.getConfigManager().getMaxDistanceFromSpawn();
            this.minPlayerDist = plugin.getConfigManager().getMinDistanceFromPlayers();
            this.minY = plugin.getConfigManager().getMinY();
            this.maxY = plugin.getConfigManager().getMaxY();
            this.spawn = targetWorld.getSpawnLocation();
        }

        public Location findNext(int batchSize) {
            WorldBorder border = targetWorld.getWorldBorder();
            Location center = border.getCenter();
            double borderSize = border.getSize();
            // Calculate effective bounds considering world border radius (half size)
            double maxAllowedRadius = (borderSize / 2.0) - 20; // 20 block safety margin inside border

            for (int i = 0; i < batchSize && attempts < maxAttempts; i++) {
                attempts++;
                double angle = random.nextDouble() * 2 * Math.PI;

                // Pick a distance that stays strictly within the border radius and config limits
                double effectiveMax = Math.min(maxDist, maxAllowedRadius);
                double effectiveMin = Math.min(minDist, effectiveMax * 0.8);
                if (effectiveMax <= 10) {
                    effectiveMax = Math.max(5, maxAllowedRadius);
                    effectiveMin = 0;
                }

                double distance = effectiveMin + random.nextDouble() * Math.max(0.1, (effectiveMax - effectiveMin));
                int x = center.getBlockX() + (int) (Math.cos(angle) * distance);
                int z = center.getBlockZ() + (int) (Math.sin(angle) * distance);

                Location testLoc = new Location(targetWorld, x, 100, z);
                if (!border.isInside(testLoc)) {
                    continue;
                }

                int y = findSafeY(targetWorld, x, z, minY, maxY);
                if (y == -1) continue;

                Location loc = new Location(targetWorld, x + 0.5, y + 1, z + 0.5);

                if (!isSafeLocation(loc) || !isFarFromPlayers(loc, minPlayerDist)) continue;
                return loc;
            }

            // Fallback to spawn location if attempts exhausted
            Location spawnLoc = targetWorld.getSpawnLocation();
            if (border.isInside(spawnLoc)) {
                int spawnY = targetWorld.getHighestBlockYAt(spawnLoc);
                return new Location(targetWorld, spawnLoc.getX(), spawnY + 1, spawnLoc.getZ());
            }

            // If spawn is outside border, use border center
            int centerY = targetWorld.getHighestBlockYAt(center);
            return new Location(targetWorld, center.getX(), centerY + 1, center.getZ());
        }

        public boolean isFinished() {
            return attempts >= maxAttempts;
        }
    }

    private int findSafeY(World world, int x, int z, int minY, int maxY) {
        // Use Bukkit's built-in getHighestBlockYAt which finds true surface level instantly without chunk loops!
        int surfaceY = world.getHighestBlockYAt(x, z);

        if (surfaceY < minY || surfaceY > maxY) {
            return -1;
        }

        Block block = world.getBlockAt(x, surfaceY, z);
        Block above = world.getBlockAt(x, surfaceY + 1, z);
        Block above2 = world.getBlockAt(x, surfaceY + 2, z);

        if (block.getType().isSolid() && above.getType().isAir() && above2.getType().isAir()) {
            return surfaceY;
        }

        return -1;
    }

    private boolean isSafeLocation(Location loc) {
        Block block = loc.getBlock();
        Block below = loc.clone().subtract(0, 1, 0).getBlock();

        if (plugin.getConfigManager().isAvoidWater()) {
            if (block.getType() == Material.WATER || below.getType() == Material.WATER) return false;
        }
        if (plugin.getConfigManager().isAvoidLava()) {
            if (block.getType() == Material.LAVA || below.getType() == Material.LAVA) return false;
        }
        if (plugin.getConfigManager().isAvoidVoid()) {
            if (loc.getY() < 0) return false;
        }

        Material belowType = below.getType();
        if (belowType == Material.FIRE || belowType == Material.CACTUS || belowType == Material.SWEET_BERRY_BUSH) {
            return false;
        }

        return below.getType().isSolid();
    }

    private boolean isFarFromPlayers(Location loc, int minDistance) {
        if (minDistance <= 0) return true;
        double minDistSq = (double) minDistance * minDistance;
        for (Player player : loc.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(loc) < minDistSq) {
                return false;
            }
        }
        return true;
    }
}
