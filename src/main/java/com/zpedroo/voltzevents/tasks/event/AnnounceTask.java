package com.zpedroo.voltzevents.tasks.event;

import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.types.Event;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class AnnounceTask extends BukkitRunnable {

    private final Plugin plugin;
    private final Event event;
    private final int delayBetweenMessagesInSeconds;
    private final int maxAnnouncesAmount;
    private int announcesAmount;
    private int countdown;
    private boolean isRunningTask = false;

    public AnnounceTask(Plugin plugin, Event event, int delayBetweenMessagesInSeconds, int maxAnnouncesAmount) {
        this.plugin = plugin;
        this.event = event;
        this.delayBetweenMessagesInSeconds = delayBetweenMessagesInSeconds;
        this.maxAnnouncesAmount = maxAnnouncesAmount;
        this.countdown = maxAnnouncesAmount * delayBetweenMessagesInSeconds;
    }

    @Override
    public void run() {
        this.isRunningTask = true;

        if (!event.isHappening()) {
            this.cancelTask();
            return;
        }

        if (countdown % delayBetweenMessagesInSeconds == 0) {
            ++announcesAmount;
            List<String> messagesToSend = event.isHosted() ? event.getMessages("STARTING_HOSTED") : event.getMessages("STARTING");
            for (String msg : messagesToSend) {
                Bukkit.broadcastMessage(replacePlaceholders(msg));
            }
        }

        --countdown;

        if (event.isSavePlayerInventory()) { // player exp saved
            event.setAllParticipantsLevel(countdown);
        }

        if (countdown <= 0) {
            this.cancelTask();

            if (event.getPlayersParticipatingAmount() < event.getMinimumPlayersToStart()) {
                for (String msg : event.getMessages("INSUFFICIENT_PLAYERS")) {
                    Bukkit.broadcastMessage(replacePlaceholders(msg));
                }

                event.cancelEvent(false);
                return;
            }

            List<String> messagesToSend = event.isHosted() ? event.getMessages("STARTED_HOSTED") : event.getMessages("STARTED");
            for (String msg : messagesToSend) {
                Bukkit.broadcastMessage(replacePlaceholders(msg));
            }

            event.getEventData().setStartTimestamp(System.currentTimeMillis());
            event.teleportPlayersToArenaAndExecuteEventActions();
            event.startEventMethods();
            addParticipationToAllParticipants();
            return;
        }

        if (DataManager.getInstance().getCache().getCountdownTitles().containsKey(countdown)) {
            String[] countdownTitles = DataManager.getInstance().getCache().getCountdownTitles().get(countdown);

            String title = countdownTitles[0];
            String subtitle = countdownTitles[1];

            event.sendTitleToAllParticipants(title, subtitle);
            event.playSoundToAllParticipants(Sound.NOTE_STICKS, 0.75f, 1f);
        }
    }

    public int getCountdown() {
        return countdown;
    }

    public void startTask() {
        if (isRunningTask) { // already started, let's create a new instance to fix some runnable issues
            AnnounceTask announceTask = new AnnounceTask(plugin, event, delayBetweenMessagesInSeconds, maxAnnouncesAmount);
            announceTask.startTask();
            event.setAnnounceTask(announceTask);
            return;
        }

        this.runTaskTimer(plugin, 0L, 20L);
    }

    public void cancelTask() {
        if (!isRunningTask) return;

        this.cancel();
    }

    private void addParticipationToAllParticipants() {
        event.getPlayersParticipating().forEach(player -> {
            DataManager.getInstance().getPlayerData(player).addParticipation(1);
        });
    }

    private String replacePlaceholders(String str) {
        return StringUtils.replaceEach(str, new String[]{
                "{host}",
                "{host_rewards}",
                "{tag}",
                "{players}",
                "{announces_now}",
                "{announces_amount}"
        }, new String[]{
                event.isHosted() ? event.getEventHost().getHostPlayerName() : "-/-",
                event.getTotalHostRewardsDisplay(),
                event.getWinnerTag(),
                String.valueOf(event.getPlayersParticipatingAmount()),
                String.valueOf(announcesAmount),
                String.valueOf(maxAnnouncesAmount)
        });
    }
}