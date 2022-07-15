package com.zpedroo.voltzevents.events.megasumo.tasks;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.enums.LeaveReason;
import com.zpedroo.voltzevents.events.megasumo.MegaSumoEvent;
import com.zpedroo.voltzevents.events.spleef.SpleefEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerCheckTask extends BukkitRunnable {

    private final MegaSumoEvent megaSumoEvent;
    private final Player player;

    public PlayerCheckTask(MegaSumoEvent megaSumoEvent, Player player) {
        this.megaSumoEvent = megaSumoEvent;
        this.player = player;
        this.runTaskTimer(VoltzEvents.get(), 1L, 1L);
    }

    @Override
    public void run() {
        if (player == null || !player.isOnline() || !megaSumoEvent.isParticipating(player)) {
            this.cancel();
            return;
        }

        if (player.getLocation().getBlock().isLiquid()) {
            if (megaSumoEvent.isStarted()) {
                this.eliminatePlayer();
                return;
            }

            if (megaSumoEvent.isWarmup()) {
                player.teleport(megaSumoEvent.getArenaLocation());
            } else if (megaSumoEvent.isWaitingPlayers()) {
                player.teleport(megaSumoEvent.getJoinLocation());
            }
        }
    }

    private void eliminatePlayer() {
        String title = SpleefEvent.Titles.ELIMINATED[0];
        String subtitle = SpleefEvent.Titles.ELIMINATED[1];

        player.sendTitle(title, subtitle);

        megaSumoEvent.leave(player, LeaveReason.ELIMINATED);

        Entity lastDamager = player.getLastDamageCause().getEntity();
        if (!(lastDamager instanceof Player)) return;

        megaSumoEvent.getEventData().addPlayerKills((Player) lastDamager, 1);
    }
}