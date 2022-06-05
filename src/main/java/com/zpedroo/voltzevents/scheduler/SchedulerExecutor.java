package com.zpedroo.voltzevents.scheduler;

import com.zpedroo.voltzevents.types.Event;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class SchedulerExecutor implements Job {

    protected static final Map<String, Task> tasks = new HashMap<>(8);

    @Override
    public void execute(JobExecutionContext execution) {
        Task task = tasks.get(execution.getMergedJobDataMap().getString("task"));
        if (task == null) return;

        Event event = task.getEvent();
        if (event == null) return;

        event.startEvent();
    }
}