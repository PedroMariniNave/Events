package com.zpedroo.voltzevents.objects;

import com.zpedroo.voltzevents.utils.config.Settings;
import lombok.Data;

import java.text.SimpleDateFormat;

@Data
public class WinnerData {

    private final String name;
    private final int position;
    private final int kills;
    private final long winTimestamp;

    public String getFormattedWinTimestamp(long startTimestamp) {
        long elapsedTime = winTimestamp - startTimestamp;
        SimpleDateFormat dateFormatter = new SimpleDateFormat(Settings.TIME_FORMATTER);

        return dateFormatter.format(elapsedTime);
    }
}