package com.zpedroo.voltzevents.objects;

import com.zpedroo.voltzevents.utils.formatter.TimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class EventData {

    private long startTimestamp = -1;
    private final Map<Integer, WinnerData> winnersData = new HashMap<>(4);
    private final Map<Player, Integer> playerKills = new HashMap<>(16);

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public Map<Integer, WinnerData> getWinnersData() {
        return winnersData;
    }

    public Map<Player, Integer> getPlayerKills() {
        return playerKills;
    }

    public WinnerData getWinnerData(int position) {
        return winnersData.get(position);
    }

    public String getFormattedStartTimestamp() {
        if (startTimestamp == -1) return "-/-";

        long elapsedTime = System.currentTimeMillis() - startTimestamp;
        return TimeFormatter.format(elapsedTime);
    }

    public int getPlayerKills(Player player) {
        return playerKills.getOrDefault(player, 0);
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public void setWinnerData(Player player, int position, long winTimestamp) {
        this.setWinnerData(player.getName(), position, getPlayerKills(player), winTimestamp);
    }

    public void setWinnerData(Player player, int position, int kills, long winTimestamp) {
        this.setWinnerData(player.getName(), position, kills, winTimestamp);
    }

    public void setWinnerData(String winnerName, int position, long winTimestamp) {
        this.setWinnerData(winnerName, position, getPlayerKills(Bukkit.getPlayer(winnerName)), winTimestamp);
    }

    public void setWinnerData(String winnerName, int position, int kills, long winTimestamp) {
        winnersData.put(position, new WinnerData(winnerName, position, kills, winTimestamp));
    }

    public void addPlayerKills(Player player, int kills) {
        this.setPlayerKills(player, getPlayerKills(player) + kills);
    }

    public void setPlayerKills(Player player, int kills) {
        this.playerKills.put(player, kills);
    }
}