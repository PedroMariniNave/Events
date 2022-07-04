package com.zpedroo.voltzevents.events.paintball;

import com.zpedroo.voltzevents.commands.ArenaEventCmd;
import com.zpedroo.voltzevents.enums.LeaveReason;
import com.zpedroo.voltzevents.events.paintball.listeners.PaintballListeners;
import com.zpedroo.voltzevents.managers.CommandManager;
import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.managers.ListenerManager;
import com.zpedroo.voltzevents.objects.player.EventItems;
import com.zpedroo.voltzevents.tasks.AnnounceTask;
import com.zpedroo.voltzevents.tasks.WarmupTask;
import com.zpedroo.voltzevents.types.ArenaEvent;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.builder.ItemBuilder;
import com.zpedroo.voltzevents.utils.color.Colorize;
import com.zpedroo.voltzevents.utils.region.CuboidRegion;
import com.zpedroo.voltzevents.utils.serialization.LocationSerialization;
import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.zpedroo.voltzevents.events.paintball.PaintballEvent.ActionBars.WARMUP_BAR;
import static com.zpedroo.voltzevents.events.paintball.PaintballEvent.Locations.*;
import static com.zpedroo.voltzevents.events.paintball.PaintballEvent.Messages.*;
import static com.zpedroo.voltzevents.events.paintball.PaintballEvent.Settings.*;

public class PaintballEvent extends ArenaEvent {

