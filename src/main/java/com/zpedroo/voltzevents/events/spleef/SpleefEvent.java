package com.zpedroo.voltzevents.events.spleef;

import com.zpedroo.voltzevents.commands.ArenaEventCmd;
import com.zpedroo.voltzevents.enums.LeaveReason;
import com.zpedroo.voltzevents.events.spleef.listeners.SpleefListeners;
import com.zpedroo.voltzevents.events.spleef.tasks.PlayerCheckTask;
import com.zpedroo.voltzevents.managers.CommandManager;
import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.managers.ListenerManager;
import com.zpedroo.voltzevents.objects.player.EventItems;
import com.zpedroo.voltzevents.tasks.AnnounceTask;
import com.zpedroo.voltzevents.types.ArenaEvent;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.color.Colorize;
import com.zpedroo.voltzevents.utils.region.CuboidRegion;
import com.zpedroo.voltzevents.utils.serialization.LocationSerialization;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zpedroo.voltzevents.events.spleef.SpleefEvent.Messages.*;
import static com.zpedroo.voltzevents.events.spleef.SpleefEvent.Locations.*;
import static com.zpedroo.voltzevents.events.spleef.SpleefEvent.Settings.*;

public class SpleefEvent extends ArenaEvent {

    private static SpleefEvent instance;
    public static SpleefEvent getInstance() { return instance; }

    public SpleefEvent(Plugin plugin) {
        super("Spleef", FileUtils.Files.SPLEEF, TAG, new HashMap<String, List<String>>() {{
            put("STARTING", EVENT_STARTING);
            put("STARTED", EVENT_STARTED);
            put("CANCELLED", EVENT_CANCELLED);
            put("FINISHED", EVENT_FINISHED);
            put("INSUFFICIENT_PLAYERS", INSUFFICIENT_PLAYERS);
        }}, WINNERS, WINNERS_AMOUNT, MINIMUM_PLAYERS, SAVE_PLAYER_INVENTORY, ADDITIONAL_VOID_CHECKER, EVENT_ITEMS, JOIN_LOCATION, EXIT_LOCATION, ARENA_LOCATION);

        instance = this;
        setAnnounceTask(new AnnounceTask(plugin, this, ANNOUNCES_DELAY, ANNOUNCES_AMOUNT));
        ListenerManager.registerListener(plugin, new SpleefListeners());
        CommandManager.registerCommand(plugin, COMMAND, ALIASES, new ArenaEventCmd(this));
        DataManager.getInstance().getCache().getEvents().add(this);
    }

    @Override
    public void checkIfPlayerIsWinner(Player player, int participantsAmount) {
        if (participantsAmount > WINNERS_AMOUNT) return;

        int position = participantsAmount;
        winEvent(player, position);

        if (getPlayersParticipatingAmount() <= 1) {
            Player winner = getPlayersParticipating().size() == 1 ? getPlayersParticipating().get(0) : null;
            if (winner != null) leave(winner, LeaveReason.WINNER, false);
            finishEvent(true);
        }
    }

    @Override
    public void startEventMethods() {
        // new WarmupTask(this, WARMUP_BAR, WARMUP_DURATION);
    }

    public void selectPlayersAndExecuteEventActions() {
    }

    @Override
    public void teleportPlayersToArenaAndExecuteEventActions() {
        getPlayersParticipating().forEach(player -> {
            player.teleport(getArenaLocation());
            new PlayerCheckTask(player);
        });
    }

    @Override
    public void resetAllValues() {
    }

    @Override
    public CuboidRegion getWinRegion() {
        return null;
    }

    @Override
    public void setWinRegion(CuboidRegion winRegion) {
    }

    protected static class Locations {

        public static final Location JOIN_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "Spleef.join", null));

        public static final Location EXIT_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "Spleef.exit", null));

        public static final Location ARENA_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "Spleef.arena", null));
    }

    public static class Settings {

        public static final String COMMAND = FileUtils.get().getString(FileUtils.Files.SPLEEF, "Settings.command");

        public static final List<String> ALIASES = FileUtils.get().getStringList(FileUtils.Files.SPLEEF, "Settings.aliases");

        public static final String TAG = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.SPLEEF, "Settings.tag"));

        public static final Map<Integer, String> WINNERS = DataManager.getInstance().getWinnersFromFile("Spleef");

        public static final EventItems EVENT_ITEMS = DataManager.getInstance().getEventItemsFromFile("Spleef");

        public static final int WINNERS_AMOUNT = FileUtils.get().getInt(FileUtils.Files.SPLEEF, "Settings.winners-amount", 1);

        public static final int MINIMUM_PLAYERS = FileUtils.get().getInt(FileUtils.Files.SPLEEF, "Settings.minimum-players");

        public static final int ANNOUNCES_DELAY = FileUtils.get().getInt(FileUtils.Files.SPLEEF, "Settings.announces-delay");

        public static final int ANNOUNCES_AMOUNT = FileUtils.get().getInt(FileUtils.Files.SPLEEF, "Settings.announces-amount");

        public static final boolean SAVE_PLAYER_INVENTORY = FileUtils.get().getBoolean(FileUtils.Files.SPLEEF, "Settings.save-player-inventory");

        public static final boolean ADDITIONAL_VOID_CHECKER = FileUtils.get().getBoolean(FileUtils.Files.SPLEEF, "Settings.additional-void-checker");
    }

    public static class Messages {

        public static final List<String> EVENT_STARTING = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.SPLEEF, "Messages.event-starting"));

        public static final List<String> EVENT_STARTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.SPLEEF, "Messages.event-started"));

        public static final List<String> EVENT_CANCELLED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.SPLEEF, "Messages.event-cancelled"));

        public static final List<String> EVENT_FINISHED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.SPLEEF, "Messages.event-finished"));

        public static final List<String> INSUFFICIENT_PLAYERS = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.SPLEEF, "Messages.insufficient-players"));
    }

    public static class Titles {

        public static final String[] ELIMINATED = new String[]{
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.SPLEEF, "Titles.eliminated.title")),
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.SPLEEF, "Titles.eliminated.subtitle"))
        };
    }
}