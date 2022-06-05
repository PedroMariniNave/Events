package com.zpedroo.voltzevents.managers;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class ListenerManager {

    public static void registerListener(Plugin plugin, Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
}