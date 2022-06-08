package com.zpedroo.voltzevents.hooks;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.events.hotpotato.HotPotatoEvent;
import com.zpedroo.voltzevents.types.Event;
import com.zpedroo.voltzevents.types.PvPEvent;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final Plugin plugin = VoltzEvents.get();
    private final Event event;

    public PlaceholderAPIHook(Event event) {
        this.event = event;
        this.register();
    }

    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    public String getIdentifier() {
        return event.getName();
    }

    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    public String onPlaceholderRequest(Player player, String identifier) {
        if (StringUtils.containsIgnoreCase(identifier, "WINNER")) {
            String[] split = identifier.split(":");
            if (split.length <= 1) return null;

            int position = Integer.parseInt(split[1]);
            return event.getWinnerName(position);
        }

        Player target = null;
        HotPotatoEvent hotPotatoEvent = null;
        switch (identifier.toUpperCase()) {
            case "PLAYERS":
                return String.valueOf(event.getPlayersParticipatingAmount());
            case "PLAYER1":
                if (!(event instanceof PvPEvent)) break;

                target = ((PvPEvent) event).getPlayer1();
                return target == null ? "???" : target.getName();
            case "PLAYER2":
                if (!(event instanceof PvPEvent)) break;

                target = ((PvPEvent) event).getPlayer2();
                return target == null ? "???" : target.getName();
            case "KILLS":
                return String.valueOf(event.getPlayerKills(player));
            case "ROUND":
                if (!(event instanceof HotPotatoEvent)) break;

                hotPotatoEvent = (HotPotatoEvent) event;
                return String.valueOf(hotPotatoEvent.getRound());
            case "TIME":
                return event.getFormattedTime();
            case "WARMUP_TIME":
                if (event.getWarmupTask() == null) break;

                return String.valueOf(event.getWarmupTask().getTimeLeft());
            case "BURN_TIME":
                if (!(event instanceof HotPotatoEvent)) break;

                hotPotatoEvent = (HotPotatoEvent) event;
                return String.valueOf(hotPotatoEvent.getBurnTimer());
            case "NEW_ROUND_TIME":
                if (!(event instanceof HotPotatoEvent)) break;

                hotPotatoEvent = (HotPotatoEvent) event;
                return String.valueOf(hotPotatoEvent.getNewRoundTimer());
        }

        return null;
    }
}