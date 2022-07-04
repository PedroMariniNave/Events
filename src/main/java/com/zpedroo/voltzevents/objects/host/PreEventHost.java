package com.zpedroo.voltzevents.objects.host;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.voltzevents.types.Event;
import com.zpedroo.voltzevents.utils.formatter.HostRewardsFormatter;
import lombok.Data;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class PreEventHost implements Cloneable {

    private Event event;
    private final Map<Currency, BigInteger> rewardsCurrency = new HashMap<>(4);

    public EventHost create(UUID hostPlayerUUID) {
        return new EventHost(hostPlayerUUID, event, rewardsCurrency);
    }

    public BigInteger getCurrencyAmount(Currency currency) {
        return rewardsCurrency.getOrDefault(currency, BigInteger.ZERO);
    }

    public BigInteger removeCurrency(Currency currency) {
        return rewardsCurrency.remove(currency);
    }

    public void addCurrencyAmount(Currency currency, BigInteger amount) {
        setCurrencyAmount(currency, getCurrencyAmount(currency).add(amount));
    }

    public void removeCurrencyAmount(Currency currency, BigInteger amount) {
        setCurrencyAmount(currency, getCurrencyAmount(currency).subtract(amount));
    }

    public void setCurrencyAmount(Currency currency, BigInteger amount) {
        if (amount.signum() <= 0) {
            removeCurrency(currency);
            return;
        }

        rewardsCurrency.put(currency, amount);
    }

    public boolean hasRewards() {
        return !rewardsCurrency.isEmpty();
    }

    public boolean canPayAllRewards(Player player) {
        for (Map.Entry<Currency, BigInteger> entry : rewardsCurrency.entrySet()) {
            Currency currency = entry.getKey();
            BigInteger amount = entry.getValue();
            if (amount.compareTo(CurrencyAPI.getCurrencyAmount(player.getUniqueId(), currency)) > 0) return false;
        }

        return true;
    }

    public void removeAllRewardsFromHost(Player player) {
        for (Map.Entry<Currency, BigInteger> entry : rewardsCurrency.entrySet()) {
            Currency currency = entry.getKey();
            BigInteger amount = entry.getValue();

            CurrencyAPI.removeCurrencyAmount(player, currency, amount);
        }
    }

    public String getTotalRewardsDisplay() {
        return HostRewardsFormatter.getTotalRewardsDisplay(rewardsCurrency);
    }

    @Override
    public PreEventHost clone() {
        try {
            return (PreEventHost) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }
}