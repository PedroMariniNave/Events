package com.zpedroo.voltzevents.events.fastcraft.tasks;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.events.fastcraft.FastCraftEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

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
            for (String str : event.getMessage("STARTED")) {
                Bukkit.broadcastMessage(replacePlaceholders(str));
            }
            return;
        }

        if (announcesAmount >= maxAnnouncesAmount) {
            event.finishEvent(true);
            return;
        }

        for (String str : event.getMessage("HAPPENING")) {
            Bukkit.broadcastMessage(replacePlaceholders(str));
        }
    }

    public void startTask() {
        this.runTaskTimerAsynchronously(VoltzEvents.get(), 0L, 20L * delayBetweenMessagesInSeconds);
    }

    private String replacePlaceholders(String str) {
        return StringUtils.replaceEach(str, new String[]{
                "{tag}",
                "{item}",
                "{announces_now}",
                "{announces_amount}"
        }, new String[]{
                event.getWinnerTag(),
                event.getTranslation(event.getCraftItem()),
                String.valueOf(announcesAmount),
                String.valueOf(maxAnnouncesAmount)
        });
    }
}