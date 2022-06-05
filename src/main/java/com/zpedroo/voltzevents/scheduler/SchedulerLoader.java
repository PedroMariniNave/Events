package com.zpedroo.voltzevents.scheduler;

import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.types.Event;
import com.zpedroo.voltzevents.utils.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SchedulerLoader extends SchedulerExecutor {

    private static SchedulerLoader instance;
    public static SchedulerLoader getInstance() { return instance; }

    private final SchedulerFactory factory = new StdSchedulerFactory();

    public SchedulerLoader() {
        instance = this;
    }

    public void startAllEventsScheduler() throws SchedulerException {
        Scheduler scheduler = factory.getScheduler();
        scheduler.start();

        for (Event event : DataManager.getInstance().getEvents()) {
            List<Task> tasks = buildEventTasks(event);
            if (tasks == null) continue;

            for (Task task : tasks) {
                String taskIdentifier = UUID.randomUUID().toString();

                JobDetail detail = JobBuilder.newJob(SchedulerExecutor.class).withIdentity(taskIdentifier).usingJobData("task", taskIdentifier).build();
                CronTrigger trigger = TriggerBuilder.newTrigger().withSchedule(task.getCronScheduleBuilder()).forJob(detail).build();
                scheduler.scheduleJob(detail, trigger);

                SchedulerExecutor.tasks.put(taskIdentifier, task);
            }
        }
    }

    public void stopAllSchedulers() throws SchedulerException {
        Scheduler scheduler = factory.getScheduler();
        scheduler.clear();
    }

    private List<Task> buildEventTasks(Event event) {
        FileConfiguration file = FileUtils.get().getFile(event.getFile()).get();
        if (file.contains("Auto-Start.enabled") && !file.getBoolean("Auto-Start.enabled")) return null;

        List<Task> ret = new ArrayList<>(4);
        for (String date : file.getStringList("Auto-Start.schedules")) {
            String formattedDate = parseDate(date);
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(formattedDate);

            ret.add(new Task(event, cronScheduleBuilder));
        }

        return ret;
    }

    private String parseDate(String date) {
        String[] split = date.split(":");

        String rawDayValue = split[0];
        String day = rawDayValue.equalsIgnoreCase("EVERYDAY") ? "*" : rawDayValue.substring(0, 3);

        String hour = split[1];
        String minute = split[2];

        String dateModel = "0 M H ? * D";
        return dateModel.replace("M", minute).replace("H", hour).replace("D", day);
    }
}