    private final ItemStack gunItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.PAINTBALL).get(), "Gun-Item").build();
    private final ItemStack reloadingGunItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.PAINTBALL).get(), "Reloading-Gun-Item").build();

    public PaintballEvent(Plugin plugin) {
        super("Paintball", FileUtils.Files.PAINTBALL, WHITELISTED_COMMANDS, TAG, new HashMap<String, List<String>>() {{
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
        ListenerManager.registerListener(plugin, new PaintballListeners(this));
        CommandManager.registerCommand(plugin, COMMAND, ALIASES, new ArenaEventCmd(this));
        DataManager.getInstance().getCache().getEvents().add(this);
    }

    public ItemStack getGunItem(int ammoAmount) {
        NBTItem nbt = new NBTItem(gunItem.clone());
        nbt.setInteger("PaintballAmmo", ammoAmount);

        return replacePlaceholders(nbt.getItem());
    }

    public ItemStack getReloadingGunItem(int ammoAmount) {
        NBTItem nbt = new NBTItem(reloadingGunItem.clone());
        nbt.setInteger("PaintballAmmo", ammoAmount);
        nbt.addCompound("Reloading");

        return replacePlaceholders(nbt.getItem());
    }

    private ItemStack replacePlaceholders(ItemStack itemToReplace) {
        NBTItem nbt = new NBTItem(itemToReplace);
        int ammoAmount = nbt.getInteger("PaintballAmmo");

        ItemStack item = nbt.getItem();
        if (item.getItemMeta() != null) {
            String displayName = item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : null;
            List<String> lore = item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : null;
            ItemMeta meta = item.getItemMeta();

            String[] placeholders = new String[]{
                    "{ammo}",
                    "{max}"
            };
            String[] replacers = new String[]{
                    String.valueOf(ammoAmount),
                    String.valueOf(MAX_AMMO_AMOUNT)
            };
            if (displayName != null) meta.setDisplayName(StringUtils.replaceEach(displayName, placeholders, replacers));
            if (lore != null) {
                List<String> newLore = new ArrayList<>(lore.size());
                for (String str : lore) {
                    newLore.add(StringUtils.replaceEach(str, placeholders, replacers));
                }

                meta.setLore(newLore);
            }

            item.setItemMeta(meta);
        }

        return item;
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
    public void startEventMethods() {
        WarmupTask warmupTask = new WarmupTask(this, WARMUP_BAR, WARMUP_DURATION);
        warmupTask.startTask();
        setWarmupTask(warmupTask);
    }

    @Override
    public void teleportPlayersToArenaAndExecuteEventActions() {
        getPlayersParticipating().forEach(player -> {
            player.teleport(getArenaLocation());
            player.getInventory().addItem(getGunItem(MAX_AMMO_AMOUNT));

            int red = ThreadLocalRandom.current().nextInt(0, 255);
            int green = ThreadLocalRandom.current().nextInt(0, 255);
            int blue = ThreadLocalRandom.current().nextInt(0, 255);

            ItemStack[] armor = new ItemStack[]{
                    new ItemStack(Material.LEATHER_BOOTS),
                    new ItemStack(Material.LEATHER_LEGGINGS),
                    new ItemStack(Material.LEATHER_CHESTPLATE),
                    new ItemStack(Material.LEATHER_HELMET)
            };

            LeatherArmorMeta meta = (LeatherArmorMeta) armor[0].getItemMeta();
            meta.setColor(Color.fromBGR(red, green, blue));
            meta.addItemFlags(ItemFlag.values());
            meta.spigot().setUnbreakable(true);

            for (ItemStack item : armor) {
                item.setItemMeta(meta);
            }

            player.getInventory().setArmorContents(armor);
            player.updateInventory();
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

        public static final Location JOIN_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "Paintball.join", null));

        public static final Location EXIT_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "Paintball.exit", null));

        public static final Location ARENA_LOCATION = LocationSerialization.deserialize(FileUtils.get().getString(FileUtils.Files.LOCATIONS, "Paintball.arena", null));
    }

    public static class Settings {

        public static final String COMMAND = FileUtils.get().getString(FileUtils.Files.PAINTBALL, "Settings.command");

        public static final List<String> ALIASES = FileUtils.get().getStringList(FileUtils.Files.PAINTBALL, "Settings.aliases");

        public static final String TAG = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.PAINTBALL, "Settings.tag"));

        public static final Map<Integer, String> WINNERS = DataManager.getInstance().getWinnersFromFile("Paintball");

        public static final EventItems EVENT_ITEMS = DataManager.getInstance().getEventItemsFromFile("Paintball");

        public static final int WINNERS_AMOUNT = FileUtils.get().getInt(FileUtils.Files.PAINTBALL, "Settings.winners-amount", 1);

        public static final int MINIMUM_PLAYERS_TO_START = FileUtils.get().getInt(FileUtils.Files.PAINTBALL, "Settings.minimum-players.to-start");

        public static final int MINIMUM_PLAYERS_AFTER_START = FileUtils.get().getInt(FileUtils.Files.PAINTBALL, "Settings.minimum-players.after-start");

        public static final int ANNOUNCES_DELAY = FileUtils.get().getInt(FileUtils.Files.PAINTBALL, "Settings.announces-delay");

        public static final int ANNOUNCES_AMOUNT = FileUtils.get().getInt(FileUtils.Files.PAINTBALL, "Settings.announces-amount");

        public static final int MAX_AMMO_AMOUNT = FileUtils.get().getInt(FileUtils.Files.PAINTBALL, "Settings.max-ammo-amount");

        public static final int RELOADING_TIME = FileUtils.get().getInt(FileUtils.Files.PAINTBALL, "Settings.reloading-time");

        public static final double SHOT_DAMAGE = FileUtils.get().getDouble(FileUtils.Files.PAINTBALL, "Settings.shot-damage");

        public static final int WARMUP_DURATION = FileUtils.get().getInt(FileUtils.Files.PAINTBALL, "Settings.warmup-duration");

        public static final boolean SAVE_PLAYER_INVENTORY = FileUtils.get().getBoolean(FileUtils.Files.PAINTBALL, "Settings.save-player-inventory");

        public static final boolean ADDITIONAL_VOID_CHECKER = FileUtils.get().getBoolean(FileUtils.Files.PAINTBALL, "Settings.additional-void-checker");

        public static final List<String> WHITELISTED_COMMANDS = FileUtils.get().getStringList(FileUtils.Files.PAINTBALL, "Whitelisted-Commands");
    }

    public static class Messages {

        public static final List<String> EVENT_STARTING = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.PAINTBALL, "Messages.event-starting"));

        public static final List<String> EVENT_STARTING_HOSTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.PAINTBALL, "Messages.event-starting-hosted"));

        public static final List<String> EVENT_STARTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.PAINTBALL, "Messages.event-started"));

        public static final List<String> EVENT_STARTED_HOSTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.PAINTBALL, "Messages.event-started-hosted"));

        public static final List<String> EVENT_CANCELLED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.PAINTBALL, "Messages.event-cancelled"));

        public static final List<String> EVENT_FINISHED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.PAINTBALL, "Messages.event-finished"));

        public static final List<String> EVENT_FINISHED_HOSTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.PAINTBALL, "Messages.event-finished-hosted"));

        public static final List<String> INSUFFICIENT_PLAYERS = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.PAINTBALL, "Messages.insufficient-players"));
    }

    public static class ActionBars {

        public static final String WARMUP_BAR = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.PAINTBALL, "Action-Bars.warmup"));
    }

    public static class Titles {

        public static final String[] ELIMINATED = new String[]{
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.PAINTBALL, "Titles.eliminated.title")),
                Colorize.getColored(FileUtils.get().getString(FileUtils.Files.PAINTBALL, "Titles.eliminated.subtitle"))
        };
    }
}