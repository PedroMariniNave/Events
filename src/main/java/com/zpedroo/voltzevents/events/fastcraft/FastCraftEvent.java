package com.zpedroo.voltzevents.events.fastcraft;

import com.zpedroo.voltzevents.commands.FunEventCmd;
import com.zpedroo.voltzevents.enums.EventPhase;
import com.zpedroo.voltzevents.events.fastcraft.listeners.FastCraftListeners;
import com.zpedroo.voltzevents.events.fastcraft.tasks.FastCraftTask;
import com.zpedroo.voltzevents.managers.CommandManager;
import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.managers.ListenerManager;
import com.zpedroo.voltzevents.objects.host.EventHost;
import com.zpedroo.voltzevents.objects.player.EventData;
import com.zpedroo.voltzevents.types.FunEvent;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.color.Colorize;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

import static com.zpedroo.voltzevents.events.fastcraft.FastCraftEvent.CraftableItems.ITEMS;
import static com.zpedroo.voltzevents.events.fastcraft.FastCraftEvent.Messages.*;
import static com.zpedroo.voltzevents.events.fastcraft.FastCraftEvent.Settings.*;

public class FastCraftEvent extends FunEvent {

    private Material craftItem = null;

    public FastCraftEvent(Plugin plugin) {
        super("FastCraft", FileUtils.Files.FASTCRAFT, TAG,  new HashMap<String, List<String>>() {{
            put("STARTED", EVENT_STARTED);
            put("STARTED_HOSTED", EVENT_STARTED_HOSTED);
            put("HAPPENING", EVENT_HAPPENING);
            put("HAPPENING_HOSTED", EVENT_HAPPENING_HOSTED);
            put("CANCELLED", EVENT_CANCELLED);
            put("FINISHED", EVENT_FINISHED);
            put("FINISHED_HOSTED", EVENT_FINISHED_HOSTED);
        }}, WINNERS, WINNERS_AMOUNT);

        ListenerManager.registerListener(plugin, new FastCraftListeners(this));
        CommandManager.registerCommand(plugin, COMMAND, ALIASES, new FunEventCmd(this));
        DataManager.getInstance().getCache().getEvents().add(this);
    }

    @Override
    public void join(Player player) {
    }

    @Override
    public void startEvent() {
        startEvent(null);
    }

    @Override
    public void startEvent(@Nullable EventHost eventHost) {
        if (isHappening()) return;

        setEventHost(eventHost);
        getWinnersPosition().clear();
        startEventMethods();
    }

    @Override
    public void cancelEvent() {
        if (!isHappening()) return;

        setEventPhase(EventPhase.INACTIVE);
        sendCancelledMessages();
    }

    @Override
    public void checkIfPlayerIsWinner(Player player, int participantsAmount) {
    }

    @Override
    public void startEventMethods() {
        EventData eventData = new EventData();
        eventData.setStartTimestamp(System.currentTimeMillis());
        int randomIndex = new Random().nextInt(ITEMS.size());

        this.craftItem = new LinkedList<>(ITEMS.keySet()).get(randomIndex);
        this.setEventPhase(EventPhase.STARTED);
        this.setEventData(eventData);
        new FastCraftTask(this, ANNOUNCES_DELAY, MAX_ANNOUNCES).startTask();
    }

    @Override
    public void teleportPlayersToArenaAndExecuteEventActions() {
    }

    @Override
    public void resetAllValues() {
    }

    public String getTranslation(Material craftItem) {
        return ITEMS.get(craftItem);
    }

    public Material getCraftItem() {
        return craftItem;
    }

    public static class CraftableItems {

        public static final Map<Material, String> ITEMS = DataManager.getInstance().getCraftableItemsAndTranslationsFromFile();
    }

    public static class Settings {

        public static final String COMMAND = FileUtils.get().getString(FileUtils.Files.FASTCRAFT, "Settings.command");

        public static final List<String> ALIASES = FileUtils.get().getStringList(FileUtils.Files.FASTCRAFT, "Settings.aliases");
        
        public static final String TAG = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.FASTCRAFT, "Settings.tag"));

        public static final Map<Integer, String> WINNERS = DataManager.getInstance().getWinnersFromFile("FastCraft");

        public static final int WINNERS_AMOUNT = FileUtils.get().getInt(FileUtils.Files.FASTCRAFT, "Settings.winners-amount", 1);

        public static final int ANNOUNCES_DELAY = FileUtils.get().getInt(FileUtils.Files.FASTCRAFT, "Settings.announces-delay");

        public static final int MAX_ANNOUNCES = FileUtils.get().getInt(FileUtils.Files.FASTCRAFT, "Settings.max-announces");
    }

    public static class Messages {

        public static final List<String> EVENT_STARTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.FASTCRAFT, "Messages.event-started"));

        public static final List<String> EVENT_STARTED_HOSTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.FASTCRAFT, "Messages.event-started-hosted"));

        public static final List<String> EVENT_HAPPENING = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.FASTCRAFT, "Messages.event-happening"));

        public static final List<String> EVENT_HAPPENING_HOSTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.FASTCRAFT, "Messages.event-happening-hosted"));

        public static final List<String> EVENT_FINISHED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.FASTCRAFT, "Messages.event-finished"));

        public static final List<String> EVENT_FINISHED_HOSTED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.FASTCRAFT, "Messages.event-finished-hosted"));

        public static final List<String> EVENT_CANCELLED = Colorize.getColored(FileUtils.get().getStringList(FileUtils.Files.FASTCRAFT, "Messages.event-cancelled"));
    }
}