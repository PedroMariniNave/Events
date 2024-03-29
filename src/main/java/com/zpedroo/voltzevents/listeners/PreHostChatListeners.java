package com.zpedroo.voltzevents.listeners;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.enums.PreHostChatAction;
import com.zpedroo.voltzevents.enums.Result;
import com.zpedroo.voltzevents.objects.host.PreEventHost;
import com.zpedroo.voltzevents.objects.host.PreEventHostEdit;
import com.zpedroo.voltzevents.utils.config.Messages;
import com.zpedroo.voltzevents.utils.config.Settings;
import com.zpedroo.voltzevents.utils.menu.Menus;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class PreHostChatListeners implements Listener {

    private static final Map<Player, PreEventHostEdit> playersEditing = new HashMap<>(4);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!playersEditing.containsKey(event.getPlayer())) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        PreEventHostEdit preHostEdit = playersEditing.get(player);
        PreEventHost preEventHost = preHostEdit.getPreEventHost();
        PreEventHost preEventHostClone = preHostEdit.getPreEventHostClone();
        if (StringUtils.equalsIgnoreCase(Settings.CANCEL_ACTION_KEY, event.getMessage())) {
            cancelEdit(player, preEventHost, preEventHostClone);
            return;
        }

        Currency currency = preHostEdit.getCurrency();
        BigInteger amount = NumberFormatter.getInstance().filter(event.getMessage());

        if (!hasCurrencyAmount(player, currency, amount)) {
            player.sendMessage(Messages.INSUFFICIENT_CURRENCY);
            handleActionResult(player, preEventHost, preEventHostClone, Result.FAILED);
            return;
        }

        if (!isValidAmount(amount)) {
            player.sendMessage(Messages.INVALID_AMOUNT);
            handleActionResult(player, preEventHost, preEventHostClone, Result.FAILED);
            return;
        }

        BigInteger minimumValue = preHostEdit.getMinimumValue();
        if (minimumValue.compareTo(amount) > 0) {
            player.sendMessage(StringUtils.replace(Messages.MINIMUM_VALUE, "{minimum}", NumberFormatter.getInstance().format(minimumValue)));
            return;
        }

        PreHostChatAction action = preHostEdit.getAction();
        switch (action) {
            case ADD_CURRENCY:
                preEventHost.addCurrencyAmount(currency, amount);
                break;
            case REMOVE_CURRENCY:
                preEventHost.removeCurrencyAmount(currency, amount);
                break;
        }

        handleActionResult(player, preEventHost, preEventHostClone, Result.SUCCESSFUL);
    }

    private void handleActionResult(Player player, PreEventHost preEventHost, PreEventHost preEventHostClone, Result result) {
        cancelEdit(player, preEventHost, preEventHostClone);
        switch (result) {
            case SUCCESSFUL:
                player.playSound(player.getLocation(), Sound.VILLAGER_YES, 1f, 1f);
                break;
            case FAILED:
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
                break;
        }
    }

    private void cancelEdit(Player player, PreEventHost preEventHost, PreEventHost preEventHostClone) {
        playersEditing.remove(player);
        sync(() -> Menus.getInstance().openHostRewardsMenu(player, preEventHost, preEventHostClone));
    }

    private boolean isValidAmount(BigInteger amount) {
        return amount.signum() > 0;
    }

    private boolean hasCurrencyAmount(Player player, Currency currency, BigInteger amount) {
        return CurrencyAPI.getCurrencyAmount(player, currency).compareTo(amount) >= 0;
    }

    private void sync(Runnable runnable) {
        VoltzEvents.get().getServer().getScheduler().runTaskLater(VoltzEvents.get(), runnable, 0L);
    }

    public static Map<Player, PreEventHostEdit> getPlayersEditing() {
        return playersEditing;
    }
}