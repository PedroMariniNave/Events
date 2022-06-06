package com.zpedroo.voltzevents.events.hotpotato.listeners;

import com.zpedroo.voltzevents.events.hotpotato.HotPotatoEvent;
import com.zpedroo.voltzevents.enums.EventPhase;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class HotPotatoListeners implements Listener {

    private final HotPotatoEvent hotPotatoEvent = HotPotatoEvent.getInstance();
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player damaged = (Player) event.getEntity();
        if (!hotPotatoEvent.isHappening() || !hotPotatoEvent.isParticipating(damaged)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK || hotPotatoEvent.getEventPhase() != EventPhase.STARTED) {
            event.setCancelled(true);
        } else {
            event.setDamage(0);
        }

        if (!(event.getDamager() instanceof Player)) return;
        
        Player damager = (Player) event.getDamager();
        if (!hotPotatoEvent.isHotPotato(damager)) return;

        ItemStack item = damager.getItemInHand();
        if (item == null || !item.isSimilar(HotPotatoEvent.getInstance().getHotPotatoItem())) return;

        hotPotatoEvent.transferHotPotato(damager, damaged);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().isSimilar(HotPotatoEvent.getInstance().getHotPotatoItem())) event.setCancelled(true);
    }
}