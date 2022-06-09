package com.zpedroo.voltzevents.events.hotpotato.tasks;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.events.hotpotato.HotPotatoEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerFinderTask extends BukkitRunnable {

    private final Plugin plugin = VoltzEvents.get();
    private final HotPotatoEvent event;

    public PlayerFinderTask(HotPotatoEvent event) {
        this.event = event;
    }

    @Override
    public void run() {
        if (!event.isHappening()) {
            this.cancel();
            return;
        }

        event.getHotPotatoesNames().forEach(name -> {
            Player player = Bukkit.getPlayer(name);
            if (player == null) return;

            Player nearestPlayer = event.getNearestUntagged(player);
            if (nearestPlayer == null) return;

            player.setCompassTarget(nearestPlayer.getLocation());
        });
    }

    public void startTask() {
        this.runTaskTimer(plugin, 0L, 20L);
    }
}