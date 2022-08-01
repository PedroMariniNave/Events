package com.zpedroo.scoreboard.utils.placeholderapi;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderUtils {

    public static String replace(Player player, String text) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) return text;

        return PlaceholderAPI.setPlaceholders(player, text);
    }
}