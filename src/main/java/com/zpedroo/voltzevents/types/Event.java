package com.zpedroo.voltzevents.types;

import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.api.PlayerLeaveEvent;
import com.zpedroo.voltzevents.enums.EventPhase;
import com.zpedroo.voltzevents.enums.LeaveReason;
import com.zpedroo.voltzevents.hooks.PlaceholderAPIHook;
import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.objects.event.SpecialItem;
import com.zpedroo.voltzevents.objects.event.WinnerData;
import com.zpedroo.voltzevents.objects.event.WinnerSettings;
import com.zpedroo.voltzevents.objects.host.EventHost;
import com.zpedroo.voltzevents.objects.player.EventData;
import com.zpedroo.voltzevents.objects.player.EventItems;
import com.zpedroo.voltzevents.objects.player.PlayerData;
import com.zpedroo.voltzevents.tasks.AnnounceTask;
import com.zpedroo.voltzevents.tasks.VoidCheckTask;
import com.zpedroo.voltzevents.tasks.WarmupTask;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.actionbar.ActionBarAPI;
import com.zpedroo.voltzevents.utils.config.Messages;
import com.zpedroo.voltzevents.utils.config.Settings;
import com.zpedroo.voltzevents.utils.formatter.TimeFormatter;
import com.zpedroo.voltzevents.utils.scoreboard.manager.ScoreboardManager;
import com.zpedroo.voltzevents.utils.scoreboard.objects.Scoreboard;
import com.zpedroo.voltzevents.utils.storer.ExpStorer;
import com.zpedroo.voltzevents.utils.storer.InventoryStorer;
import lombok.Data;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.*;

@Data
public abstract class Event {

    private final String name;
    private final FileUtils.Files file;
    private final List<Player> playersParticipating = new ArrayList<>(32);
    private final List<String> whitelistedCommands;
    private final HashMap<String, List<String>> messages;
    private final Map<Integer, String> winnersPosition;
    private Map<SpecialItem, Integer> specialItems;
    private final Map<EventPhase, Scoreboard> scoreboards;
    private final Map<Integer, WinnerSettings> winnerSettings;
    private final String winnerTag;
    private final int winnersAmount;
    private final int minimumPlayersToStart;
    private final int minimumPlayersAfterStart;
    private final boolean savePlayerInventory;
    private final boolean additionalVoidChecker;
    private EventHost eventHost;
    private EventItems eventItems;
    private EventData eventData;
    private EventPhase eventPhase = EventPhase.INACTIVE;
    private AnnounceTask announceTask;
    private WarmupTask warmupTask;
    private Location joinLocation;
    private Location exitLocation;

    public Event(String name, FileUtils.Files file, List<String> whitelistedCommands, String winnerTag, HashMap<String, List<String>> messages, Map<Integer, String> winnersPosition, int winnersAmount, int minimumPlayersToStart, int minimumPlayersAfterStart, boolean savePlayerInventory, boolean additionalVoidChecker, EventItems eventItems, Location joinLocation, Location exitLocation) {
        this.name = name;
        this.file = file;
        this.whitelistedCommands = whitelistedCommands;
        this.winnerTag = winnerTag;
        this.messages = messages;
        this.winnersPosition = winnersPosition;
        this.winnersAmount = winnersAmount;
        this.minimumPlayersToStart = minimumPlayersToStart;
        this.minimumPlayersAfterStart = minimumPlayersAfterStart;
        this.savePlayerInventory = savePlayerInventory;
        this.additionalVoidChecker = additionalVoidChecker;
        this.eventItems = eventItems;
        this.joinLocation = joinLocation;
        this.exitLocation = exitLocation;
        this.specialItems = DataManager.getInstance().getSpecialItemsFromFile(file);
        this.scoreboards = DataManager.getInstance().getScoreboardsFromFile(file);
        this.winnerSettings = DataManager.getInstance().getWinnerSettingsFromFile(file);
        new PlaceholderAPIHook(this);
    }

    public int getPlayersParticipatingAmount() {
        return playersParticipating.size();
    }

    public List<String> getMessages(String name) {
        return messages.get(name);
    }

    public WinnerSettings getWinnerSettings(int position) {
        return winnerSettings.get(position);
    }

