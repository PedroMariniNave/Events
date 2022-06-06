package com.zpedroo.voltzevents.managers.cache;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.mysql.DBConnection;
import com.zpedroo.voltzevents.objects.event.SpecialItem;
import com.zpedroo.voltzevents.objects.player.PlayerData;
import com.zpedroo.voltzevents.types.Event;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.builder.ItemBuilder;
import com.zpedroo.voltzevents.utils.color.Colorize;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class DataCache {

    private final Map<Player, PlayerData> playerData = new HashMap<>(64);
    private final Map<Player, Event> playersParticipating = new HashMap<>(32);
    private final Map<String, SpecialItem> specialItems = getSpecialItemsFromConfig();
    private final Map<Integer, String[]> countdownTitles = getCountdownTitlesFromConfig();
    private List<PlayerData> topWins = null;
    private final List<Event> events = new ArrayList<>(8);

    public DataCache() {
        VoltzEvents.get().getServer().getScheduler().runTaskLaterAsynchronously(VoltzEvents.get(), () -> topWins = DBConnection.getInstance().getDBManager().getTopWinsFromDatabase(), 40L);
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

    public void setTopWins(List<PlayerData> topWins) {
        this.topWins = topWins;
    }
}