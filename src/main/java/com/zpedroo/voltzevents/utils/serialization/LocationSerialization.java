package com.zpedroo.voltzevents.utils.serialization;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationSerialization {

    public static String serialize(Location location) {
        if (location == null) return "";

        StringBuilder serialized = new StringBuilder(6);
        serialized.append(location.getWorld().getName());
        serialized.append("#" + location.getX());
        serialized.append("#" + location.getY());
        serialized.append("#" + location.getZ());
        serialized.append("#" + location.getYaw());
        serialized.append("#" + location.getPitch());

        return serialized.toString();
    }

    public static Location deserialize(String serialized) {
        if (serialized == null || serialized.isEmpty()) return null;

        String[] locationSplit = serialized.split("#");
        if (locationSplit.length < 6) return null;

        World world = Bukkit.getWorld(locationSplit[0]);
        double x = Double.parseDouble(locationSplit[1]);
        double y = Double.parseDouble(locationSplit[2]);
        double z = Double.parseDouble(locationSplit[3]);
        float yaw = Float.parseFloat(locationSplit[4]);
        float pitch = Float.parseFloat(locationSplit[5]);

        return new Location(world, x, y, z, yaw, pitch);
    }
}