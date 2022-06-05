package com.zpedroo.voltzevents.events.spleef.listeners;

import com.zpedroo.voltzevents.events.spleef.SpleefEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class SpleefListeners implements Listener {

    private final SpleefEvent spleefEvent = SpleefEvent.getInstance();
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!spleefEvent.isHappening() || !spleefEvent.isParticipating(player)) return;
        if (spleefEvent.getPlayer1() == null || spleefEvent.getPlayer2() == null ||
                (!spleefEvent.getPlayer1().getUniqueId().equals(player.getUniqueId())
                        && !spleefEvent.getPlayer2().getUniqueId().equals(player.getUniqueId()))) event.setCancelled(true);
    }
}