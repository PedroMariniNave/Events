package com.zpedroo.voltzevents.utils.formatter;

import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.voltzevents.utils.config.Settings;

import java.math.BigInteger;
import java.util.Map;

public class HostRewardsFormatter {

    public static String getTotalRewardsDisplay(Map<Currency, BigInteger> rewards) {
        if (rewards.isEmpty()) return "-/-";

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Currency, BigInteger> entry : rewards.entrySet()) {
            if (builder.length() > 0) builder.append(Settings.CURRENCY_SEPARATOR);

            Currency currency = entry.getKey();
            BigInteger amount = entry.getValue();

            builder.append(currency.getAmountDisplay(amount));
        }

        return builder.toString();
    }
}