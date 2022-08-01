package com.zpedroo.voltzevents.tasks.event;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.types.ArenaEvent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class WinnerRegionCheckTask extends BukkitRunnable {

    private final ArenaEvent event;
    private final Player player;

    public WinnerRegionCheckTask(ArenaEvent event, Player player) {
        this.event = event;
        this.player = player;
        this.runTaskTimer(VoltzEvents.get(), 1L, 1L);
    }

    @Override
    public void run() {
        if (player == null || !player.isOnline() || !event.isHappening() || !event.isParticipating(player) || event.getWinRegion() == null) {
            this.cancel();
            return;
        }

        if (event.getWinRegion().isOnRegion(player)) {
            event.checkIfPlayerIsWinner(player, event.getPlayersParticipatingAmount());
        }
    }
}