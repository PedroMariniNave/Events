package com.zpedroo.voltzevents.tasks.event;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.types.ArenaEvent;
import com.zpedroo.voltzevents.types.Event;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class VoidCheckTask extends BukkitRunnable {

    private final Event event;
    private final Player player;

    public VoidCheckTask(Event event, Player player) {
        this.event = event;
        this.player = player;
    }

    @Override
    public void run() {
        if (player == null || !player.isOnline() || !event.isHappening() || !event.isParticipating(player)) {
            this.cancel();
            return;
        }

        if (player.getLocation().getY() <= 0) {
            if (event instanceof ArenaEvent && event.isStarted()) {
                ArenaEvent arenaEvent = (ArenaEvent) event;
                player.teleport(arenaEvent.getArenaLocation());
            } else {
                player.teleport(event.getJoinLocation());
            }
        }
    }

    public void startTask() {
        this.runTaskTimer(VoltzEvents.get(), 0L, 2L);
    }
}