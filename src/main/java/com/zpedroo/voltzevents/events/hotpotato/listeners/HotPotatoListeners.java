package com.zpedroo.voltzevents.events.hotpotato.listeners;

import com.zpedroo.voltzevents.events.hotpotato.HotPotatoEvent;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class HotPotatoListeners implements Listener {

    private final HotPotatoEvent hotPotatoEvent;

    public HotPotatoListeners(HotPotatoEvent hotPotatoEvent) {
        this.hotPotatoEvent = hotPotatoEvent;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player damaged = (Player) event.getEntity();
        if (!hotPotatoEvent.isHappening() || !hotPotatoEvent.isParticipating(damaged)) return;
        if (!(event.getDamager() instanceof Player)) return;
        
        Player damager = (Player) event.getDamager();
        if (!hotPotatoEvent.isHotPotato(damager) || hotPotatoEvent.isHotPotato(damaged)) return;

        ItemStack item = damager.getItemInHand();
        if (item == null || item.getType().equals(Material.AIR)) return;

        NBTItem nbt = new NBTItem(item);
        if (!nbt.hasKey("HOT_POTATO")) return;

        hotPotatoEvent.transferHotPotato(damager, damaged);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!hotPotatoEvent.isParticipating(player)) return;

        event.setDamage(0);
    }
}