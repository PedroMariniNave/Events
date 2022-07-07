package com.zpedroo.voltzevents.objects.host;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.objects.event.WinnerSettings;
import com.zpedroo.voltzevents.types.Event;
import com.zpedroo.voltzevents.utils.cooldown.Cooldown;
import com.zpedroo.voltzevents.utils.formatter.HostRewardsFormatter;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;

@Data
public class EventHost {

    private final UUID hostPlayerUUID;
    private final Event event;
    private final Map<Currency, BigInteger> currencyRewards;

    public String getHostPlayerName() {
        return Bukkit.getOfflinePlayer(hostPlayerUUID).getName();
    }

    public String getTotalRewardsDisplay() {
        return HostRewardsFormatter.getTotalRewardsDisplay(currencyRewards);
    }

    public void giveRewards(Player player, WinnerSettings winnerSettings) {
        for (Map.Entry<Currency, BigInteger> entry : currencyRewards.entrySet()) {
            Currency currency = entry.getKey();
            BigInteger amount = entry.getValue();
            BigInteger amountToGive = winnerSettings.getAmountWithPercentageApplied(amount);

            CurrencyAPI.addCurrencyAmount(player.getUniqueId(), currency, amountToGive);
        }
    }

    public void refundHost() {
        for (Map.Entry<Currency, BigInteger> entry : currencyRewards.entrySet()) {
            Currency currency = entry.getKey();
            BigInteger amount = entry.getValue();

            CurrencyAPI.addCurrencyAmount(hostPlayerUUID, currency, amount);
        }
    }

    public void resetHostCooldown() {
        Cooldown.get().removeCooldown(hostPlayerUUID, VoltzEvents.class);
    }
}