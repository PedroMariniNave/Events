package com.zpedroo.voltzevents;

import com.zpedroo.voltzevents.commands.EventsCmd;
import com.zpedroo.voltzevents.events.fastcraft.FastCraftEvent;
import com.zpedroo.voltzevents.events.fight.FightEvent;
import com.zpedroo.voltzevents.events.hotpotato.HotPotatoEvent;
import com.zpedroo.voltzevents.events.killer.KillerEvent;
import com.zpedroo.voltzevents.events.paintball.PaintballEvent;
import com.zpedroo.voltzevents.events.parkour.ParkourEvent;
import com.zpedroo.voltzevents.events.race.RaceEvent;
import com.zpedroo.voltzevents.events.sumo.SumoEvent;
import com.zpedroo.voltzevents.listeners.*;
import com.zpedroo.voltzevents.managers.CommandManager;
import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.managers.WinRegionManager;
import com.zpedroo.voltzevents.mysql.DBConnection;
import com.zpedroo.voltzevents.scheduler.SchedulerLoader;
import com.zpedroo.voltzevents.tasks.SaveTask;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.cooldown.Cooldown;
import com.zpedroo.voltzevents.utils.menu.Menus;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.quartz.SchedulerException;

import java.util.logging.Level;

import static com.zpedroo.voltzevents.utils.config.Settings.ALIASES;
import static com.zpedroo.voltzevents.utils.config.Settings.COMMAND;

public class VoltzEvents extends JavaPlugin {

    private static VoltzEvents instance;
    public static VoltzEvents get() { return instance; }

    public void onEnable() {
        instance = this;
        new FileUtils(this);

        if (!isMySQLEnabled(getConfig())) {
            getLogger().log(Level.SEVERE, "MySQL are disabled! You need to enable it.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new DBConnection(getConfig());
        new DataManager();
        new WinRegionManager();
        new Menus();
        new Cooldown();
        new SaveTask(this);

        getServer().getScheduler().runTaskLaterAsynchronously(this, this::loadEvents, 100L);
        registerListeners();
        registerHooks();
        startAllEventsScheduler();
        CommandManager.registerCommand(this, COMMAND, ALIASES, new EventsCmd());
    }

    private void startAllEventsScheduler() {
        SchedulerLoader scheduler = new SchedulerLoader();
        try {
            scheduler.startAllEventsScheduler();
        } catch (SchedulerException ex) {
            ex.printStackTrace();
        }
    }

    public void onDisable() {
        if (!isMySQLEnabled(getConfig())) return;

        try {
            DataManager.getInstance().cancelAllEvents();
            DataManager.getInstance().saveAllEventItemsInFile();
            DataManager.getInstance().saveAllWinnersInFile();
            DataManager.getInstance().saveAllPlayersData();
            DBConnection.getInstance().closeConnection();
            SchedulerLoader.getInstance().stopAllSchedulers();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "An error occurred while trying to save data!");
            ex.printStackTrace();
        }
    }

    private void loadEvents() {
        new FastCraftEvent(this);
        new FightEvent(this);
        new KillerEvent(this);
        new SumoEvent(this);
        new ParkourEvent(this);
        new RaceEvent(this);
        new HotPotatoEvent(this);
        new PaintballEvent(this);
//        new SpleefEvent(this);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerGeneralListeners(), this);
        getServer().getPluginManager().registerEvents(new PreHostChatListeners(), this);
        getServer().getPluginManager().registerEvents(new SpecialItemListeners(), this);
        getServer().getPluginManager().registerEvents(new WinRegionSetListeners(), this);
    }

    private void registerHooks() {
        if (Bukkit.getPluginManager().getPlugin("Legendchat") != null) {
            getServer().getPluginManager().registerEvents(new TagListeners(), this);
        }
    }

    private boolean isMySQLEnabled(FileConfiguration file) {
        if (!file.contains("MySQL.enabled")) return false;

        return file.getBoolean("MySQL.enabled");
    }
}