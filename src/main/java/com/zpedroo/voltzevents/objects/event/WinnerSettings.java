package com.zpedroo.voltzevents.objects.event;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
public class WinnerSettings {

    private final String display;
    private final String rewardsDisplay;
    private final List<String> winnerMessages;
    private final List<String> participantsMessages;
    private final List<String> rewardsCommands;
    private final int hostRewardsPercentage;

    public BigInteger getAmountWithPercentageApplied(BigInteger amount) {
        return amount.multiply(BigInteger.valueOf(hostRewardsPercentage)).divide(BigInteger.valueOf(100L));
    }
}