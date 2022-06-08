package com.zpedroo.voltzevents.events.sumo.tasks;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.enums.EventPhase;
import com.zpedroo.voltzevents.enums.LeaveReason;
import com.zpedroo.voltzevents.events.sumo.SumoEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerCheckTask extends BukkitRunnable {

    private final SumoEvent sumoEvent = SumoEvent.getInstance();
    private final Player player;

    public PlayerCheckTask(Player player) {
        this.player = player;
        this.runTaskTimerAsynchronously(VoltzEvents.get(), 1L, 1L);
    }

    @Override
    public void run() {
        if (sumoEvent.getPlayer1() == null || sumoEvent.getPlayer2() == null) {
            this.cancel();
            return;
        }

        if (player == null || !player.isOnline() || !sumoEvent.isFighting(player) || player.getLocation().getBlock().isLiquid()) {
            selectWinner();
        }
    }

    private void selectWinner() {
        this.cancel();

        Player winner = sumoEvent.getPlayer1().equals(player) ? sumoEvent.getPlayer2() : sumoEvent.getPlayer1();
        if (winner == null) return;

        player.sendTitle(SumoEvent.Titles.ELIMINATED[0], SumoEvent.Titles.ELIMINATED[1]);

        sumoEvent.setEventPhase(EventPhase.WARMUP);
        sumoEvent.setPlayer1(null);
        sumoEvent.setPlayer2(null);

        winner.getInventory().clear();
        winner.getInventory().setArmorContents(new ItemStack[4]);
        sumoEvent.getEventData().addPlayerKills(winner, 1);
        sumoEvent.leave(player, LeaveReason.ELIMINATED);

        sumoEvent.sendTitleToAllParticipants(SumoEvent.Titles.WINNER[0], SumoEvent.Titles.WINNER[1], new String[]{
                "{winner}",
                "{loser}"
        }, new String[]{
                winner.getName(),
                player.getName()
        });

        if (sumoEvent.isFinished()) return;

        winner.teleport(sumoEvent.getJoinLocation());
        sumoEvent.setPlayerSpecialItems(winner);
        sumoEvent.updateAllParticipantsView();

        new BukkitRunnable() {
            @Override
            public void run() {
                sumoEvent.selectPlayersAndExecuteEventActions();
            }
        }.runTaskLaterAsynchronously(VoltzEvents.get(), 20L * SumoEvent.Settings.TELEPORT_DELAY);
    }
}