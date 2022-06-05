package com.zpedroo.voltzevents.types;

import com.zpedroo.voltzevents.api.PlayerLeaveEvent;
import com.zpedroo.voltzevents.enums.EventPhase;
import com.zpedroo.voltzevents.hooks.PlaceholderAPIHook;
import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.objects.*;
import com.zpedroo.voltzevents.tasks.AnnounceTask;
import com.zpedroo.voltzevents.tasks.VoidCheckTask;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.actionbar.ActionBarAPI;
import com.zpedroo.voltzevents.utils.config.Messages;
import com.zpedroo.voltzevents.utils.config.Settings;
import com.zpedroo.voltzevents.utils.formatter.TimeFormatter;
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
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Data
public abstract class Event {

    private final String name;
    private final FileUtils.Files file;
    private final List<Player> playersParticipating = new ArrayList<>(32);
    private final HashMap<String, List<String>> messages;
    private final Map<Integer, String> winnersPosition;
    private Map<SpecialItem, Integer> specialItems;
    private final String winnerTag;
    private final int winnersAmount;
    private final int minimumPlayers;
    private final boolean savePlayerInventory;
    private final boolean additionalVoidChecker;
    private EventItems eventItems;
    private EventData eventData;
    private EventPhase eventPhase = EventPhase.INACTIVE;
    private AnnounceTask announceTask;
    private Location joinLocation;
    private Location exitLocation;

    public Event(String name, FileUtils.Files file, String winnerTag, HashMap<String, List<String>> messages, Map<Integer, String> winnersPosition, int winnersAmount, int minimumPlayers, boolean savePlayerInventory, boolean additionalVoidChecker, EventItems eventItems, Location joinLocation, Location exitLocation) {
        this.name = name;
        this.file = file;
        this.winnerTag = winnerTag;
        this.messages = messages;
        this.winnersPosition = winnersPosition;
        this.winnersAmount = winnersAmount;
        this.minimumPlayers = minimumPlayers;
        this.savePlayerInventory = savePlayerInventory;
        this.additionalVoidChecker = additionalVoidChecker;
        this.eventItems = eventItems;
        this.joinLocation = joinLocation;
        this.exitLocation = exitLocation;
        this.specialItems = DataManager.getInstance().getSpecialItemsFromFile(file);
        new PlaceholderAPIHook(this);
    }

    public Map<SpecialItem, Integer> getSpecialItems() {
        return specialItems;
    }

    public int getPlayersParticipatingAmount() {
        return playersParticipating.size();
    }

    public List<String> getMessage(String message) {
        return messages.get(message);
    }

