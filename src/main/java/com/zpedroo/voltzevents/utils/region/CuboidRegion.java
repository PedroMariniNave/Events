package com.zpedroo.voltzevents.utils.region;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class CuboidRegion {

    private final World world;
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int minZ;
    private final int maxZ;

    public CuboidRegion(Location loc1, Location loc2) {
        this(loc1.getWorld(), loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ(), loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());
    }

    public CuboidRegion(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.world = world;
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public World getWorld() {
        return world;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public Location getFirstLocation() {
        return new Location(world, minX, minY, minZ);
    }

    public Location getSecondLocation() {
        return new Location(world, maxX, maxY, maxZ);
    }

    public boolean isOnRegion(Player player) {
        return contains(player.getLocation());
    }

    public boolean contains(CuboidRegion cuboidRegion) {
        return cuboidRegion.getWorld().equals(world) &&
                cuboidRegion.getMinX() >= minX && cuboidRegion.getMaxX() <= maxX &&
                cuboidRegion.getMinY() >= minY && cuboidRegion.getMaxY() <= maxY &&
                cuboidRegion.getMinZ() >= minZ && cuboidRegion.getMaxZ() <= maxZ;
    }

    public boolean contains(Location location) {
        return contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CuboidRegion)) return false;

        final CuboidRegion other = (CuboidRegion) obj;
        return world.equals(other.world)
                && minX == other.minX
                && minY == other.minY
                && minZ == other.minZ
                && maxX == other.maxX
                && maxY == other.maxY
                && maxZ == other.maxZ;
    }
}