package com.zpedroo.voltzevents.scheduler;

import com.zpedroo.voltzevents.types.Event;
import lombok.Data;
import org.quartz.CronScheduleBuilder;

@Data
public class Task {

    private final Event event;
    private final CronScheduleBuilder cronScheduleBuilder;
}