package com.zpedroo.voltzevents.tasks;

import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.types.Event;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AnnounceTask extends BukkitRunnable {

    private final Plugin plugin;
    private final Event event;
    private final int delayBetweenMessagesInSeconds;
    private final int maxAnnouncesAmount;
    private int announcesAmount;
    private int countdown;
    private boolean runningTask = false;

    public AnnounceTask(Plugin plugin, Event event, int delayBetweenMessagesInSeconds, int maxAnnouncesAmount) {
        this.plugin = plugin;
        this.event = event;
        this.delayBetweenMessagesInSeconds = delayBetweenMessagesInSeconds;
        this.maxAnnouncesAmount = maxAnnouncesAmount;
        this.countdown = maxAnnouncesAmount * delayBetweenMessagesInSeconds;
    }

    @Override
    public void run() {
        this.runningTask = true;

        if (!event.isHappening()) {
            this.cancelTask();
            return;
        }

        if (countdown % delayBetweenMessagesInSeconds == 0) {
            ++announcesAmount;
            for (String msg : event.getMessage("STARTING")) {
                Bukkit.broadcastMessage(replacePlaceholders(msg));
            }
        }

        event.setAllParticipantsLevel(--countdown);

        if (countdown <= 0) {
            this.cancelTask();

            if (event.getPlayersParticipatingAmount() < event.getMinimumPlayers()) {
                for (String msg : event.getMessage("INSUFFICIENT_PLAYERS")) {
                    Bukkit.broadcastMessage(replacePlaceholders(msg));
                }

                event.finishEvent(false);
                return;
            }

            for (String msg : event.getMessage("STARTED")) {
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
        this.runTaskTimerAsynchronously(plugin, 0L, 20L);
    }

    public void cancelTask() {
        if (!runningTask) return;

        this.cancel();
        this.event.setAnnounceTask(new AnnounceTask(plugin, event, delayBetweenMessagesInSeconds, maxAnnouncesAmount));
    }

    private void addParticipationToAllParticipants() {
        event.getPlayersParticipating().forEach(player -> {
            DataManager.getInstance().getPlayerData(player).addParticipation(1);
        });
    }

    private String replacePlaceholders(String str) {
        return StringUtils.replaceEach(str, new String[]{
                "{tag}",
                "{players}",
                "{announces_now}",
                "{announces_amount}"
        }, new String[]{
                event.getWinnerTag(),
                String.valueOf(event.getPlayersParticipatingAmount()),
                String.valueOf(announcesAmount),
                String.valueOf(maxAnnouncesAmount)
        });
    }
}