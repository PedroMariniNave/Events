package com.zpedroo.voltzevents.events.hotpotato;

import com.zpedroo.voltzevents.commands.ArenaEventCmd;
import com.zpedroo.voltzevents.enums.EventPhase;
import com.zpedroo.voltzevents.events.hotpotato.listeners.HotPotatoListeners;
import com.zpedroo.voltzevents.events.hotpotato.tasks.HotPotatoTask;
import com.zpedroo.voltzevents.managers.CommandManager;
import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.managers.ListenerManager;
import com.zpedroo.voltzevents.objects.EventItems;
import com.zpedroo.voltzevents.tasks.AnnounceTask;
import com.zpedroo.voltzevents.types.ArenaEvent;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.builder.ItemBuilder;
import com.zpedroo.voltzevents.utils.color.Colorize;
import com.zpedroo.voltzevents.utils.region.CuboidRegion;
import com.zpedroo.voltzevents.utils.serialization.LocationSerialization;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zpedroo.voltzevents.events.hotpotato.HotPotatoEvent.Locations.*;
import static com.zpedroo.voltzevents.events.hotpotato.HotPotatoEvent.Messages.*;
import static com.zpedroo.voltzevents.events.hotpotato.HotPotatoEvent.Settings.*;

public class HotPotatoEvent extends ArenaEvent {

    private static HotPotatoEvent instance;
    public static HotPotatoEvent getInstance() { return instance; }

