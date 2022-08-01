package com.zpedroo.voltzevents.objects.general;

import com.zpedroo.voltzevents.enums.EventPhase;
import lombok.Data;

import java.util.List;

@Data
public class ScoreboardInfo {

    private final String title;
    private final List<String> lines;
    private final EventPhase displayPhase;
}