package com.zpedroo.voltzevents.utils.scoreboard;

import com.zpedroo.voltzevents.tasks.general.ScoreboardUpdateTask;
import com.zpedroo.voltzevents.types.Event;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardUtils {

    private static final Map<Player, ScoreboardUpdateTask> playersTask = new HashMap<>(32);

    public static void registerPlayerScoreboard(Player player, Event event) {
        ScoreboardUpdateTask task = new ScoreboardUpdateTask(player, event);
        task.start();
        playersTask.put(player, task);
    }

    public static void removePlayerScoreboard(Player player) {
        ScoreboardUpdateTask activeTask = playersTask.remove(player);
        if (activeTask == null) return;

        activeTask.cancel();
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}