    private final ItemStack hotPotatoItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.HOT_POTATO).get(), "Hot-Potato-Item").build();
    private int roundTimer = 0;
    private int newRoundTimer = ROUND_DELAY;
    private final List<String> hotPotatoesNames = new ArrayList<>(getHotPotatoesSpawnAmount());

    public HotPotatoEvent(Plugin plugin) {
        super("HotPotato", FileUtils.Files.HOT_POTATO, TAG, new HashMap<String, List<String>>() {{
            put("ROUND_STARTED", ROUND_STARTED);
            put("ROUND_FINISHED", ROUND_FINISHED);
            put("STARTING", EVENT_STARTING);
            put("STARTED", EVENT_STARTED);
            put("CANCELLED", EVENT_CANCELLED);
            put("FINISHED", EVENT_FINISHED);
            put("INSUFFICIENT_PLAYERS", INSUFFICIENT_PLAYERS);
        }}, WINNERS, WINNERS_AMOUNT, MINIMUM_PLAYERS, SAVE_PLAYER_INVENTORY, ADDITIONAL_VOID_CHECKER, EVENT_ITEMS, JOIN_LOCATION, EXIT_LOCATION, ARENA_LOCATION);

        instance = this;
        setAnnounceTask(new AnnounceTask(plugin, this, ANNOUNCES_DELAY, ANNOUNCES_AMOUNT));
        ListenerManager.registerListener(plugin, new HotPotatoListeners());
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
            if (winner != null) leave(winner, false, true);
            finishEvent(true);
        }
    }

    @Override
    public void startEventMethods() {
        this.setEventPhase(EventPhase.WARMUP);
        HotPotatoTask hotPotatoTask = new HotPotatoTask(this);
        hotPotatoTask.startTask();
    }

    @Override
    public void teleportPlayersToArenaAndExecuteEventActions() {
        getPlayersParticipating().forEach(player -> player.teleport(getArenaLocation()));
    }

    @Override
    public void resetAllValues() {
        this.roundTimer = 0;
        this.newRoundTimer = ROUND_DELAY;
        this.hotPotatoesNames.clear();
    }

    public void selectHotPotatoesAndAnnounce() {
        for (int i = 0; i < getHotPotatoesSpawnAmount(); ++i) {
            Player selectedPlayer = getPlayersParticipating().stream().filter(player -> !isHotPotato(player)).findAny().get();
            setHotPotato(selectedPlayer);
        }

        sendMessageToAllParticipants(ROUND_STARTED, new String[]{
                "{players}"
        }, new String[]{
                StringUtils.join(hotPotatoesNames, ChatColor.translateAlternateColorCodes('&', "&7, &f"))
        });
    }

    public ItemStack getHotPotatoItem() {
        return hotPotatoItem.clone();
    }

    public List<String> getHotPotatoesNames() {
        return hotPotatoesNames;
    }

    public int getHotPotatoesAmount() {
        return hotPotatoesNames.size();
    }

    public int getHotPotatoesSpawnAmount() {
        if (getPlayersParticipatingAmount() <= HOT_POTATOES_PROPORTION) return 1;

        int hotPotatoesAmount = getPlayersParticipatingAmount() / HOT_POTATOES_PROPORTION;
        if (hotPotatoesAmount > HOT_POTATOES_MAX) hotPotatoesAmount = HOT_POTATOES_MAX;

        return hotPotatoesAmount;
    }

    public int getRoundTimer() {
        return roundTimer;
    }

    public int getNewRoundTimer() {
        return newRoundTimer;
    }

    public void explodeHotPotato(Player player) {
        this.explodeHotPotato(player.getName());
    }

    public void explodeHotPotato(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null || !isHotPotato(player)) return;

        hotPotatoesNames.remove(playerName);
        player.getWorld().createExplosion(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 5, false, false);
        player.sendTitle(Titles.ELIMINATED[0], Titles.ELIMINATED[1]);

        leave(player, false, true);
    }

    public void explodeAllHotPotatoes() {
        new ArrayList<>(hotPotatoesNames).forEach(this::explodeHotPotato);
    }

    public void transferHotPotato(Player hotPotato, Player newHotPotato) {
        hotPotatoesNames.remove(hotPotato.getName());
        hotPotato.getInventory().setHelmet(new ItemStack(Material.AIR));
        hotPotato.getInventory().removeItem(hotPotatoItem);
        setHotPotato(newHotPotato);

        hotPotato.sendTitle(Titles.UNTAGGED[0], Titles.UNTAGGED[1]);
        newHotPotato.sendTitle(Titles.TAGGED[0], Titles.TAGGED[1]);
    }

    public void setHotPotato(Player player) {
        hotPotatoesNames.add(player.getName());
        player.getInventory().setHelmet(new ItemStack(Material.TNT));
        player.getInventory().addItem(hotPotatoItem);
    }

    public void setRoundTimer(int roundTimer) {
        this.roundTimer = roundTimer;
    }

    public void setNewRoundTimer(int newRoundTimer) {
        this.newRoundTimer = newRoundTimer;
    }

    public boolean isHotPotato(Player player) {
        return this.isHotPotato(player.getName());
    }

    public boolean isHotPotato(String playerName) {
        return hotPotatoesNames.contains(playerName);
    }

    @Override
    public CuboidRegion getWinRegion() {
        return null;
    }

    @Override
    public void setWinRegion(CuboidRegion winRegion) {
    }

    protected static class Locations {

        public static final Location JOIN_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "HotPotato.join", null));

        public static final Location EXIT_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "HotPotato.exit", null));

        public static final Location ARENA_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "HotPotato.arena", null));
    }

    public static class Settings {

        public static final String COMMAND = FileUtils.get().getString(FileUtils.Files.HOT_POTATO, "Settings.command");

        public static final List<String> ALIASES = FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Settings.aliases");

        public static final String TAG = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.HOT_POTATO, "Settings.tag"));

        public static final Map<Integer, String> WINNERS = DataManager.getInstance().getWinnersFromFile("HotPotato");

        public static final EventItems EVENT_ITEMS = DataManager.getInstance().getEventItemsFromFile("HotPotato");

        public static final int WINNERS_AMOUNT = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Settings.winners-amount", 1);

        public static final int MINIMUM_PLAYERS = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Settings.minimum-players");

        public static final int HOT_POTATOES_PROPORTION = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Settings.hot-potatoes.proportion");

        public static final int HOT_POTATOES_MAX = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Settings.hot-potatoes.max");

        public static final int ANNOUNCES_DELAY = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Settings.announces-delay");

        public static final int ROUND_DELAY = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Settings.round-delay");

        public static final int ROUND_DURATION = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Settings.round-duration");

        public static final int ANNOUNCES_AMOUNT = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Settings.announces-amount");

        public static final boolean SAVE_PLAYER_INVENTORY = FileUtils.get().getBoolean(FileUtils.Files.HOT_POTATO, "Settings.save-player-inventory");

        public static final boolean ADDITIONAL_VOID_CHECKER = FileUtils.get().getBoolean(FileUtils.Files.HOT_POTATO, "Settings.additional-void-checker");
    }

    public static class Messages {

        public static final List<String> ROUND_STARTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Messages.round-started"));

        public static final List<String> ROUND_FINISHED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Messages.round-finished"));

        public static final List<String> EVENT_STARTING = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Messages.event-starting"));

        public static final List<String> EVENT_STARTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Messages.event-started"));

        public static final List<String> EVENT_CANCELLED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Messages.event-cancelled"));

        public static final List<String> EVENT_FINISHED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Messages.event-finished"));

        public static final List<String> INSUFFICIENT_PLAYERS = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Messages.insufficient-players"));
    }

    public static class ActionBars {

        public static final String TAGGED = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.HOT_POTATO, "Action-Bars.tagged"));

        public static final String UNTAGGED = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.HOT_POTATO, "Action-Bars.untagged"));

        public static final String WARMUP = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.HOT_POTATO, "Action-Bars.warmup"));
    }

    public static class Titles {

        public static final String[] TAGGED = new String[]{
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.HOT_POTATO, "Titles.tagged.title")),
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.HOT_POTATO, "Titles.tagged.subtitle"))
        };

        public static final String[] UNTAGGED = new String[]{
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.HOT_POTATO, "Titles.untagged.title")),
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.HOT_POTATO, "Titles.untagged.subtitle"))
        };

        public static final String[] ELIMINATED = new String[]{
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.HOT_POTATO, "Titles.eliminated.title")),
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.HOT_POTATO, "Titles.eliminated.subtitle"))
        };
    }

    public static class Potions {

        public static final boolean IS_TAGGED_POTION_ENABLED = FileUtils.get().getBoolean(FileUtils.Files.HOT_POTATO, "Potions.tagged.enabled");

        public static final PotionEffectType TAGGED_POTION_TYPE = PotionEffectType.getByName(FileUtils.get().getString(FileUtils.Files.HOT_POTATO, "Potions.tagged.type"));

        public static final int TAGGED_POTION_LEVEL = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Potions.tagged.level");

        public static final boolean IS_UNTAGGED_POTION_ENABLED = FileUtils.get().getBoolean(FileUtils.Files.HOT_POTATO, "Potions.untagged.enabled");

        public static final PotionEffectType UNTAGGED_POTION_TYPE = PotionEffectType.getByName(FileUtils.get().getString(FileUtils.Files.HOT_POTATO, "Potions.untagged.type"));

        public static final int UNTAGGED_POTION_LEVEL = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Potions.untagged.level");
    }
}