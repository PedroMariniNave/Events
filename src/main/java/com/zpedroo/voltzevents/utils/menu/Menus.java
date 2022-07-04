package com.zpedroo.voltzevents.utils.menu;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.enums.PreHostChatAction;
import com.zpedroo.voltzevents.listeners.PreHostChatListeners;
import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.objects.host.EventHost;
import com.zpedroo.voltzevents.objects.host.PreEventHost;
import com.zpedroo.voltzevents.objects.host.PreEventHostEdit;
import com.zpedroo.voltzevents.objects.player.PlayerData;
import com.zpedroo.voltzevents.types.Event;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.builder.InventoryBuilder;
import com.zpedroo.voltzevents.utils.builder.InventoryUtils;
import com.zpedroo.voltzevents.utils.builder.ItemBuilder;
import com.zpedroo.voltzevents.utils.color.Colorize;
import com.zpedroo.voltzevents.utils.config.Messages;
import com.zpedroo.voltzevents.utils.config.Settings;
import com.zpedroo.voltzevents.utils.cooldown.Cooldown;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;

import static com.zpedroo.voltzevents.utils.config.Settings.HOST_EVENT_PERMISSION;

public class Menus extends InventoryUtils {

    private static Menus instance;
    public static Menus getInstance() { return instance; }

    public Menus() {
        instance = this;
    }

    public void openMainMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.MAIN;

        String title = Colorize.getColored(FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);
        PlayerData data = DataManager.getInstance().getPlayerData(player);

