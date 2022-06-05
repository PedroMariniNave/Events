package com.zpedroo.voltzevents.utils.formatter;

import com.zpedroo.voltzevents.utils.config.Settings;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.util.concurrent.TimeUnit;

public class TimeFormatter {

    public static String format(int timeInSeconds) {
        long timeInMillis = TimeUnit.SECONDS.toMillis(timeInSeconds);
        return DurationFormatUtils.formatDuration(timeInMillis, Settings.TIME_FORMATTER, true);
    }

    public static String format(long timeInMillis) {
        return DurationFormatUtils.formatDuration(timeInMillis, Settings.TIME_FORMATTER, true);
    }
}