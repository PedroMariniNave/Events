package com.zpedroo.voltzevents.utils.scoreboard.tasks;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.types.Event;
import com.zpedroo.voltzevents.utils.scoreboard.ScoreboardHandler;
import com.zpedroo.voltzevents.utils.scoreboard.manager.ScoreboardManager;
import com.zpedroo.voltzevents.utils.scoreboard.objects.Scoreboard;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ScoreboardUpdater extends BukkitRunnable {

    private final Event event;
    private final Scoreboard scoreboard;
    private final ScoreboardHandler scoreboardHandler;
    private final Player player;

    public ScoreboardUpdater(Event event, Scoreboard scoreboard, ScoreboardHandler scoreboardHandler) {
        this.event = event;
        this.scoreboard = scoreboard;
        this.scoreboardHandler = scoreboardHandler;
        this.player = scoreboardHandler.getPlayer();
    }

    @Override
    public void run() {
        if (player == null || !player.isOnline() || event.getEventPhase() != scoreboard.getDisplayPhase()) {
            scoreboardHandler.delete();

            Scoreboard newScoreboard = event.getScoreboard();
            if (newScoreboard != null) {
                ScoreboardManager.setScoreboard(player, event, newScoreboard);
            }
            this.cancel();
            return;
        }

        ScoreboardManager.updateScoreboard(player, scoreboard, scoreboardHandler);
    }

    public void startTask() {
        this.runTaskTimerAsynchronously(VoltzEvents.get(), scoreboard.getUpdateTime(), scoreboard.getUpdateTime());
    }
}