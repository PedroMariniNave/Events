package com.zpedroo.voltzevents.tasks;

import com.zpedroo.voltzevents.managers.DataManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import static com.zpedroo.voltzevents.utils.config.Settings.SAVE_INTERVAL;

public class SaveTask extends BukkitRunnable {

    public SaveTask(Plugin plugin) {
        this.runTaskTimerAsynchronously(plugin, SAVE_INTERVAL * 20L, SAVE_INTERVAL * 20L);
    }

    @Override
    public void run() {
        DataManager.getInstance().saveAllPlayersData();
        DataManager.getInstance().updateTopWins();
    }
}