package com.zpedroo.voltzevents.events.killer.listeners;

import com.zpedroo.voltzevents.enums.LeaveReason;
import com.zpedroo.voltzevents.events.killer.KillerEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class KillerListeners implements Listener {

    private final KillerEvent killerEvent = KillerEvent.getInstance();
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        if (!killerEvent.isHappening() || !killerEvent.isParticipating(event.getEntity())) return;

        Player player = event.getEntity();
        Player killer = player.getKiller();
        if (killer == null) return;

        player.sendTitle(KillerEvent.Titles.ELIMINATED[0], KillerEvent.Titles.ELIMINATED[1]);

        killerEvent.getEventData().addPlayerKills(killer, 1);
        killerEvent.leave(player, LeaveReason.ELIMINATED, false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        if (!killerEvent.isHappening() || !killerEvent.isParticipating(event.getPlayer())) return;

        Player player = event.getPlayer();
        player.damage(9999);
    }
}