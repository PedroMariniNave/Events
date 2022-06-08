package com.zpedroo.voltzevents.tasks;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.enums.EventPhase;
import com.zpedroo.voltzevents.types.Event;
import org.bukkit.scheduler.BukkitRunnable;

public class WarmupTask extends BukkitRunnable {

    private final Event event;
    private final String actionbar;
    private int timeLeft;
    private final Runnable actionWhenFinishTask;

    public WarmupTask(Event event, String actionbar, int timeLeft) {
        this(event, actionbar, timeLeft, null);
    }

    public WarmupTask(Event event, String actionbar, int timeLeft, Runnable actionWhenFinishTask) {
        this.event = event;
        this.actionbar = actionbar;
        this.timeLeft = timeLeft;
        this.actionWhenFinishTask = actionWhenFinishTask;
    }

    @Override
    public void run() {
        if (event.getEventPhase() != EventPhase.WARMUP) event.setEventPhase(EventPhase.WARMUP);

        if (--timeLeft <= 0) {
            this.cancel();
            event.setEventPhase(EventPhase.STARTED);

            if (actionWhenFinishTask != null) actionWhenFinishTask.run();
            return;
        }

        event.sendActionBarToAllParticipants(actionbar, new String[]{
                "{timer}"
        }, new String[]{
                String.valueOf(timeLeft)
        });
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void startTask() {
        this.runTaskTimerAsynchronously(VoltzEvents.get(), 0L, 20L);
    }
}