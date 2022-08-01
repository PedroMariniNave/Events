package com.zpedroo.voltzevents.events.hotpotato;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.commands.ArenaEventCmd;
import com.zpedroo.voltzevents.enums.EventPhase;
import com.zpedroo.voltzevents.enums.LeaveReason;
import com.zpedroo.voltzevents.events.hotpotato.listeners.HotPotatoListeners;
import com.zpedroo.voltzevents.events.hotpotato.tasks.HotPotatoTask;
import com.zpedroo.voltzevents.events.hotpotato.tasks.PlayerFinderTask;
import com.zpedroo.voltzevents.managers.CommandManager;
import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.managers.ListenerManager;
import com.zpedroo.voltzevents.objects.player.EventItems;
import com.zpedroo.voltzevents.tasks.event.AnnounceTask;
import com.zpedroo.voltzevents.types.ArenaEvent;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.builder.ItemBuilder;
import com.zpedroo.voltzevents.utils.color.Colorize;
import com.zpedroo.voltzevents.utils.region.CuboidRegion;
import com.zpedroo.voltzevents.utils.serialization.LocationSerialization;
import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

import static com.zpedroo.voltzevents.events.hotpotato.HotPotatoEvent.Locations.*;
import static com.zpedroo.voltzevents.events.hotpotato.HotPotatoEvent.Messages.*;
import static com.zpedroo.voltzevents.events.hotpotato.HotPotatoEvent.Settings.*;

public class HotPotatoEvent extends ArenaEvent {

    private final Map<Integer, ItemStack> taggedItems = getTaggedItemsFromFile();
    private final List<String> hotPotatoesNames = new ArrayList<>(HOT_POTATOES_MAX);

    private HotPotatoTask hotPotatoTask = null;

    public HotPotatoEvent(Plugin plugin) {
        super("HotPotato", FileUtils.Files.HOT_POTATO, WHITELISTED_COMMANDS, TAG, new HashMap<String, List<String>>() {{
            put("ROUND_STARTED", ROUND_STARTED);
            put("ROUND_FINISHED", ROUND_FINISHED);
            put("STARTING", EVENT_STARTING);
            put("STARTING_HOSTED", EVENT_STARTING_HOSTED);
            put("STARTED", EVENT_STARTED);
            put("STARTED_HOSTED", EVENT_STARTED_HOSTED);
            put("CANCELLED", EVENT_CANCELLED);
            put("FINISHED", EVENT_FINISHED);
            put("FINISHED_HOSTED", EVENT_FINISHED_HOSTED);
            put("INSUFFICIENT_PLAYERS", INSUFFICIENT_PLAYERS);
        }}, WINNERS, WINNERS_AMOUNT, MINIMUM_PLAYERS_TO_START, MINIMUM_PLAYERS_AFTER_START, SAVE_PLAYER_INVENTORY, ADDITIONAL_VOID_CHECKER, EVENT_ITEMS, JOIN_LOCATION, EXIT_LOCATION, ARENA_LOCATION);

        setAnnounceTask(new AnnounceTask(plugin, this, ANNOUNCES_DELAY, ANNOUNCES_AMOUNT));
        ListenerManager.registerListener(plugin, new HotPotatoListeners(this));
        CommandManager.registerCommand(plugin, COMMAND, ALIASES, new ArenaEventCmd(this));
        DataManager.getInstance().getCache().getEvents().add(this);
    }

    @Override
    public void checkIfPlayerIsWinner(Player player, int participantsAmount) {
        if (participantsAmount > WINNERS_AMOUNT) return;

        int position = participantsAmount;
        winEvent(player, position);

        int newParticipantsAmount = participantsAmount - 1;
        if (newParticipantsAmount <= MINIMUM_PLAYERS_AFTER_START) {
            Optional<Player> optionalPlayer = getPlayersParticipating().stream().filter(players -> !players.equals(player)).findFirst();
            optionalPlayer.ifPresent(winner -> leave(winner, LeaveReason.WINNER));
            finishEvent(true);
        }
    }

    @Override
    public void executeJoinMethods(Player player) {
    }

    @Override
    public void startEventMethods() {
        this.setEventPhase(EventPhase.STARTED);
        this.selectHotPotatoesAndAnnounce();
        this.hotPotatoTask = new HotPotatoTask(this);
        this.hotPotatoTask.startTask();

        PlayerFinderTask playerFinderTask = new PlayerFinderTask(this);
        playerFinderTask.startTask();
    }

    @Override
    public void teleportPlayersToArenaAndExecuteEventActions() {
//        getPlayersParticipating().forEach(player -> player.teleport(getArenaLocation()));
    }

    @Override
    public void resetAllValues() {
        this.hotPotatoesNames.clear();
    }

    @Override
    public CuboidRegion getWinRegion() {
        return null;
    }

    @Override
    public void setWinRegion(CuboidRegion winRegion) {
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

    public int getRound() {
        return hotPotatoTask.getRound();
    }

    public int getBurnTimer() {
        return hotPotatoTask.getBurnTimer();
    }

    public int getNewRoundTimer() {
        return hotPotatoTask.getNewRoundTimer();
    }

    public void explodeHotPotato(Player player) {
        this.explodeHotPotato(player.getName());
    }

    public void explodeHotPotato(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null || !isHotPotato(player)) return;

        hotPotatoesNames.remove(playerName);
        player.getWorld().createExplosion(player.getLocation().getX(), player.getLocation().getY() + 1, player.getLocation().getZ(), 5, false, false);
        player.sendTitle(Titles.ELIMINATED[0], Titles.ELIMINATED[1]);

        leave(player, LeaveReason.ELIMINATED);
    }

