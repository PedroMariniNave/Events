package com.zpedroo.voltzevents.managers;

import com.zpedroo.voltzevents.enums.EventPhase;
import com.zpedroo.voltzevents.managers.cache.DataCache;
import com.zpedroo.voltzevents.mysql.DBConnection;
import com.zpedroo.voltzevents.objects.event.SpecialItem;
import com.zpedroo.voltzevents.objects.event.WinnerSettings;
import com.zpedroo.voltzevents.objects.player.EventItems;
import com.zpedroo.voltzevents.objects.player.PlayerData;
import com.zpedroo.voltzevents.types.ArenaEvent;
import com.zpedroo.voltzevents.types.Event;
import com.zpedroo.voltzevents.types.PvPEvent;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.color.Colorize;
import com.zpedroo.voltzevents.utils.region.CuboidRegion;
import com.zpedroo.voltzevents.objects.general.ScoreboardInfo;
import com.zpedroo.voltzevents.utils.serialization.Base64Encoder;
import com.zpedroo.voltzevents.utils.serialization.LocationSerialization;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DataManager {

    private static DataManager instance;
    public static DataManager getInstance() { return instance; }

    private final DataCache dataCache = new DataCache();

    public DataManager() {
        instance = this;
    }

    public PlayerData getPlayerData(Player player) {
        PlayerData data = dataCache.getPlayerData().get(player);
        if (data == null) {
            data = DBConnection.getInstance().getDBManager().getPlayerDataFromDatabase(player);
            dataCache.getPlayerData().put(player, data);
        }

        return data;
    }

    public Event getPlayerParticipatingEvent(Player player) {
        return dataCache.getPlayersParticipating().get(player);
    }

    public SpecialItem getSpecialItem(String identifier) {
        return dataCache.getSpecialItems().get(identifier);
    }

    public List<PlayerData> getTopWins() {
        return dataCache.getTopWins();
    }

    public List<Event> getEvents() {
        return dataCache.getEvents();
    }

    @Nullable
    public Event getEventByName(String eventName) {
        return dataCache.getEvents().stream().filter(event -> StringUtils.equalsIgnoreCase(event.getName(), eventName)).findFirst().orElse(null);
    }

    public CuboidRegion getWinRegionFromFile(String eventName) {
        FileUtils.Files file = FileUtils.Files.LOCATIONS;

        String serializedFirstLocation = FileUtils.get().getString(file, eventName + ".win-region.first-location");
        String serializedSecondLocation = FileUtils.get().getString(file, eventName + ".win-region.second-location");
        Location firstLocation = LocationSerialization.deserialize(serializedFirstLocation);
        Location secondLocation = LocationSerialization.deserialize(serializedSecondLocation);
        if (firstLocation == null || secondLocation == null) return null;

        return new CuboidRegion(firstLocation, secondLocation);
    }

    public EventItems getEventItemsFromFile(String eventName) {
        FileUtils.Files file = FileUtils.Files.ITEMS;
        if (!FileUtils.get().getFile(file).get().contains(eventName)) return null;

        String serializedInventory = FileUtils.get().getString(file, eventName + ".inventory-items", "");
        String serializedArmor = FileUtils.get().getString(file, eventName + ".armor-items", "");

        ItemStack[] inventoryItems = null;
        ItemStack[] armorItems = null;
        try {
            inventoryItems = Base64Encoder.itemStackArrayFromBase64(serializedInventory);
            armorItems = Base64Encoder.itemStackArrayFromBase64(serializedArmor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (inventoryItems == null || armorItems == null) return null;

        return new EventItems(inventoryItems, armorItems);
    }

    public Map<Integer, String> getWinnersFromFile(String eventName) {
        Map<Integer, String> ret = new HashMap<>(4);
        FileUtils.Files file = FileUtils.Files.WINNERS;

        if (FileUtils.get().getFile(file).get().contains(eventName)) {
            for (String positionStr : FileUtils.get().getSection(file, eventName)) {
                String winnerName = FileUtils.get().getString(file, eventName + "." + positionStr);
                int position = Integer.parseInt(positionStr);

                ret.put(position, winnerName);
            }
        }

        return ret;
    }

    public Map<SpecialItem, Integer> getSpecialItemsFromFile(FileUtils.Files file) {
        if (!FileUtils.get().getFile(file).get().contains("Special-Items")) return null;

        Map<SpecialItem, Integer> ret = new HashMap<>(4);
        for (String str : FileUtils.get().getStringList(file, "Special-Items")) {
            String[] split = str.split(":");
            if (split.length < 2) continue;

            SpecialItem specialItem = getSpecialItem(split[0].toUpperCase());
            if (specialItem == null) continue;

            int slot = Integer.parseInt(split[1]);

            ret.put(specialItem, slot);
        }

        return ret;
    }

    public Map<Material, String> getCraftableItemsAndTranslationsFromFile() {
        Map<Material, String> ret = new HashMap<>(4);
        FileUtils.Files file = FileUtils.Files.FASTCRAFT;

        for (String str : FileUtils.get().getStringList(file, "Craftable-Items")) {
            String[] split = str.split(",");
            if (split.length <= 1) continue;

            Material material = Material.getMaterial(split[0]);
            if (material == null) continue;

            String translation = Colorize.getColored(split[1]);
            ret.put(material, translation);
        }

        return ret;
    }

    public Map<EventPhase, ScoreboardInfo> getScoreboardsFromFile(FileUtils.Files file) {
        Map<EventPhase, ScoreboardInfo> ret = new HashMap<>(4);

        if (FileUtils.get().getFile(file).get().contains("Scoreboards")) {
            for (String str : FileUtils.get().getSection(file, "Scoreboards")) {
                String title = Colorize.getColored(FileUtils.get().getString(file, "Scoreboards." + str + ".title"));
                List<String> lines = Colorize.getColored(FileUtils.get().getStringList(file, "Scoreboards." + str + ".lines"));
                EventPhase eventPhase = EventPhase.valueOf(FileUtils.get().getString(file, "Scoreboards." + str + ".display-phase"));

                Collections.reverse(lines);
                ret.put(eventPhase, new ScoreboardInfo(title, lines, eventPhase));
            }
        }

        return ret;
    }

    public Map<Integer, WinnerSettings> getWinnerSettingsFromFile(FileUtils.Files file) {
        Map<Integer, WinnerSettings> ret = new HashMap<>(4);

        Set<String> positions = FileUtils.get().getSection(file, "Winner-Settings");
        if (positions == null) return ret;

        for (String position : positions) {
            int pos = Integer.parseInt(position);

            String display = Colorize.getColored(FileUtils.get().getString(file, "Winner-Settings." + position + ".display"));
            String rewardsDisplay = Colorize.getColored(FileUtils.get().getString(file, "Winner-Settings." + position + ".rewards.display"));
            List<String> winnerMessages = Colorize.getColored(FileUtils.get().getStringList(file, "Winner-Settings." + position + ".winner-messages"));
            List<String> participantsMessages = Colorize.getColored(FileUtils.get().getStringList(file, "Winner-Settings." + position + ".participants-messages"));
            List<String> rewardsCommands = FileUtils.get().getStringList(file, "Winner-Settings." + position + ".rewards.commands");
            int hostRewardsPercentage = FileUtils.get().getInt(file, "Winner-Settings." + position + ".rewards.host-percentage");

            ret.put(pos, new WinnerSettings(display, rewardsDisplay, winnerMessages, participantsMessages, rewardsCommands, hostRewardsPercentage));
        }

        return ret;
    }

    public void savePlayerData(Player player) {
        PlayerData data = dataCache.getPlayerData().get(player);
        if (data == null || !data.isUpdate()) return;

        DBConnection.getInstance().getDBManager().savePlayerData(data);
        data.setUpdate(false);
    }

    public void saveAllPlayersData() {
        dataCache.getPlayerData().keySet().forEach(this::savePlayerData);
    }

    public void cancelAllEvents() {
        dataCache.getEvents().forEach(Event::cancelEvent);
    }

    /*
    public void saveAllLocationsInFile() {
        dataCache.getEvents().forEach(this::saveLocationsInFile);
    }
     */

    public void saveItemsInFile(Event event) {
        if (event.getEventItems() == null) return;

        EventItems eventItems = event.getEventItems();
        writeInFile(FileUtils.Files.ITEMS, event.getName() + ".inventory-items", Base64Encoder.itemStackArrayToBase64(eventItems.getInventoryItems()));
        writeInFile(FileUtils.Files.ITEMS, event.getName() + ".armor-items", Base64Encoder.itemStackArrayToBase64(eventItems.getArmorItems()));
    }

    public void saveAllEventItemsInFile() {
        dataCache.getEvents().forEach(this::saveItemsInFile);
    }

    public void saveWinnersInFile(Event event) {
        writeInFile(FileUtils.Files.WINNERS, event.getName(), null); // reset old winners

        for (Map.Entry<Integer, String> entry : event.getWinnersPosition().entrySet()) {
            String winnerName = entry.getValue();
            int position = entry.getKey();

            writeInFile(FileUtils.Files.WINNERS, event.getName() + "." + position, winnerName);
        }
    }

    public void saveAllWinnersInFile() {
        dataCache.getEvents().forEach(this::saveWinnersInFile);
    }

    public void saveLocationsInFile(Event event) {
        writeInFile(FileUtils.Files.LOCATIONS, event.getName() + ".join", LocationSerialization.serialize(event.getJoinLocation()));
        writeInFile(FileUtils.Files.LOCATIONS, event.getName() + ".exit", LocationSerialization.serialize(event.getExitLocation()));
    }

    public void saveLocationsInFile(PvPEvent event) {
        saveLocationsInFile((Event) event);

        writeInFile(FileUtils.Files.LOCATIONS, event.getName() + ".pos1", LocationSerialization.serialize(event.getPos1Location()));
        writeInFile(FileUtils.Files.LOCATIONS, event.getName() + ".pos2", LocationSerialization.serialize(event.getPos2Location()));
    }

    public void saveLocationsInFile(ArenaEvent event) {
        saveLocationsInFile((Event) event);

        writeInFile(FileUtils.Files.LOCATIONS, event.getName() + ".arena", LocationSerialization.serialize(event.getArenaLocation()));

        CuboidRegion winRegion = event.getWinRegion();
        if (winRegion != null) {
            writeInFile(FileUtils.Files.LOCATIONS, event.getName() + ".win-region.first-location", LocationSerialization.serialize(winRegion.getFirstLocation()));
            writeInFile(FileUtils.Files.LOCATIONS, event.getName() + ".win-region.second-location", LocationSerialization.serialize(winRegion.getSecondLocation()));
        }
    }

     public void setPlayerParticipatingEvent(Player player, Event event) {
         if (event == null) {
            dataCache.getPlayersParticipating().remove(player);
        } else {
            dataCache.getPlayersParticipating().put(player, event);
        }
     }

     public void updateTopWins() {
        dataCache.setTopWins(DBConnection.getInstance().getDBManager().getTopWinsFromDatabase());
     }

     private void writeInFile(FileUtils.Files file, String path, Object value) {
         FileUtils.FileManager fileManager = FileUtils.get().getFile(file);
         fileManager.get().set(path, value);
         fileManager.save();
     }

    public DataCache getCache() {
        return dataCache;
    }
}