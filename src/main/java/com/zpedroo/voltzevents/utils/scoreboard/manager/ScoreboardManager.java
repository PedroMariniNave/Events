package com.zpedroo.voltzevents.utils.scoreboard.manager;

import com.zpedroo.voltzevents.types.Event;
import com.zpedroo.voltzevents.utils.scoreboard.ScoreboardHandler;
import com.zpedroo.voltzevents.utils.scoreboard.objects.Scoreboard;
import com.zpedroo.voltzevents.utils.scoreboard.tasks.ScoreboardUpdater;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreboardManager {

    private static final Map<Player, ScoreboardHandler> scoreboards = new HashMap<>(32);

    public static void setScoreboard(Player player, Event event) {
        Scoreboard scoreboard = event.getScoreboard();
        if (scoreboard == null) return;

        setScoreboard(player, event, scoreboard);
    }

    public static void setScoreboard(Player player, Event event, Scoreboard scoreboard) {
        ScoreboardHandler scoreboardHandler = new ScoreboardHandler(player);
        scoreboards.put(player, scoreboardHandler);

        updateScoreboard(player, scoreboard, scoreboardHandler);

        ScoreboardUpdater scoreboardUpdater = new ScoreboardUpdater(event, scoreboard, scoreboardHandler);
        scoreboardUpdater.startTask();
    }

    public static void updateScoreboard(Player player, Scoreboard scoreboard, ScoreboardHandler scoreboardHandler) {
        if (scoreboardHandler.isDeleted()) return;

        String replacedTitle = replacePlaceholders(player, scoreboard.getTitle());
        List<String> replacedLines = replacePlaceholders(player, scoreboard.getLines());

        scoreboardHandler.updateTitle(replacedTitle);
        scoreboardHandler.updateLines(replacedLines);
    }

    public static void removeScoreboard(Player player) {
        ScoreboardHandler scoreboard = scoreboards.remove(player);
        if (scoreboard != null) {
            scoreboard.delete();
        }
    }

    private static String replacePlaceholders(Player player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    private static List<String> replacePlaceholders(Player player, List<String> list) {
        List<String> ret = new ArrayList<>(list.size());
        for (String str : list) {
            ret.add(replacePlaceholders(player, str));
        }

        return ret;
    }
}