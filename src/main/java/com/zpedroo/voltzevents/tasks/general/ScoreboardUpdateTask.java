package com.zpedroo.voltzevents.tasks.general;

import com.zpedroo.scoreboard.utils.placeholderapi.PlaceholderUtils;
import com.zpedroo.voltzevents.utils.scoreboard.ScoreboardLine;
import com.zpedroo.voltzevents.utils.scoreboard.ChatColorService;
import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.objects.general.ScoreboardInfo;
import com.zpedroo.voltzevents.types.Event;
import com.zpedroo.voltzevents.utils.scoreboard.ScoreboardUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.zpedroo.voltzevents.utils.config.Settings.SCOREBOARD_UPDATE_INTERVAL;

public class ScoreboardUpdateTask extends BukkitRunnable {

    private final Plugin plugin = VoltzEvents.get();
    private final Player player;
    private final Event event;
    private Scoreboard scoreboard;
    private ScoreboardInfo scoreboardInfo;

    public ScoreboardUpdateTask(Player player, Event event) {
        this.player = player;
        this.event = event;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            ScoreboardUtils.removePlayerScoreboard(player);
            return;
        }

        if (scoreboard == null) {
            createScoreboard();
        }

        updateScoreboard();
        setPlayerScoreboard();
    }

    private void updateScoreboard() {
        updateTitle();
        updateLines();
    }

    private void updateTitle() {
        if (scoreboard == null) return;

        Objective objective = getScoreboardObjective();
        if (objective.getDisplayName().equals(getScoreboardInfo().getTitle())) return;

        objective.setDisplayName(getScoreboardInfo().getTitle());
    }

    private void updateLines() {
        if (scoreboard == null) return;

        Objective objective = getScoreboardObjective();
        ScoreboardInfo newScoreboardInfo = getScoreboardInfo();
        if (newScoreboardInfo != scoreboardInfo) {
            scoreboardInfo = newScoreboardInfo;
            scoreboard.getEntries().forEach(entry -> scoreboard.resetScores(entry));
        }

        List<String> lines = scoreboardInfo.getLines();
        for (int score = 0; score < lines.size(); ++score) {
            String line = PlaceholderUtils.replace(player, lines.get(score));
            String teamName = ChatColorService.getRandomColors(score) + "" + ChatColor.RESET;

            Team team = scoreboard.getTeam(teamName);
            if (team == null) team = scoreboard.registerNewTeam(teamName);
            if (!team.hasEntry(teamName)) team.addEntry(teamName);

            ScoreboardLine scoreboardLine = new ScoreboardLine(line, score);
            if (!team.getPrefix().equals(scoreboardLine.getPrefix())) team.setPrefix(scoreboardLine.getPrefix());
            if (!team.getSuffix().equals(scoreboardLine.getSuffix())) team.setSuffix(scoreboardLine.getSuffix());

            if (!objective.getScore(teamName).isScoreSet() || objective.getScore(teamName).getScore() != score) {
                objective.getScore(teamName).setScore(score);
            }
        }
    }

    private void createScoreboard() {
        Future<Scoreboard> scoreboardFuture = Bukkit.getScheduler().callSyncMethod(plugin, () -> Bukkit.getScoreboardManager().getNewScoreboard());
        try {
            scoreboard = scoreboardFuture.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            throw new RuntimeException("Failed to update scoreboard", ex);
        }

        Objective objective = scoreboard.registerNewObjective("EventsScoreboard", "dummy");
        objective.setDisplayName(getScoreboardInfo().getTitle());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    private void setPlayerScoreboard() {
        if (isSameScoreboard(player.getScoreboard(), scoreboard)) return;

        player.setScoreboard(scoreboard);
    }
    
    private ScoreboardInfo getScoreboardInfo() {
        return event.getScoreboard();
    }

    private Objective getScoreboardObjective() {
        return scoreboard.getObjective("EventsScoreboard");
    }

    private boolean isSameScoreboard(Scoreboard scoreboard, Scoreboard scoreboardToCompare) {
        return scoreboard.equals(scoreboardToCompare);
    }

    public void start() {
        this.runTaskTimerAsynchronously(plugin, 0L, SCOREBOARD_UPDATE_INTERVAL);
    }
}