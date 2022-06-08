package com.zpedroo.voltzevents.events.fight.listeners;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.enums.EventPhase;
import com.zpedroo.voltzevents.enums.LeaveReason;
import com.zpedroo.voltzevents.events.fight.FightEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class FightListeners implements Listener {

    private final FightEvent fightEvent = FightEvent.getInstance();
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        if (!fightEvent.isHappening() || !fightEvent.isParticipating(event.getEntity())) return;

        Player player = event.getEntity();
        Player killer = player.getKiller();
        if (killer == null) return;

        player.sendTitle(FightEvent.Titles.ELIMINATED[0], FightEvent.Titles.ELIMINATED[1]);

        fightEvent.setEventPhase(EventPhase.WARMUP);
        fightEvent.setPlayer1(null);
        fightEvent.setPlayer2(null);

        killer.getInventory().clear();
        killer.getInventory().setArmorContents(new ItemStack[4]);
        killer.setHealth(killer.getMaxHealth());
        fightEvent.getEventData().addPlayerKills(killer, 1);
        fightEvent.leave(player, LeaveReason.ELIMINATED);

        fightEvent.sendTitleToAllParticipants(FightEvent.Titles.WINNER[0], FightEvent.Titles.WINNER[1], new String[]{
                "{winner}",
                "{loser}"
        }, new String[]{
                killer.getName(),
                player.getName()
        });

        if (fightEvent.isFinished()) return;

        killer.teleport(fightEvent.getJoinLocation());
        fightEvent.setPlayerSpecialItems(killer);
        fightEvent.updateAllParticipantsView();

        new BukkitRunnable() {
            @Override
            public void run() {
                fightEvent.selectPlayersAndExecuteEventActions();
            }
        }.runTaskLaterAsynchronously(VoltzEvents.get(), 20L * FightEvent.Settings.TELEPORT_DELAY);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!fightEvent.isHappening() || !fightEvent.isParticipating(player) || fightEvent.isFighting(player)) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        if (!fightEvent.isHappening() || !fightEvent.isParticipating(event.getPlayer())) return;

        Player player = event.getPlayer();
        if (!fightEvent.isFighting(player)) return;

        Player winner = fightEvent.getPlayer1().equals(player) ? fightEvent.getPlayer2() : fightEvent.getPlayer1();
        player.damage(9999, winner);
    }
}