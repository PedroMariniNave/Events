package com.zpedroo.voltzevents.utils.formatter;

import com.zpedroo.voltzevents.utils.config.Settings;
import org.apache.commons.lang.time.DurationFormatUtils;
import java.util.concurrent.TimeUnit;

import static com.zpedroo.voltzevents.utils.config.TimeTranslations.*;

public class TimeFormatter {

    public static String format(int timeInSeconds) {
        long timeInMillis = TimeUnit.SECONDS.toMillis(timeInSeconds);
        return DurationFormatUtils.formatDuration(timeInMillis, Settings.TIME_FORMATTER, true);
    }

    public static String format(long timeInMillis) {
        return DurationFormatUtils.formatDuration(timeInMillis, Settings.TIME_FORMATTER, true);
    }

    public static String millisToFormattedTime(long timeInMillis) {
        StringBuilder builder = new StringBuilder();

        long hours = TimeUnit.MILLISECONDS.toHours(timeInMillis) - (TimeUnit.MILLISECONDS.toDays(timeInMillis) * 24);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) - (TimeUnit.MILLISECONDS.toHours(timeInMillis) * 60);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) - (TimeUnit.MILLISECONDS.toMinutes(timeInMillis) * 60);

        if (hours > 0) builder.append(hours).append(" ").append(hours == 1 ? HOUR : HOURS).append(" ");
        if (minutes > 0) builder.append(minutes).append(" ").append(minutes == 1 ? MINUTE : MINUTES).append(" ");
        if (seconds > 0) builder.append(seconds).append(" ").append(seconds == 1 ? SECOND : SECONDS);

        return builder.toString().isEmpty() ? NOW : builder.toString();
    }
}