package com.zpedroo.voltzevents.events.paintball.listeners;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.enums.EventPhase;
import com.zpedroo.voltzevents.enums.LeaveReason;
import com.zpedroo.voltzevents.events.paintball.PaintballEvent;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class PaintballListeners implements Listener {

    private final PaintballEvent paintballEvent = PaintballEvent.getInstance();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        if (!paintballEvent.isHappening() || !paintballEvent.isParticipating(event.getEntity())) return;

        event.getDrops().clear();

        Player player = event.getEntity();
        Player killer = player.getKiller();
        if (killer == null) return;

        player.sendTitle(PaintballEvent.Titles.ELIMINATED[0], PaintballEvent.Titles.ELIMINATED[1]);

        killer.setHealth(killer.getMaxHealth());
        paintballEvent.getEventData().addPlayerKills(killer, 1);
        paintballEvent.leave(player, LeaveReason.ELIMINATED);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!paintballEvent.isHappening() || !paintballEvent.isParticipating(player)) return;
        if (paintballEvent.getEventPhase() != EventPhase.STARTED || !event.getDamager().hasMetadata("Paintball")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPaintballDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager().hasMetadata("Paintball")) || !(event.getEntity() instanceof Player)) return;

        event.setDamage(PaintballEvent.Settings.SHOT_DAMAGE);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (!paintballEvent.isHappening() || !paintballEvent.isParticipating(event.getPlayer())) return;
        if (paintballEvent.getEventPhase() != EventPhase.STARTED) return;
        if (event.getItem() == null || event.getItem().getType().equals(Material.AIR)) return;

        ItemStack item = event.getItem().clone();
        NBTItem nbt = new NBTItem(item);
        if (!nbt.hasKey("PaintballAmmo") || nbt.hasKey("Reloading")) return;

        Player player = event.getPlayer();
        int paintballAmmo = nbt.getInteger("PaintballAmmo");

        switch (event.getAction()) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                if (paintballAmmo >= PaintballEvent.Settings.MAX_AMMO_AMOUNT) return;

                reloadAmmo(player, item);
                break;
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                if (paintballAmmo <= 0) {
                    reloadAmmo(player, item);
                    return;
                }

                shoot(player, item);
                break;
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        if (!paintballEvent.isStarted() || !paintballEvent.isParticipating(event.getPlayer())) return;

        Player player = event.getPlayer();
        player.damage(9999);
    }

    private void reloadAmmo(Player player, ItemStack item) {
        int slot = player.getInventory().first(item);
        if (slot == -1) return;

        NBTItem nbt = new NBTItem(item);
        int ammoAmount = nbt.getInteger("PaintballAmmo");

        ItemStack reloadingGunItem = paintballEvent.getReloadingGunItem(ammoAmount);
        player.getInventory().setItem(slot, reloadingGunItem);
        player.getWorld().playSound(player.getLocation(), Sound.CLICK, 0.5f, 2f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                int slot = player.getInventory().first(reloadingGunItem);
                if (slot == -1) return;

                player.getInventory().setItem(slot, paintballEvent.getGunItem(PaintballEvent.Settings.MAX_AMMO_AMOUNT));
                player.getWorld().playSound(player.getLocation(), Sound.HORSE_ARMOR, 0.2f, 4f);
            }
        }.runTaskLater(VoltzEvents.get(), 20L * PaintballEvent.Settings.RELOADING_TIME);
    }

    private void shoot(Player player, ItemStack item) {
        NBTItem nbt = new NBTItem(item);
        int ammoAmount = nbt.getInteger("PaintballAmmo");
        int newPaintballAmmo = ammoAmount - 1;
        Snowball snowball = player.launchProjectile(Snowball.class);
        snowball.setMetadata("Paintball", new FixedMetadataValue(VoltzEvents.get(), true));
        player.getWorld().playSound(player.getLocation(), Sound.BLAZE_HIT, 0.2f, 15f);

        ItemStack newGunItem = paintballEvent.getGunItem(newPaintballAmmo);
        player.setItemInHand(newGunItem);

        if (newPaintballAmmo <= 0) {
            reloadAmmo(player, newGunItem);
        }
    }
}