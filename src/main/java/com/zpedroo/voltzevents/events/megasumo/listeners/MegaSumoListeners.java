package com.zpedroo.voltzevents.events.megasumo.listeners;

import com.zpedroo.voltzevents.events.megasumo.MegaSumoEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class MegaSumoListeners implements Listener {

    private final MegaSumoEvent megaSumoEvent;

    public MegaSumoListeners(MegaSumoEvent megaSumoEvent) {
        this.megaSumoEvent = megaSumoEvent;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!megaSumoEvent.isParticipating(player)) return;

        event.setDamage(0);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!megaSumoEvent.isParticipating(player) || !megaSumoEvent.isStarted()) return;

        Player damager = (Player) event.getDamager();
        megaSumoEvent.getEventData().setPlayerLastDamager(player, damager);
    }
}