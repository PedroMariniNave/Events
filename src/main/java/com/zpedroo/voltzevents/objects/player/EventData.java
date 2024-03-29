package com.zpedroo.voltzevents.objects.player;

import com.zpedroo.voltzevents.objects.event.WinnerData;
import com.zpedroo.voltzevents.utils.formatter.TimeFormatter;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@Getter
public class EventData {

    private long startTimestamp = -1;
    private final Map<Integer, WinnerData> winnersData = new HashMap<>(4);
    private final Map<Player, Integer> playerKills = new HashMap<>(16);
    private final Map<Player, Player> playerLastDamager = new HashMap<>(16);

    public WinnerData getWinnerData(int position) {
        return winnersData.get(position);
    }

    public Player getLastDamager(Player player) {
        return playerLastDamager.get(player);
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
        this.winnersData.put(position, new WinnerData(winnerName, position, kills, winTimestamp));
    }

    public void setPlayerLastDamager(Player player, Player damager) {
        this.playerLastDamager.put(player, damager);
    }

    public void addPlayerKills(Player player, int kills) {
        this.setPlayerKills(player, getPlayerKills(player) + kills);
    }

    public void setPlayerKills(Player player, int kills) {
        this.playerKills.put(player, kills);
    }
}