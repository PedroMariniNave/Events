package com.zpedroo.voltzevents.events.killer;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.commands.ArenaEventCmd;
import com.zpedroo.voltzevents.events.killer.listeners.KillerListeners;
import com.zpedroo.voltzevents.managers.CommandManager;
import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.managers.ListenerManager;
import com.zpedroo.voltzevents.objects.EventItems;
import com.zpedroo.voltzevents.tasks.AnnounceTask;
import com.zpedroo.voltzevents.tasks.WarmupTask;
import com.zpedroo.voltzevents.types.ArenaEvent;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.color.Colorize;
import com.zpedroo.voltzevents.utils.region.CuboidRegion;
import com.zpedroo.voltzevents.utils.serialization.LocationSerialization;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zpedroo.voltzevents.events.killer.KillerEvent.ActionBars.WARMUP_BAR;
import static com.zpedroo.voltzevents.events.killer.KillerEvent.Locations.*;
import static com.zpedroo.voltzevents.events.killer.KillerEvent.Messages.*;
import static com.zpedroo.voltzevents.events.killer.KillerEvent.Settings.*;

public class KillerEvent extends ArenaEvent {

    private static KillerEvent instance;
    public static KillerEvent getInstance() { return instance; }

    public KillerEvent(Plugin plugin) {
        super("Killer", FileUtils.Files.KILLER, TAG, new HashMap<String, List<String>>() {{
            put("STARTING", EVENT_STARTING);
            put("STARTED", EVENT_STARTED);
            put("CANCELLED", EVENT_CANCELLED);
            put("FINISHED", EVENT_FINISHED);
            put("INSUFFICIENT_PLAYERS", INSUFFICIENT_PLAYERS);
        }}, WINNERS, WINNERS_AMOUNT, MINIMUM_PLAYERS, SAVE_PLAYER_INVENTORY, EVENT_ITEMS, JOIN_LOCATION, EXIT_LOCATION, ARENA_LOCATION);

        instance = this;
        setAnnounceTask(new AnnounceTask(plugin, this, ANNOUNCES_DELAY, ANNOUNCES_AMOUNT));
        ListenerManager.registerListener(plugin, new KillerListeners());
        CommandManager.registerCommand(plugin, COMMAND, ALIASES, new ArenaEventCmd(this));
        DataManager.getInstance().getCache().getEvents().add(this);
    }

    @Override
    public void checkWinner(Player player) {
        if (getPlayersParticipatingAmount() > WINNERS_AMOUNT) return;

        int position = getPlayersParticipatingAmount();
        winEvent(player, position, true);

        if (getPlayersParticipatingAmount() <= 1) {
            Player winner = getPlayersParticipating().size() == 1 ? getPlayersParticipating().get(0) : null;
            if (winner == null) return;

            winEvent(winner, 1, false);
            sendFinishMessages();

            new BukkitRunnable() {
                @Override
                public void run() {
                    finishEvent(false);
                }
            }.runTaskLater(VoltzEvents.get(), 20L * FINISH_TIME);
        }
    }

    @Override
    public void startEventMethods() {
        WarmupTask warmupTask = new WarmupTask(this, WARMUP_BAR, WARMUP_DURATION);
        warmupTask.startTask();
    }

    @Override
    public void teleportPlayersToArenaAndExecuteEventActions() {
        getPlayersParticipating().forEach(player -> player.teleport(getArenaLocation()));
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

        public static final Location JOIN_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "Killer.join", null));

        public static final Location EXIT_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "Killer.exit", null));

        public static final Location ARENA_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "Killer.arena", null));
    }

    public static class Settings {

        public static final String COMMAND = FileUtils.get().getString(FileUtils.Files.KILLER, "Settings.command");

        public static final List<String> ALIASES = FileUtils.get().getStringList(FileUtils.Files.KILLER, "Settings.aliases");

        public static final String TAG = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.KILLER, "Settings.tag"));

        public static final Map<Integer, String> WINNERS = DataManager.getInstance().getWinnersFromFile("Killer");

        public static final EventItems EVENT_ITEMS = DataManager.getInstance().getEventItemsFromFile("Killer");

        public static final int WINNERS_AMOUNT = FileUtils.get().getInt(FileUtils.Files.KILLER, "Settings.winners-amount", 1);

        public static final int MINIMUM_PLAYERS = FileUtils.get().getInt(FileUtils.Files.KILLER, "Settings.minimum-players");

        public static final int ANNOUNCES_DELAY = FileUtils.get().getInt(FileUtils.Files.KILLER, "Settings.announces-delay");

        public static final int ANNOUNCES_AMOUNT = FileUtils.get().getInt(FileUtils.Files.KILLER, "Settings.announces-amount");

        public static final int WARMUP_DURATION = FileUtils.get().getInt(FileUtils.Files.KILLER, "Settings.warmup-duration");

        public static final int FINISH_TIME = FileUtils.get().getInt(FileUtils.Files.KILLER, "Settings.finish-time");

        public static final boolean SAVE_PLAYER_INVENTORY = FileUtils.get().getBoolean(FileUtils.Files.KILLER, "Settings.save-player-inventory");
    }

    public static class Messages {

        public static final List<String> EVENT_STARTING = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.KILLER, "Messages.event-starting"));

        public static final List<String> EVENT_STARTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.KILLER, "Messages.event-started"));

        public static final List<String> EVENT_CANCELLED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.KILLER, "Messages.event-cancelled"));

        public static final List<String> EVENT_FINISHED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.KILLER, "Messages.event-finished"));

        public static final List<String> INSUFFICIENT_PLAYERS = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.KILLER, "Messages.insufficient-players"));
    }

    public static class ActionBars {

        public static final String WARMUP_BAR = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.KILLER, "Action-Bars.warmup"));
    }

    public static class Titles {

        public static final String[] ELIMINATED = new String[]{
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.KILLER, "Titles.eliminated.title")),
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.KILLER, "Titles.eliminated.subtitle"))
        };
    }
}