    private String getListedWinners() {
        StringBuilder builder = new StringBuilder();

        for (int position = 1; position <= winnersAmount; ++position) {
            WinnerSettings winnerSettings = getWinnerSettings(position);
            if (winnerSettings == null) continue;
            if (builder.length() > 0) builder.append("\n");

            WinnerData winnerData = eventData.getWinnerData(position);
            String winnerName = winnerData == null ? Settings.NULL_WINNER : winnerData.getName();
            String formattedTime = winnerData == null ? "-/-" : winnerData.getFormattedWinTimestamp(eventData.getStartTimestamp());
            int kills = winnerData == null ? 0 : winnerData.getKills();

            builder.append(StringUtils.replaceEach(winnerSettings.getDisplay(), new String[]{
                    "{tag}",
                    "{rewards}",
                    "{player}",
                    "{position}",
                    "{kills}",
                    "{time}"
            }, new String[]{
                    winnerTag,
                    getRewardsDisplay(winnerSettings),
                    winnerName == null ? Settings.NULL_WINNER : winnerName,
                    String.valueOf(position),
                    String.valueOf(kills),
                    formattedTime,
            }));
        }

        return builder.toString();
    }

    public String getRewardsDisplay(WinnerSettings winnerSettings) {
        if (isHosted()) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<Currency, BigInteger> entry : eventHost.getCurrencyRewards().entrySet()) {
                if (builder.length() > 0) builder.append(Settings.CURRENCY_SEPARATOR);

                Currency currency = entry.getKey();
                BigInteger amount = entry.getValue();
                BigInteger amountWithPercentageApplied = winnerSettings.getAmountWithPercentageApplied(amount);

                builder.append(currency.getAmountDisplay(amountWithPercentageApplied));
            }

