package com.zpedroo.voltzevents.utils.scoreboard.objects;

import com.zpedroo.voltzevents.enums.EventPhase;
import lombok.Data;

import java.util.List;

@Data
public class Scoreboard {

    private final String title;
    private final List<String> lines;
    private final EventPhase displayPhase;
    private final int updateTime;
}