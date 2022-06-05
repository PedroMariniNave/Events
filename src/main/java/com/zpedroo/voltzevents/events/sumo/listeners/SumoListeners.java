package com.zpedroo.voltzevents.events.sumo.listeners;

import com.zpedroo.voltzevents.events.sumo.SumoEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class SumoListeners implements Listener {

    private final SumoEvent sumoEvent = SumoEvent.getInstance();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!sumoEvent.isHappening() || !sumoEvent.isParticipating(player)) return;
        if (sumoEvent.isFighting(player)) {
            event.setDamage(0);
        } else {
            event.setCancelled(true);
        }
    }
}