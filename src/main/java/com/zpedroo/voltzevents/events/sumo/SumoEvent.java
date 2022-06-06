package com.zpedroo.voltzevents.events.sumo;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.commands.PvPEventCmd;
import com.zpedroo.voltzevents.enums.EventPhase;
import com.zpedroo.voltzevents.events.sumo.listeners.SumoListeners;
import com.zpedroo.voltzevents.events.sumo.tasks.PlayerCheckTask;
import com.zpedroo.voltzevents.managers.CommandManager;
import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.managers.ListenerManager;
import com.zpedroo.voltzevents.objects.player.EventItems;
import com.zpedroo.voltzevents.tasks.AnnounceTask;
import com.zpedroo.voltzevents.types.PvPEvent;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.color.Colorize;
import com.zpedroo.voltzevents.utils.serialization.LocationSerialization;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zpedroo.voltzevents.events.sumo.SumoEvent.Messages.*;
import static com.zpedroo.voltzevents.events.sumo.SumoEvent.Locations.*;
import static com.zpedroo.voltzevents.events.sumo.SumoEvent.Settings.*;

public class SumoEvent extends PvPEvent {

    private static SumoEvent instance;
    public static SumoEvent getInstance() { return instance; }

    public SumoEvent(Plugin plugin) {
        super("Sumo", FileUtils.Files.SUMO, TAG, new HashMap<String, List<String>>() {{
            put("STARTING", EVENT_STARTING);
            put("STARTED", EVENT_STARTED);
            put("CANCELLED", EVENT_CANCELLED);
            put("FINISHED", EVENT_FINISHED);
            put("INSUFFICIENT_PLAYERS", INSUFFICIENT_PLAYERS);
        }}, WINNERS, WINNERS_AMOUNT, MINIMUM_PLAYERS, SAVE_PLAYER_INVENTORY, ADDITIONAL_VOID_CHECKER, EVENT_ITEMS, JOIN_LOCATION, EXIT_LOCATION, POS1_LOCATION, POS2_LOCATION);

        instance = this;
        setAnnounceTask(new AnnounceTask(plugin, this, ANNOUNCES_DELAY, ANNOUNCES_AMOUNT));
        ListenerManager.registerListener(plugin, new SumoListeners());
        CommandManager.registerCommand(plugin, COMMAND, ALIASES, new PvPEventCmd(this));
        DataManager.getInstance().getCache().getEvents().add(this);
    }

    @Override
    public void checkWinner(Player player) {
        if (getPlayersParticipatingAmount() > WINNERS_AMOUNT) return;

        int position = getPlayersParticipatingAmount();
        winEvent(player, position, true);

        if (getPlayersParticipatingAmount() <= 1) {
            Player winner = getPlayersParticipating().size() == 1 ? getPlayersParticipating().get(0) : null;
            if (winner != null) leave(winner, false, true);
            finishEvent(true);
        }
    }

    @Override
    public void startEventMethods() {
        this.setEventPhase(EventPhase.STARTED);
        this.selectPlayersAndExecuteEventActions();
    }

    public void selectPlayersAndExecuteEventActions() {
        setEventPhase(EventPhase.STARTED);

        Player player1 = getPlayersParticipating().stream().findAny().get();
        Player player2 = getPlayersParticipating().stream().filter(player -> player.getUniqueId() != player1.getUniqueId()).findAny().get();

        setPlayer1(player1);
        setPlayer2(player2);

        addEventItemsToPlayer(player1);
        addEventItemsToPlayer(player2);

        new BukkitRunnable() { // sync
            @Override
            public void run() {
                showPlayerToAllParticipants(player1);
                showPlayerToAllParticipants(player2);
            }
        }.runTaskLater(VoltzEvents.get(), 0L);

        player1.teleport(getPos1Location());
        player2.teleport(getPos2Location());

        new PlayerCheckTask(player1);
        new PlayerCheckTask(player2);

        sendTitleToAllParticipants(Titles.FIGHTERS[0], Titles.FIGHTERS[1], new String[]{
                "{player1}",
                "{player2}"
        }, new String[]{
                player1.getName(),
                player2.getName()
        });
    }

    @Override
    public void teleportPlayersToArenaAndExecuteEventActions() {
    }

    @Override
    public void resetAllValues() {
    }

    protected static class Locations {

        public static final Location JOIN_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "Sumo.join", null));

        public static final Location EXIT_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "Sumo.exit", null));

        public static final Location POS1_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "Sumo.pos1", null));

        public static final Location POS2_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "Sumo.pos2", null));
    }

    public static class Settings {

        public static final String COMMAND = FileUtils.get().getString(FileUtils.Files.SUMO, "Settings.command");

        public static final List<String> ALIASES = FileUtils.get().getStringList(FileUtils.Files.SUMO, "Settings.aliases");

        public static final String TAG = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.SUMO, "Settings.tag"));

        public static final Map<Integer, String> WINNERS = DataManager.getInstance().getWinnersFromFile("Sumo");

        public static final EventItems EVENT_ITEMS = DataManager.getInstance().getEventItemsFromFile("Sumo");

        public static final int WINNERS_AMOUNT = FileUtils.get().getInt(FileUtils.Files.SUMO, "Settings.winners-amount", 1);

        public static final int MINIMUM_PLAYERS = FileUtils.get().getInt(FileUtils.Files.SUMO, "Settings.minimum-players");

        public static final int ANNOUNCES_DELAY = FileUtils.get().getInt(FileUtils.Files.SUMO, "Settings.announces-delay");

        public static final int ANNOUNCES_AMOUNT = FileUtils.get().getInt(FileUtils.Files.SUMO, "Settings.announces-amount");

        public static final int TELEPORT_DELAY = FileUtils.get().getInt(FileUtils.Files.SUMO, "Settings.teleport-delay");

        public static final boolean SAVE_PLAYER_INVENTORY = FileUtils.get().getBoolean(FileUtils.Files.SUMO, "Settings.save-player-inventory");

        public static final boolean ADDITIONAL_VOID_CHECKER = FileUtils.get().getBoolean(FileUtils.Files.SUMO, "Settings.additional-void-checker");
    }

    public static class Messages {

        public static final List<String> EVENT_STARTING = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.SUMO, "Messages.event-starting"));

        public static final List<String> EVENT_STARTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.SUMO, "Messages.event-started"));

        public static final List<String> EVENT_CANCELLED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.SUMO, "Messages.event-cancelled"));

        public static final List<String> EVENT_FINISHED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.SUMO, "Messages.event-finished"));

        public static final List<String> INSUFFICIENT_PLAYERS = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.SUMO, "Messages.insufficient-players"));
    }

    public static class Titles {

        public static final String[] WINNER = new String[]{
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.SUMO, "Titles.winner.title")),
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.SUMO, "Titles.winner.subtitle"))
        };

        public static final String[] FIGHTERS = new String[]{
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.SUMO, "Titles.fighters.title")),
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.SUMO, "Titles.fighters.subtitle"))
        };

        public static final String[] ELIMINATED = new String[]{
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.SUMO, "Titles.eliminated.title")),
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.SUMO, "Titles.eliminated.subtitle"))
        };
    }
}