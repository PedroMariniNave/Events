package com.zpedroo.voltzevents.objects;

import lombok.Data;

import java.util.List;

@Data
public class WinnerSettings {

    private final String display;
    private final List<String> winnerMessages;
    private final List<String> participantsMessages;
    private final List<String> rewards;
}