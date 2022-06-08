package com.zpedroo.voltzevents.events.race;

import com.zpedroo.voltzevents.commands.ArenaEventCmd;
import com.zpedroo.voltzevents.enums.EventPhase;
import com.zpedroo.voltzevents.enums.LeaveReason;
import com.zpedroo.voltzevents.managers.CommandManager;
import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.objects.player.EventItems;
import com.zpedroo.voltzevents.tasks.AnnounceTask;
import com.zpedroo.voltzevents.tasks.WinnerRegionCheckTask;
import com.zpedroo.voltzevents.types.ArenaEvent;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.color.Colorize;
import com.zpedroo.voltzevents.utils.region.CuboidRegion;
import com.zpedroo.voltzevents.utils.serialization.LocationSerialization;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zpedroo.voltzevents.events.race.RaceEvent.Locations.*;
import static com.zpedroo.voltzevents.events.race.RaceEvent.Messages.*;
import static com.zpedroo.voltzevents.events.race.RaceEvent.Settings.*;

public class RaceEvent extends ArenaEvent {

    private static RaceEvent instance;
    public static RaceEvent getInstance() { return instance; }

    private CuboidRegion winRegion = DataManager.getInstance().getWinRegionFromFile("Race");

    public RaceEvent(Plugin plugin) {
        super("Race", FileUtils.Files.RACE, WHITELISTED_COMMANDS, TAG, new HashMap<String, List<String>>() {{
            put("STARTING", EVENT_STARTING);
            put("STARTED", EVENT_STARTED);
            put("CANCELLED", EVENT_CANCELLED);
            put("FINISHED", EVENT_FINISHED);
            put("INSUFFICIENT_PLAYERS", INSUFFICIENT_PLAYERS);
        }}, WINNERS, WINNERS_AMOUNT, MINIMUM_PLAYERS_TO_START, MINIMUM_PLAYERS_AFTER_START, SAVE_PLAYER_INVENTORY, ADDITIONAL_VOID_CHECKER, EVENT_ITEMS, JOIN_LOCATION, EXIT_LOCATION, ARENA_LOCATION);

        instance = this;
        setAnnounceTask(new AnnounceTask(plugin, this, ANNOUNCES_DELAY, ANNOUNCES_AMOUNT));
        CommandManager.registerCommand(plugin, COMMAND, ALIASES, new ArenaEventCmd(this));
        DataManager.getInstance().getCache().getEvents().add(this);
    }

    @Override
    public void checkIfPlayerIsWinner(Player player, int participantsAmount) {
        if (participantsAmount > WINNERS_AMOUNT) return;

        int position = 1;
        for (int pos = 1; pos <= WINNERS_AMOUNT; ++pos) {
            if (!hasWinner(pos)) {
                position = pos;
                break;
            }
        }

        winEvent(player, position);
        leave(player, LeaveReason.WINNER);

        if (position == WINNERS_AMOUNT || getPlayersParticipatingAmount() <= MINIMUM_PLAYERS_AFTER_START) {
            finishEvent(true);
        }
    }

    @Override
    public void startEventMethods() {
        setEventPhase(EventPhase.STARTED);
    }

    @Override
    public void teleportPlayersToArenaAndExecuteEventActions() {
        getPlayersParticipating().forEach(player -> {
            player.teleport(getArenaLocation());
            new WinnerRegionCheckTask(this, player);
        });
    }

    @Override
    public void resetAllValues() {
    }

    @Override
    public CuboidRegion getWinRegion() {
        return winRegion;
    }

    @Override
    public void setWinRegion(CuboidRegion winRegion) {
        this.winRegion = winRegion;
        DataManager.getInstance().saveLocationsInFile(this);
    }

    protected static class Locations {

        public static final Location JOIN_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "Race.join", null));

        public static final Location EXIT_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "Race.exit", null));

        public static final Location ARENA_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "Race.arena", null));
    }

    public static class Settings {

        public static final String COMMAND = FileUtils.get().getString(FileUtils.Files.RACE, "Settings.command");

        public static final List<String> ALIASES = FileUtils.get().getStringList(FileUtils.Files.RACE, "Settings.aliases");

        public static final String TAG = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.RACE, "Settings.tag"));

        public static final Map<Integer, String> WINNERS = DataManager.getInstance().getWinnersFromFile("Race");

        public static final EventItems EVENT_ITEMS = DataManager.getInstance().getEventItemsFromFile("Race");

        public static final int WINNERS_AMOUNT = FileUtils.get().getInt(FileUtils.Files.RACE, "Settings.winners-amount", 1);

        public static final int MINIMUM_PLAYERS_TO_START = FileUtils.get().getInt(FileUtils.Files.RACE, "Settings.minimum-players.to-start");

        public static final int MINIMUM_PLAYERS_AFTER_START = FileUtils.get().getInt(FileUtils.Files.RACE, "Settings.minimum-players.after-start");

        public static final int ANNOUNCES_DELAY = FileUtils.get().getInt(FileUtils.Files.RACE, "Settings.announces-delay");

        public static final int ANNOUNCES_AMOUNT = FileUtils.get().getInt(FileUtils.Files.RACE, "Settings.announces-amount");

        public static final boolean SAVE_PLAYER_INVENTORY = FileUtils.get().getBoolean(FileUtils.Files.RACE, "Settings.save-player-inventory");

        public static final boolean ADDITIONAL_VOID_CHECKER = FileUtils.get().getBoolean(FileUtils.Files.RACE, "Settings.additional-void-checker");

        public static final List<String> WHITELISTED_COMMANDS = FileUtils.get().getStringList(FileUtils.Files.RACE, "Whitelisted-Commands");
    }

    public static class Messages {

        public static final List<String> EVENT_STARTING = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.RACE, "Messages.event-starting"));

        public static final List<String> EVENT_STARTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.RACE, "Messages.event-started"));

        public static final List<String> EVENT_CANCELLED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.RACE, "Messages.event-cancelled"));

        public static final List<String> EVENT_FINISHED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.RACE, "Messages.event-finished"));

        public static final List<String> INSUFFICIENT_PLAYERS = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.RACE, "Messages.insufficient-players"));
    }
}