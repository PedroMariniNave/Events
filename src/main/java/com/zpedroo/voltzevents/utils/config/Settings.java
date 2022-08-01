package com.zpedroo.voltzevents.utils.config;

import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.color.Colorize;

import java.util.List;

public class Settings {

    public static final String COMMAND = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.command");

    public static final List<String> ALIASES = FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Settings.aliases");

    public static final String START_KEY = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.command-keys.start");

    public static final String CANCEL_KEY = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.command-keys.cancel");

    public static final String SET_POS1_KEY = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.command-keys.set-pos1");

    public static final String SET_POS2_KEY = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.command-keys.set-pos2");

    public static final String SET_ARENA_KEY = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.command-keys.set-arena");

    public static final String SET_WIN_REGION_KEY = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.command-keys.set-win-region");

    public static final String SET_ITEMS_KEY = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.command-keys.set-items");

    public static final String SET_JOIN_KEY = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.command-keys.set-join");

    public static final String SET_EXIT_KEY = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.command-keys.set-exit");

    public static final String ADMIN_PERMISSION = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.admin-permission");

    public static final String HOST_EVENT_PERMISSION = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.host-event.permission");

    public static final String NULL_WINNER = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.null-winner");

    public static final String TIME_FORMATTER = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.time-formatter");

    public static final String CURRENCY_SEPARATOR = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.currency-separator"));

    public static final String CANCEL_ACTION_KEY = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.cancel-action-key"));

    public static final boolean DISABLE_INVENTORY_INTERACTION = FileUtils.get().getBoolean(FileUtils.Files.CONFIG, "Settings.disable-inventory-interaction");

    public static final boolean DISABLE_ITEM_PICKUP = FileUtils.get().getBoolean(FileUtils.Files.CONFIG, "Settings.disable-item-pickup");

    public static final boolean DISABLE_ITEM_DROP = FileUtils.get().getBoolean(FileUtils.Files.CONFIG, "Settings.disable-item-drop");

    public static final boolean DISABLE_BLOCK_PLACE = FileUtils.get().getBoolean(FileUtils.Files.CONFIG, "Settings.disable-block-place");

    public static final boolean DISABLE_BLOCK_BREAK = FileUtils.get().getBoolean(FileUtils.Files.CONFIG, "Settings.disable-block-break");

    public static final int HOST_EVENT_COOLDOWN = FileUtils.get().getInt(FileUtils.Files.CONFIG, "Settings.host-event.cooldown");

    public static final long DATA_UPDATE_INTERVAL = FileUtils.get().getLong(FileUtils.Files.CONFIG, "Settings.data-update-interval");

    public static final long SCOREBOARD_UPDATE_INTERVAL = FileUtils.get().getLong(FileUtils.Files.CONFIG, "Settings.scoreboard-update-interval");
}