package com.zpedroo.voltzevents.tasks;

import com.zpedroo.voltzevents.managers.DataManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import static com.zpedroo.voltzevents.utils.config.Settings.DATA_UPDATE_INTERVAL;

public class DataUpdateTask extends BukkitRunnable {

    public DataUpdateTask(Plugin plugin) {
        this.runTaskTimerAsynchronously(plugin, DATA_UPDATE_INTERVAL * 20L, DATA_UPDATE_INTERVAL * 20L);
    }

    @Override
    public void run() {
        DataManager.getInstance().saveAllPlayersData();
        DataManager.getInstance().updateTopWins();
    }
}