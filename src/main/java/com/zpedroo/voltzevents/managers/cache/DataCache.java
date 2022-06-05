package com.zpedroo.voltzevents.managers.cache;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.mysql.DBConnection;
import com.zpedroo.voltzevents.objects.SpecialItem;
import com.zpedroo.voltzevents.objects.WinnerSettings;
import com.zpedroo.voltzevents.types.Event;
import com.zpedroo.voltzevents.objects.PlayerData;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.builder.ItemBuilder;
import com.zpedroo.voltzevents.utils.color.Colorize;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
public class DataCache {

    private Table<Event, Integer, WinnerSettings> winnerSettings = null;
    private final Map<Player, PlayerData> playerData = new HashMap<>(64);
    private final Map<Player, Event> playersParticipating = new HashMap<>(32);
    private final Map<String, SpecialItem> specialItems = getSpecialItemsFromConfig();
    private final Map<Integer, String[]> countdownTitles = getCountdownTitlesFromConfig();
    private List<PlayerData> topWins = null;
    private final List<Event> events = new ArrayList<>(8);

    public DataCache() {
        VoltzEvents.get().getServer().getScheduler().runTaskLaterAsynchronously(VoltzEvents.get(), () -> winnerSettings = getAllEventsWinnerSettings(), 100L);
        VoltzEvents.get().getServer().getScheduler().runTaskLaterAsynchronously(VoltzEvents.get(), () -> topWins = DBConnection.getInstance().getDBManager().getTopWinsFromDatabase(), 100L);
    }

    private Map<String, SpecialItem> getSpecialItemsFromConfig() {
        FileUtils.Files file = FileUtils.Files.CONFIG;
        FileConfiguration fileConfiguration = FileUtils.get().getFile(file).get();
        Map<String, SpecialItem> ret = new HashMap<>(8);

        for (String name : FileUtils.get().getSection(file, "Special-Items")) {
            String identifier = name.toUpperCase();
            String action = FileUtils.get().getString(file, "Special-Items." + name + ".action");
            ItemStack defaultItem = null;
            ItemStack secondaryItem = null;
            if (fileConfiguration.contains("Special-Items." + name + ".default-item.type")) {
                defaultItem = ItemBuilder.build(fileConfiguration, "Special-Items." + name + ".default-item").build();
            } else {
                defaultItem = ItemBuilder.build(fileConfiguration, "Special-Items." + name).build();
            }

            if (fileConfiguration.contains("Special-Items." + name + ".secondary-item.type")) {
                secondaryItem = ItemBuilder.build(fileConfiguration, "Special-Items." + name + ".secondary-item").build();
            }

            ret.put(identifier, new SpecialItem(identifier, action, defaultItem, secondaryItem));
        }

        return ret;
    }

    private Map<Integer, String[]> getCountdownTitlesFromConfig() {
        FileUtils.Files file = FileUtils.Files.CONFIG;
        Map<Integer, String[]> ret = new HashMap<>(8);

        for (String str : FileUtils.get().getSection(file, "Countdown-Titles")) {
            int countdownNumber = Integer.parseInt(str);
            String title = Colorize.getColored(FileUtils.get().getString(file, "Countdown-Titles." + str + ".title"));
            String subtitle = Colorize.getColored(FileUtils.get().getString(file, "Countdown-Titles." + str + ".subtitle"));

            ret.put(countdownNumber, new String[] { title, subtitle });
        }

        return ret;
    }

    private Table<Event, Integer, WinnerSettings> getAllEventsWinnerSettings() {
        HashBasedTable<Event, Integer, WinnerSettings> ret = HashBasedTable.create();

        for (Event event : events) {
            FileUtils.Files file = event.getFile();
            Set<String> positions = FileUtils.get().getSection(file, "Winner-Settings");
            if (positions == null) continue;

            for (String position : positions) {
                int pos = Integer.parseInt(position);

                String display = Colorize.getColored(FileUtils.get().getString(file, "Winner-Settings." + position + ".display"));
                List<String> winnerMessages = Colorize.getColored(FileUtils.get().getStringList(file, "Winner-Settings." + position + ".winner-messages"));
                List<String> participantsMessages = Colorize.getColored(FileUtils.get().getStringList(file, "Winner-Settings." + position + ".participants-messages"));
                List<String> rewards = FileUtils.get().getStringList(file, "Winner-Settings." + position + ".rewards");

                ret.put(event, pos, new WinnerSettings(display, winnerMessages, participantsMessages, rewards));
            }
        }

        return ret;
    }

    public void setTopWins(List<PlayerData> topWins) {
        this.topWins = topWins;
    }
}