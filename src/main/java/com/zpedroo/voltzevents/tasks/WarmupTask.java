package com.zpedroo.voltzevents.tasks;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.enums.EventPhase;
import com.zpedroo.voltzevents.types.Event;
import org.bukkit.scheduler.BukkitRunnable;

public class WarmupTask extends BukkitRunnable {

    private final Event event;
    private final String actionbar;
    private int duration;
    private final Runnable actionWhenFinishTask;

    public WarmupTask(Event event, String actionbar, int duration) {
        this(event, actionbar, duration, null);
    }

    public WarmupTask(Event event, String actionbar, int duration, Runnable actionWhenFinishTask) {
        this.event = event;
        this.actionbar = actionbar;
        this.duration = duration;
        this.actionWhenFinishTask = actionWhenFinishTask;
    }

    @Override
    public void run() {
        if (event.getEventPhase() != EventPhase.WARMUP) event.setEventPhase(EventPhase.WARMUP);

        event.sendActionBarToAllParticipants(actionbar, new String[]{
                "{timer}"
        }, new String[]{
                String.valueOf(duration)
        });

        if (--duration <= 0) {
            this.cancel();
            event.setEventPhase(EventPhase.STARTED);

            if (actionWhenFinishTask != null) actionWhenFinishTask.run();
        }
    }

    public void startTask() {
        this.runTaskTimerAsynchronously(VoltzEvents.get(), 0L, 20L);
    }
}