package com.zpedroo.voltzevents.scheduler;

import com.zpedroo.voltzevents.types.Event;
import org.quartz.CronScheduleBuilder;

public class Task {

    private final Event event;
    private final CronScheduleBuilder cronScheduleBuilder;

    public Task(Event event, CronScheduleBuilder cronScheduleBuilder) {
        this.event = event;
        this.cronScheduleBuilder = cronScheduleBuilder;
    }

    public Event getEvent() {
        return event;
    }

    public CronScheduleBuilder getCronScheduleBuilder() {
        return cronScheduleBuilder;
    }
}