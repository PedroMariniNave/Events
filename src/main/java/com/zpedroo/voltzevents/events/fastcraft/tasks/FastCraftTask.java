package com.zpedroo.voltzevents.events.fastcraft.tasks;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.events.fastcraft.FastCraftEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class FastCraftTask extends BukkitRunnable {

    private final FastCraftEvent event;
    private final int delayBetweenMessagesInSeconds;
    private final int maxAnnouncesAmount;
    private int announcesAmount;

    public FastCraftTask(FastCraftEvent event, int delayBetweenMessagesInSeconds, int maxAnnouncesAmount) {
        this.event = event;
        this.delayBetweenMessagesInSeconds = delayBetweenMessagesInSeconds;
        this.maxAnnouncesAmount = maxAnnouncesAmount;
    }

    @Override
    public void run() {
        if (!event.isHappening()) {
            this.cancel();
            return;
        }

        if (++announcesAmount == 1) {
            List<String> messagesToSend = event.isHosted() ? event.getMessages("STARTED_HOSTED") : event.getMessages("STARTED");
            for (String str : messagesToSend) {
                Bukkit.broadcastMessage(replacePlaceholders(str));
            }
            return;
        }

        if (announcesAmount >= maxAnnouncesAmount) {
            event.finishEvent(true);
            return;
        }

        List<String> messagesToSend = event.isHosted() ? event.getMessages("HAPPENING_HOSTED") : event.getMessages("HAPPENING");
        for (String str : messagesToSend) {
            Bukkit.broadcastMessage(replacePlaceholders(str));
        }
    }

    public void startTask() {
        this.runTaskTimerAsynchronously(VoltzEvents.get(), 0L, 20L * delayBetweenMessagesInSeconds);
    }

    private String replacePlaceholders(String str) {
        return StringUtils.replaceEach(str, new String[]{
                "{host}",
                "{host_rewards}",
                "{tag}",
                "{item}",
                "{announces_now}",
                "{announces_amount}"
        }, new String[]{
                event.isHosted() ? event.getEventHost().getHostPlayerName() : "-/-",
                event.getTotalHostRewardsDisplay(),
                event.getWinnerTag(),
                event.getTranslation(event.getCraftItem()),
                String.valueOf(announcesAmount),
                String.valueOf(maxAnnouncesAmount)
        });
    }
}