    public void explodeAllHotPotatoes() {
        new ArrayList<>(hotPotatoesNames).forEach(this::explodeHotPotato);
    }

    public void transferHotPotato(Player hotPotato, Player newHotPotato) {
        hotPotatoesNames.remove(hotPotato.getName());
        hotPotato.getInventory().setHelmet(new ItemStack(Material.AIR));

        taggedItems.values().forEach(item -> hotPotato.getInventory().removeItem(item));
        setHotPotato(newHotPotato);

        hotPotato.sendTitle(Titles.UNTAGGED[0], Titles.UNTAGGED[1]);
        newHotPotato.sendTitle(Titles.TAGGED[0], Titles.TAGGED[1]);

        explodeFirework(newHotPotato.getLocation().clone().add(0D, 2D, 0D));
    }

    public void setHotPotato(Player player) {
        hotPotatoesNames.add(player.getName());
        player.getInventory().setHelmet(new ItemStack(Material.TNT));
        for (Map.Entry<Integer, ItemStack> entry : taggedItems.entrySet()) {
            int slot = entry.getKey();
            ItemStack item = entry.getValue();

            player.getInventory().setItem(slot, item);
        }
    }

    public boolean isHotPotato(Player player) {
        return isHotPotato(player.getName());
    }

    public boolean isHotPotato(String playerName) {
        return hotPotatoesNames.contains(playerName);
    }

    public Player getNearestUntagged(Player player) {
        Player nearestPlayer = null;

        for (Player players : getPlayersParticipating().stream().filter(
                participant -> !isHotPotato(participant) && !participant.equals(player))
                .collect(Collectors.toList())
        ) {
            if (nearestPlayer == null) {
                nearestPlayer = players;
                continue;
            }

            double actualDistance = player.getLocation().distance(nearestPlayer.getLocation());
            double distance = player.getLocation().distance(players.getLocation());
            if (distance < actualDistance) nearestPlayer = players;
        }

        return nearestPlayer;
    }

    private void explodeFirework(Location location) {
        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkEffect effect = FireworkEffect.builder().trail(true).flicker(false).withColor(Color.GREEN).with(FireworkEffect.Type.CREEPER).build();
        FireworkMeta meta = firework.getFireworkMeta();
        meta.clearEffects();
        meta.addEffect(effect);
        firework.setFireworkMeta(meta);

        new BukkitRunnable() {
            @Override
            public void run() {
                firework.detonate();
            }
        }.runTaskLaterAsynchronously(VoltzEvents.get(), 2L);
    }

    private Map<Integer, ItemStack> getTaggedItemsFromFile() {
        Map<Integer, ItemStack> ret = new HashMap<>(2);
        FileUtils.Files file = FileUtils.Files.HOT_POTATO;
        for (String str : FileUtils.get().getSection(file, "Tagged-Items")) {
            String identifier = FileUtils.get().getString(file, "Tagged-Items." + str + ".identifier");
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Tagged-Items." + str).build();
            int slot = FileUtils.get().getInt(file, "Tagged-Items." + str + ".slot");

            NBTItem nbt = new NBTItem(item);
            nbt.addCompound(identifier);

            ret.put(slot, nbt.getItem());
        }

        return ret;
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

        public static final int MINIMUM_PLAYERS_TO_START = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Settings.minimum-players.to-start");

        public static final int MINIMUM_PLAYERS_AFTER_START = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Settings.minimum-players.after-start");

        public static final int HOT_POTATOES_PROPORTION = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Settings.hot-potatoes.proportion");

        public static final int HOT_POTATOES_MAX = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Settings.hot-potatoes.max");

        public static final int ANNOUNCES_DELAY = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Settings.announces-delay");

        public static final int ROUND_DELAY = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Settings.round-delay");

        public static final int ROUND_DURATION = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Settings.round-duration");

        public static final int ANNOUNCES_AMOUNT = FileUtils.get().getInt(FileUtils.Files.HOT_POTATO, "Settings.announces-amount");

        public static final boolean SAVE_PLAYER_INVENTORY = FileUtils.get().getBoolean(FileUtils.Files.HOT_POTATO, "Settings.save-player-inventory");

        public static final boolean ADDITIONAL_VOID_CHECKER = FileUtils.get().getBoolean(FileUtils.Files.HOT_POTATO, "Settings.additional-void-checker");

        public static final List<String> WHITELISTED_COMMANDS = FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Whitelisted-Commands");
    }

    public static class Messages {

        public static final List<String> ROUND_STARTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Messages.round-started"));

        public static final List<String> ROUND_FINISHED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Messages.round-finished"));

        public static final List<String> EVENT_STARTING = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Messages.event-starting"));

        public static final List<String> EVENT_STARTING_HOSTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Messages.event-starting-hosted"));

        public static final List<String> EVENT_STARTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Messages.event-started"));

        public static final List<String> EVENT_STARTED_HOSTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Messages.event-started-hosted"));

        public static final List<String> EVENT_CANCELLED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Messages.event-cancelled"));

        public static final List<String> EVENT_FINISHED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Messages.event-finished"));

        public static final List<String> EVENT_FINISHED_HOSTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.HOT_POTATO, "Messages.event-finished-hosted"));

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