    private String getListedWinners() {
        StringBuilder builder = new StringBuilder();

        for (int position = 1; position <= winnersAmount; ++position) {
            WinnerSettings winnerSettings = DataManager.getInstance().getWinnerSettings(this, position);
            if (winnerSettings == null) continue;
            if (builder.length() > 0) builder.append("\n");

            WinnerData winnerData = eventData.getWinnerData(position);
            String winnerName = winnerData == null ? Settings.NULL_WINNER : winnerData.getName();
            String formattedTime = winnerData == null ? "-/-" : winnerData.getFormattedWinTimestamp(eventData.getStartTimestamp());
            int kills = winnerData == null ? 0 : winnerData.getKills();

            builder.append(StringUtils.replaceEach(winnerSettings.getDisplay(), new String[]{
                    "{player}",
                    "{position}",
                    "{kills}",
                    "{time}"
            }, new String[]{
                    winnerName == null ? Settings.NULL_WINNER : winnerName,
                    String.valueOf(position),
                    String.valueOf(kills),
                    formattedTime,
            }));
        }

        return builder.toString();
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
                "{winners}",
                "{tag}"
        }, new String[]{
                getListedWinners(),
                getWinnerTag()
        });
    }

    public String replacePlayerPlaceholders(Player player, String text) {
        if (eventData == null) return text;

        int position = getWinnerPosition(player);
        WinnerData winnerData = eventData.getWinnerData(position);

        return replacePlaceholders(text, new String[]{
                "{player}",
                "{position}",
                "{kills}",
                "{time}",
                "{tag}"
        }, new String[]{
                winnerData.getName(),
                String.valueOf(position),
                String.valueOf(winnerData.getKills()),
                winnerData.getFormattedWinTimestamp(eventData.getStartTimestamp()),
                getWinnerTag()
        });
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
        return eventPhase == EventPhase.ANNOUNCE;
    }

    public boolean isParticipating(Player player) {
        return playersParticipating.contains(player);
    }

    public int getPlayerKills(Player player) {
        return eventData == null ? 0 : eventData.getPlayerKills(player);
    }

    public String getFormattedTime() {
        if (eventData != null && eventData.getStartTimestamp() == -1) return announceTask == null ? "-/-" : TimeFormatter.format(announceTask.getCountdown());

        return eventData == null ? "-/-" : eventData.getFormattedStartTimestamp();
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
        if (isParticipating(player)) {
            leave(player, true, false);
            return;
        }

        if (!canJoin()) {
            player.sendMessage(Messages.NOT_STARTED);
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
            if (specialItems != null) setPlayerSpecialItems(player);
        }

        playersParticipating.add(player);
        updateAllParticipantsView();
        player.teleport(joinLocation);
        if (!player.hasPermission(Settings.ADMIN_PERMISSION)) player.setFlying(false);

        if (additionalVoidChecker) {
            VoidCheckTask voidCheckTask = new VoidCheckTask(this, player);
            voidCheckTask.startTask();
        }
    }

    public void leave(Player player, boolean checkParticipantsAmount, boolean checkWinner) {
        if (!isParticipating(player)) {
            player.sendMessage(Messages.NOT_PARTICIPATING);
            return;
        }

        if (!isHappening()) {
            player.sendMessage(Messages.NOT_STARTED);
            return;
        }

        if (checkWinner) checkWinner(player);

        PlayerLeaveEvent event = new PlayerLeaveEvent(player, this);
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
        player.teleport(exitLocation);

        if (checkParticipantsAmount) {
            checkParticipantsAmount();
        }
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

    private void checkParticipantsAmount() {
        if (getPlayersParticipatingAmount() <= 0 && eventPhase != EventPhase.ANNOUNCE) {
            if (!winnersPosition.isEmpty()) {
                this.finishEvent(true);
            } else {
                this.cancelEvent();
            }
        }
    }

    public void startEvent() {
        if (isHappening()) return;

        this.winnersPosition.clear();
        this.eventPhase = EventPhase.ANNOUNCE;
        this.eventData = new EventData();
        this.announceTask.startTask();
    }

    public void cancelEvent() {
        if (!isHappening()) return;

        new ArrayList<>(playersParticipating).forEach(player -> leave(player, false, false));
        this.announceTask.cancelTask();
        this.eventPhase = EventPhase.INACTIVE;

        sendCanceledMessages();
    }

    public void finishEvent(boolean sendFinishMessages) {
        if (!isHappening()) return;

        new ArrayList<>(playersParticipating).forEach(player -> leave(player, false, false));
        this.eventPhase = EventPhase.INACTIVE;

        if (sendFinishMessages) sendFinishMessages();
    }

    public void sendCanceledMessages() {
        for (String msg : getMessage("CANCELLED")) {
            Bukkit.broadcastMessage(replaceFinishedEventPlaceholders(msg));
        }
    }

    public void sendFinishMessages() {
        for (String msg : getMessage("FINISHED")) {
            Bukkit.broadcastMessage(replaceFinishedEventPlaceholders(msg));
        }
    }

    public void winEvent(Player player, int position, boolean forceLeave) {
        if (isParticipating(player) && forceLeave) leave(player, false, false);

        setWinner(player.getName(), position);

        WinnerSettings winnerSettings = DataManager.getInstance().getWinnerSettings(this, position);
        if (winnerSettings != null) {
            for (String msg : winnerSettings.getWinnerMessages()) {
                player.sendMessage(replacePlayerPlaceholders(player, msg));
            }

            for (String msg : winnerSettings.getParticipantsMessages()) {
                sendMessageToAllParticipants(replacePlayerPlaceholders(player, msg));
            }

            for (String command : winnerSettings.getRewards()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replacePlayerPlaceholders(player, command));
            }
        }

        if (position == 1) {
            PlayerData data = DataManager.getInstance().getPlayerData(player);
            data.addWins(1);
        }
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

    public void hidePlayer(Player viewer, Player target) {
        if (!viewer.canSee(target)) return;
        if (this instanceof PvPEvent) {
            PvPEvent event = (PvPEvent) Event.this;
            if (event.isFighting(target)) return;
        }

        viewer.hidePlayer(target);
        sendTabPacket((CraftPlayer) viewer, ((CraftPlayer) target).getHandle());
    }

    public void showPlayer(Player viewer, Player target) {
        if (viewer.canSee(target)) return;

        viewer.showPlayer(target);
    }

    public void showPlayerToAllParticipants(Player target) {
        playersParticipating.forEach(viewer -> showPlayer(viewer, target));
    }

    public void hideAllParticipants(Player viewer) {
        playersParticipating.forEach(target -> hidePlayer(viewer, target));
    }

    public void showAllParticipants(Player viewer) {
        playersParticipating.forEach(target -> showPlayer(viewer, target));
    }

    private void sendTabPacket(CraftPlayer viewer, EntityPlayer target) {
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, target);
        viewer.getHandle().playerConnection.sendPacket(packet);
    }

    public void setAllParticipantsLevel(int level) {
        playersParticipating.forEach(player -> player.setLevel(level));
    }

    public void sendMessageToAllParticipants(String message) {
        playersParticipating.forEach(player -> player.sendMessage(message));
    }

    public void sendMessageToAllParticipants(List<String> messages) {
        for (String message : messages) {
            this.sendMessageToAllParticipants(message);
        }
    }

    public void sendMessageToAllParticipants(String message, String[] placeholders, String[] replacers) {
        this.sendMessageToAllParticipants(StringUtils.replaceEach(message, placeholders, replacers));
    }

    public void sendMessageToAllParticipants(List<String> messages, String[] placeholders, String[] replacers) {
        List<String> replacedMessages = new ArrayList<>(messages.size());
        for (String message : messages) {
            replacedMessages.add(StringUtils.replaceEach(message, placeholders, replacers));
        }

        this.sendMessageToAllParticipants(replacedMessages);
    }

    public void sendTitleToAllParticipants(String title, String subtitle) {
        playersParticipating.forEach(player -> player.sendTitle(title, subtitle));
    }

    public void sendTitleToAllParticipants(String title, String subtitle, String[] placeholders, String[] replacers) {
        this.sendTitleToAllParticipants(StringUtils.replaceEach(title, placeholders, replacers), StringUtils.replaceEach(subtitle, placeholders, replacers));
    }

    public void sendActionBarToAllParticipants(String text) {
        playersParticipating.forEach(player -> ActionBarAPI.sendActionBar(player, text));
    }

    public void sendActionBarToAllParticipants(String text, String[] placeholders, String[] replacers) {
        this.sendActionBarToAllParticipants(StringUtils.replaceEach(text, placeholders, replacers));
    }

    public void playSoundToAllParticipants(Sound sound) {
        this.playSoundToAllParticipants(sound, 1f, 1f);
    }

    public void playSoundToAllParticipants(Sound sound, float volume, float pitch) {
        playersParticipating.forEach(player -> {
            player.playSound(player.getLocation(), sound, volume, pitch);
        });
    }

    public abstract void checkWinner(Player player);

    public abstract void startEventMethods();

    public abstract void teleportPlayersToArenaAndExecuteEventActions();

    public abstract void resetAllValues();
}