        for (String items : FileUtils.get().getSection(file, "Inventory.items")) {
            String action = FileUtils.get().getString(file, "Inventory.items." + items + ".action");
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + items, new String[]{
                    "{player}",
                    "{wins}",
                    "{participations}",
                    "{win_rate}"
            }, new String[]{
                    player.getName(),
                    String.valueOf(data.getWinsAmount()),
                    String.valueOf(data.getParticipationsAmount()),
                    data.getFormattedWinRate()
            }).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + items + ".slot");

            inventory.addItem(item, slot, () -> {
                switch (action.toUpperCase()) {
                    case "EVENTS":
                        openEventsMenu(player);
                        break;
                    case "TOP":
                        openTopMenu(player);
                        break;
                }
            }, InventoryUtils.ActionType.ALL_CLICKS);
        }

        int hostEventSlot = FileUtils.get().getInt(file, "Host-Event-Items.slot");
        if (hostEventSlot != -1) {
            String toGet = getDisplay(player, HOST_EVENT_PERMISSION, VoltzEvents.class);
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Host-Event-Items." + toGet, new String[]{
                    "{cooldown}"
            }, new String[]{
                    Cooldown.get().getTimeLeftFormatted(player, VoltzEvents.class)
            }).build();

            inventory.addItem(item, hostEventSlot, () -> {
                if (!player.hasPermission(HOST_EVENT_PERMISSION) || Cooldown.get().isInCooldown(player, VoltzEvents.class)) {
                    player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
                    return;
                }

                openHostManagement(player, new PreEventHost());
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }

    public void openEventsMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.EVENTS;

        String title = Colorize.getColored(FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String items : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + items).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + items + ".slot");

            inventory.addItem(item, slot);
        }

        inventory.open(player);
    }

    public void openTopMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.TOP;

        String title = Colorize.getColored(FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        int pos = 0;
        String[] slots = FileUtils.get().getString(file, "Inventory.slots").replace(" ", "").split(",");

        for (PlayerData data : DataManager.getInstance().getTopWins()) {
            if (++pos > slots.length) break;

            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Item", new String[]{
                    "{pos}",
                    "{player}",
                    "{wins}",
                    "{participations}",
                    "{win_rate}"
            }, new String[]{
                    String.valueOf(pos),
                    Bukkit.getOfflinePlayer(data.getUniqueId()).getName(),
                    String.valueOf(data.getWinsAmount()),
                    String.valueOf(data.getParticipationsAmount()),
                    data.getFormattedWinRate(),
            }).build();

            int slot = Integer.parseInt(slots[pos-1]);

            inventory.addItem(item, slot);
        }

        inventory.open(player);
    }

    public void openSelectEventMenu(Player player, PreEventHost preEventHost) {
        FileUtils.Files file = FileUtils.Files.SELECT_EVENT;

        String title = Colorize.getColored(FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String items : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + items).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + items + ".slot");
            String eventName = FileUtils.get().getString(file, "Inventory.items." + items + ".event");
            Event event = DataManager.getInstance().getEventByName(eventName);

            inventory.addItem(item, slot, () -> {
                if (event == null) return;

                preEventHost.setEvent(event);
                openHostManagement(player, preEventHost);
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }

    public void openHostManagement(Player player, PreEventHost preEventHost) {
        FileUtils.Files file = FileUtils.Files.HOST_MANAGEMENT;

        String title = Colorize.getColored(FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String items : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + items, new String[]{
                    "{rewards}"
            }, new String[]{
                    preEventHost.getTotalRewardsDisplay()
            }).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + items + ".slot");
            String action = FileUtils.get().getString(file, "Inventory.items." + items + ".action");

            inventory.addItem(item, slot, () -> {
                switch (action.toUpperCase()) {
                    case "REWARDS":
                        openHostRewardsMenu(player, preEventHost);
                        break;
                    case "CONFIRM":
                        Event event = preEventHost.getEvent();
                        if (event == null) {
                            player.sendMessage(Messages.INVALID_EVENT);
                            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
                            return;
                        }

                        if (!preEventHost.hasRewards()) {
                            player.sendMessage(Messages.INVALID_REWARDS);
                            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
                            return;
                        }

                        if (event.isHappening()) {
                            player.sendMessage(Messages.ALREADY_STARTED);
                            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
                            return;
                        }

                        if (!preEventHost.canPayAllRewards(player)) {
                            player.sendMessage(Messages.INSUFFICIENT_CURRENCY);
                            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
                            return;
                        }

                        inventory.close(player);
                        preEventHost.removeAllRewardsFromHost(player);

                        Cooldown.get().addCooldown(player, VoltzEvents.class, Settings.HOST_EVENT_COOLDOWN);
                        EventHost eventHost = preEventHost.create(player.getUniqueId());
                        event.startEvent(eventHost);
                        event.join(player);
                        break;
                    case "CANCEL":
                        inventory.close(player);
                        break;
                }
            }, ActionType.ALL_CLICKS);
        }

        Event selectedEvent = preEventHost.getEvent();
        String selectedEventName = selectedEvent == null ? "none" : selectedEvent.getName().toLowerCase();
        ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Select-Event." + selectedEventName).build();
        int slot = FileUtils.get().getInt(file, "Select-Event.slot");

        inventory.addItem(item, slot, () -> openSelectEventMenu(player, preEventHost), ActionType.ALL_CLICKS);

        inventory.open(player);
    }

    public void openHostRewardsMenu(Player player, PreEventHost preEventHost) {
        FileUtils.Files file = FileUtils.Files.HOST_REWARDS;

        String title = Colorize.getColored(FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);
        final PreEventHost preEventHostClone = preEventHost.clone();

        for (String items : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + items).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + items + ".slot");
            String action = FileUtils.get().getString(file, "Inventory.items." + items + ".action");

            inventory.addItem(item, slot, () -> {
                switch (action.toUpperCase()) {
                    case "CONFIRM":
                        openHostManagement(player, preEventHost);
                        break;
                    case "CANCEL":
                        openHostManagement(player, preEventHostClone);
                        break;
                }
            }, ActionType.ALL_CLICKS);
        }

        for (String currencyName : FileUtils.get().getSection(file, "Currencies")) {
            Currency currency = CurrencyAPI.getCurrency(currencyName);
            if (currency == null) continue;

            BigInteger minCurrencyAmount = NumberFormatter.getInstance().filter(FileUtils.get().getString(file, "Currencies." + currencyName + ".minimum"));
            BigInteger currencyAmount = preEventHost.getCurrencyAmount(currency);

            String[] placeholders = new String[]{
                    "{currency}",
                    "{currency_amount}",
                    "{currency_min_amount}"
            };
            String[] replacers = new String[]{
                    currency.getCurrencyDisplay(),
                    currency.getAmountDisplay(currencyAmount),
                    currency.getAmountDisplay(minCurrencyAmount)
            };
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Currencies." + currencyName, placeholders, replacers).build();
            int slot = FileUtils.get().getInt(file, "Currencies." + currencyName + ".slot");

            inventory.addItem(item, slot);
            addCurrencyAddAction(player, preEventHost, inventory, currency, minCurrencyAmount, placeholders, replacers, slot);
            addCurrencyRemoveAction(player, preEventHost, inventory, currency, minCurrencyAmount, placeholders, replacers, slot);
            addCurrencyResetAction(player, preEventHost, inventory, currency, slot);
        }

        inventory.open(player);
    }

    private void addCurrencyAddAction(
            Player player, PreEventHost preEventHost, InventoryBuilder inventory, Currency currency,
            BigInteger minCurrencyAmount, String[] placeholders, String[] replacers, int slot
    ) {
        inventory.addAction(slot, () -> {
            inventory.close(player);

            PreEventHostEdit edit = new PreEventHostEdit(preEventHost, PreHostChatAction.ADD_CURRENCY, currency, minCurrencyAmount);
            PreHostChatListeners.getPlayersEditing().put(player, edit);

            for (String msg : Messages.ADD_CURRENCY) {
                player.sendMessage(StringUtils.replaceEach(msg, placeholders, replacers));
            }
        }, ActionType.LEFT_CLICK);
    }

    private void addCurrencyRemoveAction(
            Player player, PreEventHost preEventHost, InventoryBuilder inventory, Currency currency,
            BigInteger minCurrencyAmount, String[] placeholders, String[] replacers, int slot
    ) {
        inventory.addAction(slot, () -> {
            inventory.close(player);

            PreEventHostEdit edit = new PreEventHostEdit(preEventHost, PreHostChatAction.REMOVE_CURRENCY, currency, minCurrencyAmount);
            PreHostChatListeners.getPlayersEditing().put(player, edit);

            for (String msg : Messages.REMOVE_CURRENCY) {
                player.sendMessage(StringUtils.replaceEach(msg, placeholders, replacers));
            }
        }, ActionType.RIGHT_CLICK);
    }

    private void addCurrencyResetAction(Player player, PreEventHost preEventHost, InventoryBuilder inventory, Currency currency, int slot) {
        inventory.addAction(slot, () -> {
            BigInteger amountToRefund = preEventHost.removeCurrency(currency);
            if (amountToRefund.signum() <= 0) return;

            CurrencyAPI.addCurrencyAmount(player.getUniqueId(), currency, amountToRefund);
            openHostRewardsMenu(player, preEventHost);
        }, ActionType.SCROLL);
    }

    private String getDisplay(Player player, String permission, Object cooldownClass) {
        if (!player.hasPermission(permission)) {
            return "locked";
        } else if (Cooldown.get().isInCooldown(player, cooldownClass)) {
            return "cooldown";
        }

        return "unlocked";
    }
}