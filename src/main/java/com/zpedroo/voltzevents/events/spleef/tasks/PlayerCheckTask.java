package com.zpedroo.voltzevents.events.spleef.tasks;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.enums.LeaveReason;
import com.zpedroo.voltzevents.events.spleef.SpleefEvent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerCheckTask extends BukkitRunnable {

    private final SpleefEvent spleefEvent = SpleefEvent.getInstance();
    private final Player player;

    public PlayerCheckTask(Player player) {
        this.player = player;
        this.runTaskTimer(VoltzEvents.get(), 1L, 1L);
    }

    @Override
    public void run() {
        if (!spleefEvent.isHappening() || !spleefEvent.isParticipating(player)) {
            this.cancel();
            return;
        }

        if (player == null || !player.isOnline() || player.getLocation().getBlock().isLiquid()) {
            this.cancel();
            this.eliminatePlayer();
        }
    }

    private void eliminatePlayer() {
        String title = SpleefEvent.Titles.ELIMINATED[0];
        String subtitle = SpleefEvent.Titles.ELIMINATED[1];

        player.sendTitle(title, subtitle);

        spleefEvent.leave(player, LeaveReason.ELIMINATED);
    }
}