            return builder.toString();
        }

        return winnerSettings.getRewardsDisplay();
    }

    public String getTotalHostRewardsDisplay() {
        if (!isHosted()) return "";

        return eventHost.getTotalRewardsDisplay();
    }

    public String getWinnerName(int position) {
        return winnersPosition.getOrDefault(position, Settings.NULL_WINNER);
    }

    public int getWinnerPosition(@NotNull Player player) {
        return getWinnerPosition(player.getName());
    }

    public int getWinnerPosition(String winnerName) {
        for (Map.Entry<Integer, String> entry : winnersPosition.entrySet()) {
            String winnerFoundName = entry.getValue();
            int position = entry.getKey();
            if (StringUtils.equals(winnerFoundName, winnerName)) return position;
        }

        return -1;
    }

    public boolean hasWinner(int position) {
        return winnersPosition.get(position) != null;
    }

    public String replacePlaceholders(String text, String[] placeholders, String[] replacers) {
        return StringUtils.replaceEach(text, placeholders, replacers);
    }

    public String replaceFinishedEventPlaceholders(String text) {
        return replacePlaceholders(text, new String[]{
                "{host}",
                "{winners}",
                "{tag}"
        }, new String[]{
                isHosted() ? eventHost.getHostPlayerName() : "-/-",
                getListedWinners(),
                getWinnerTag()
        });
    }

    public String replacePlayerPlaceholders(String text, Player player, WinnerSettings winnerSettings) {
        if (eventData == null) return text;

        int position = getWinnerPosition(player);
        WinnerData winnerData = eventData.getWinnerData(position);

        return replacePlaceholders(text, new String[]{
                "{player}",
                "{position}",
                "{kills}",
                "{time}",
                "{rewards}",
                "{tag}"
        }, new String[]{
                winnerData.getName(),
                String.valueOf(position),
                String.valueOf(winnerData.getKills()),
                winnerData.getFormattedWinTimestamp(eventData.getStartTimestamp()),
                getRewardsDisplay(winnerSettings),
                getWinnerTag()
        });
    }

    public Scoreboard getScoreboard() {
        return scoreboards.get(eventPhase);
    }

    public boolean isHosted() {
        return eventHost != null;
    }

    public boolean isHappening() {
        return eventPhase != EventPhase.INACTIVE;
    }

    public boolean isStarted() {
        return eventPhase == EventPhase.STARTED;
    }

    public boolean isFinished() {
        return eventPhase == EventPhase.INACTIVE;
    }

    public boolean canJoin() {
        return eventPhase == EventPhase.WAITING_PLAYERS;
    }

    public boolean isParticipating(Player player) {
        return playersParticipating.contains(player);
    }

    public boolean isWinner(Player player) {
        return getWinnerPosition(player) != -1;
    }

    private boolean isVisibilityToggleAllowed() {
        if (specialItems != null) {
            for (SpecialItem specialItem : specialItems.keySet()) {
                if (!StringUtils.equalsIgnoreCase(specialItem.getAction(), "SWITCH_VISIBILITY")) continue;

                return true;
            }
        }

        return false;
    }

    public int getPlayerKills(Player player) {
        return eventData == null ? 0 : eventData.getPlayerKills(player);
    }

    public String getFormattedTime() {
        if (eventData != null && eventData.getStartTimestamp() == -1) return announceTask == null ? "-/-" : TimeFormatter.format(announceTask.getCountdown());

        return eventData == null ? "-/-" : eventData.getFormattedStartTimestamp();
    }

    public Player getRandomParticipant() {
        int participantsAmount = getPlayersParticipatingAmount();
        if (participantsAmount <= 0) return null;

        int randomIndex = new Random().nextInt(participantsAmount);

        return playersParticipating.get(randomIndex);
    }

    public Player getRandomParticipant(Player playerToIgnore) {
        Player selected = getRandomParticipant();
        while (selected.equals(playerToIgnore)) {
            selected = getRandomParticipant();
        }

        return selected;
    }

    public void setWinner(String winnerName, int position) {
        this.winnersPosition.put(position, winnerName);
        this.eventData.setWinnerData(winnerName, position, System.currentTimeMillis());
    }

    public void setEventItems(ItemStack[] inventoryItems, ItemStack[] armorItems) {
        this.eventItems = new EventItems(inventoryItems, armorItems);
    }

    public void setJoinLocation(Location joinLocation) {
        this.joinLocation = joinLocation;
        DataManager.getInstance().saveLocationsInFile(this);
    }

    public void setExitLocation(Location exitLocation) {
        this.exitLocation = exitLocation;
        DataManager.getInstance().saveLocationsInFile(this);
    }

    public void join(Player player) {
        if (!canJoin()) {
            player.sendMessage(Messages.NOT_STARTED);
            return;
        }

        Event participatingEvent = DataManager.getInstance().getPlayerParticipatingEvent(player);
        if (participatingEvent != null) {
            player.sendMessage(Messages.ALREADY_PARTICIPATING);
            return;
        }

        DataManager.getInstance().setPlayerParticipatingEvent(player, this);
        if (savePlayerInventory || eventItems != null) {
            InventoryStorer.storePlayerInventory(player);
            ExpStorer.storePlayerExp(player);
            clearPotionEffects(player);
        }

        if (!(this instanceof PvPEvent)) {
            if (eventItems != null) addEventItemsToPlayer(player);
        }

        if (specialItems != null) setPlayerSpecialItems(player);

        playersParticipating.add(player);
        setScoreboard(player);

        if (isVisibilityToggleAllowed()) {
            updateAllParticipantsView();
        }

        player.setAllowFlight(false);
        player.setFlying(false);
        player.teleport(joinLocation);

        if (additionalVoidChecker) {
            VoidCheckTask voidCheckTask = new VoidCheckTask(this, player);
            voidCheckTask.startTask();
        }
    }

    public void leave(Player player, LeaveReason leaveReason) {
        leave(player, leaveReason, false, false);
    }

    public void leave(Player player, LeaveReason leaveReason, boolean checkParticipantsAmount, boolean checkTopOne) {
        if (!isHappening()) {
            player.sendMessage(Messages.NOT_STARTED);
            return;
        }

        PlayerLeaveEvent event = new PlayerLeaveEvent(player, this, leaveReason, getPlayersParticipatingAmount());
        Bukkit.getPluginManager().callEvent(event);

        DataManager.getInstance().setPlayerParticipatingEvent(player, null);
        if (eventItems != null || specialItems != null) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[4]);
        }

        if (savePlayerInventory) {
            InventoryStorer.restorePlayerInventory(player);
            ExpStorer.restorePlayerExp(player);
            clearPotionEffects(player);
        }

        playersParticipating.remove(player);
        removeScoreboard(player);
        player.teleport(exitLocation);
        showAllParticipants(player);

        if (checkParticipantsAmount) {
            checkParticipantsAmount(checkTopOne);
        }
    }

    public void setScoreboard(Player player) {
        ScoreboardManager.setScoreboard(player, this);
    }

    public void removeScoreboard(Player player) {
        ScoreboardManager.removeScoreboard(player);
    }

    public void addEventItemsToPlayer(Player player) {
        if (eventItems == null) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[4]);
        } else {
            player.getInventory().setContents(eventItems.getInventoryItems());
            player.getInventory().setArmorContents(eventItems.getArmorItems());
        }

        player.updateInventory();
    }

    public void setPlayerSpecialItems(Player player) {
        if (specialItems == null) return;

        PlayerData data = DataManager.getInstance().getPlayerData(player);
        for (Map.Entry<SpecialItem, Integer> entry : specialItems.entrySet()) {
            SpecialItem specialItem = entry.getKey();
            if (specialItem == null) continue;

            int slot = entry.getValue();
            boolean status = data.getSpecialItemStatus(specialItem);

            ItemStack item = status ? specialItem.getDefaultItem() : specialItem.getSecondaryItem();
            if (item == null) continue;

            player.getInventory().setItem(slot, item);
        }

        player.updateInventory();
    }

    private void clearPotionEffects(Player player) {
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
    }

    private void checkParticipantsAmount(boolean checkTopOne) {
        if (getPlayersParticipatingAmount() > minimumPlayersAfterStart || eventPhase == EventPhase.WAITING_PLAYERS) return;

        if (checkTopOne) {
            Player winner = playersParticipating.size() > 0 ? playersParticipating.get(0) : null;
            if (winner != null) winEvent(winner, 1);
        }

        if (!winnersPosition.isEmpty()) {
            this.finishEvent(true);
        } else {
            this.cancelEvent();
        }
    }

    public void startEvent() {
        startEvent(null);
    }

    public void startEvent(@Nullable EventHost eventHost) {
        if (isHappening()) return;

        this.eventHost = eventHost;
        this.winnersPosition.clear();
        this.eventPhase = EventPhase.WAITING_PLAYERS;
        this.eventData = new EventData();
        this.announceTask.startTask();
    }

    public void cancelEvent() {
        cancelEvent(true);
    }

    public void cancelEvent(boolean sendCancelledMessages) {
        if (!isHappening()) return;

        if (isHosted()) {
            eventHost.refundHost();
        }

        if (sendCancelledMessages) sendCancelledMessages();

        this.announceTask.cancelTask();
        this.finishEvent(false);
    }

    public void finishEvent(boolean sendFinishMessages) {
        if (!isHappening()) return;

        if (sendFinishMessages) sendFinishMessages();

        new ArrayList<>(playersParticipating).forEach(player -> leave(player, LeaveReason.EVENT_FINISHED, false, false));
        this.eventPhase = EventPhase.INACTIVE;
        this.eventHost = null;
    }

    public void sendCancelledMessages() {
        for (String msg : getMessages("CANCELLED")) {
            Bukkit.broadcastMessage(replaceFinishedEventPlaceholders(msg));
        }
    }

    public void sendFinishMessages() {
        List<String> messagesToSend = isHosted() ? getMessages("FINISHED_HOSTED") : getMessages("FINISHED");
        for (String msg : messagesToSend) {
            Bukkit.broadcastMessage(replaceFinishedEventPlaceholders(msg));
        }
    }

    public void winEvent(Player player, int position) {
        if (isWinner(player)) return;

        setWinner(player.getName(), position);

        WinnerSettings winnerSettings = getWinnerSettings(position);
        if (winnerSettings != null) {
            for (String msg : winnerSettings.getWinnerMessages()) {
                player.sendMessage(replacePlayerPlaceholders(msg, player, winnerSettings));
            }

            for (String msg : winnerSettings.getParticipantsMessages()) {
                sendMessageToAllParticipants(replacePlayerPlaceholders(msg, player, winnerSettings), Collections.singletonList(player));
            }

            giveRewards(player, winnerSettings);
        }

        if (position == 1) {
            PlayerData data = DataManager.getInstance().getPlayerData(player);
            data.addWins(1);
        }
    }

    private void giveRewards(Player player, WinnerSettings winnerSettings) {
        if (isHosted()) {
            eventHost.giveRewards(player, winnerSettings);
            return;
        }

        new BukkitRunnable() { // inventory will be restored after 2 ticks, so we'll deliver rewards in 4 ticks (2 ticks later)
            @Override
            public void run() {
                for (String command : winnerSettings.getRewardsCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replacePlayerPlaceholders(command, player, winnerSettings));
                }
            }
        }.runTaskLater(VoltzEvents.get(), 4L);
    }

    public void updatePlayerView(Player player) {
        PlayerData data = DataManager.getInstance().getPlayerData(player);
        if (data.isViewingParticipants()) {
            showAllParticipants(player);
        } else {
            hideAllParticipants(player);
        }
    }

    public void updateAllParticipantsView() {
        playersParticipating.forEach(this::updatePlayerView);
    }

    public void hidePlayer(@NotNull Player viewer, @NotNull Player target) {
        if (viewer.equals(target) || !viewer.canSee(target)) return;
        if (this instanceof PvPEvent) {
            PvPEvent event = (PvPEvent) Event.this;
            if (event.isFighting(target)) return;
        }

        VoltzEvents.get().getServer().getScheduler().runTaskLater(VoltzEvents.get(), () -> viewer.hidePlayer(target), 0L); // sync method
        sendTabPacket((CraftPlayer) viewer, ((CraftPlayer) target).getHandle());
    }

    public void showPlayer(@NotNull Player viewer, @NotNull Player target) {
        if (viewer.equals(target) || viewer.canSee(target)) return;

        VoltzEvents.get().getServer().getScheduler().runTaskLater(VoltzEvents.get(), () -> viewer.showPlayer(target), 0L); // sync method
    }

    public void showPlayerToAllParticipants(@NotNull Player target) {
        playersParticipating.forEach(viewer -> showPlayer(viewer, target));
    }

    public void hideAllParticipants(@NotNull Player viewer) {
        playersParticipating.forEach(target -> hidePlayer(viewer, target));
    }

    public void showAllParticipants(@NotNull Player viewer) {
        playersParticipating.forEach(target -> showPlayer(viewer, target));
    }

    private void sendTabPacket(CraftPlayer viewer, EntityPlayer target) {
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, target);
        viewer.getHandle().playerConnection.sendPacket(packet);
    }

    public void setAllParticipantsLevel(int level) {
        playersParticipating.forEach(player -> player.setLevel(level));
    }

    public void sendMessageToAllParticipants(String message, List<Player> playersToIgnore) {
        playersParticipating.stream().filter(participant -> playersToIgnore != null && !playersToIgnore.contains(participant)).forEach(player -> player.sendMessage(message));
    }

    public void sendMessageToAllParticipants(List<String> messages) {
        for (String message : messages) {
            sendMessageToAllParticipants(message, Collections.emptyList());
        }
    }

    public void sendMessageToAllParticipants(List<String> messages, List<Player> playersToIgnore) {
        for (String message : messages) {
            sendMessageToAllParticipants(message, playersToIgnore);
        }
    }

    public void sendMessageToAllParticipants(String message, String[] placeholders, String[] replacers) {
        sendMessageToAllParticipants(StringUtils.replaceEach(message, placeholders, replacers), Collections.emptyList());
    }

    public void sendMessageToAllParticipants(List<String> messages, String[] placeholders, String[] replacers) {
        List<String> replacedMessages = new ArrayList<>(messages.size());
        for (String message : messages) {
            replacedMessages.add(StringUtils.replaceEach(message, placeholders, replacers));
        }

        sendMessageToAllParticipants(replacedMessages);
    }

    public void sendMessageToAllParticipants(List<String> messages, String[] placeholders, String[] replacers, List<Player> playersToIgnore) {
        List<String> replacedMessages = new ArrayList<>(messages.size());
        for (String message : messages) {
            replacedMessages.add(StringUtils.replaceEach(message, placeholders, replacers));
        }

        sendMessageToAllParticipants(replacedMessages, playersToIgnore);
    }

    public void sendTitleToAllParticipants(String title, String subtitle) {
        playersParticipating.forEach(player -> player.sendTitle(title, subtitle));
    }

    public void sendTitleToAllParticipants(String title, String subtitle, String[] placeholders, String[] replacers) {
        sendTitleToAllParticipants(StringUtils.replaceEach(title, placeholders, replacers), StringUtils.replaceEach(subtitle, placeholders, replacers));
    }

    public void sendActionBarToAllParticipants(String text) {
        playersParticipating.forEach(player -> ActionBarAPI.sendActionBar(player, text));
    }

    public void sendActionBarToAllParticipants(String text, String[] placeholders, String[] replacers) {
        sendActionBarToAllParticipants(StringUtils.replaceEach(text, placeholders, replacers));
    }

    public void playSoundToAllParticipants(Sound sound) {
        this.playSoundToAllParticipants(sound, 1f, 1f);
    }

    public void playSoundToAllParticipants(Sound sound, float volume, float pitch) {
        playersParticipating.forEach(player -> {
            player.playSound(player.getLocation(), sound, volume, pitch);
        });
    }

    public abstract void checkIfPlayerIsWinner(Player player, int participantsAmount);

    public abstract void startEventMethods();

    public abstract void teleportPlayersToArenaAndExecuteEventActions();

    public abstract void resetAllValues();
}