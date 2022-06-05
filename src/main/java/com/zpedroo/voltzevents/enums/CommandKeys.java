package com.zpedroo.voltzevents.enums;

import com.zpedroo.voltzevents.utils.config.Settings;
import org.apache.commons.lang.StringUtils;

public enum CommandKeys {
    START(Settings.START_KEY),
    CANCEL(Settings.CANCEL_KEY),
    SET_POS1(Settings.SET_POS1_KEY),
    SET_POS2(Settings.SET_POS2_KEY),
    SET_ARENA(Settings.SET_ARENA_KEY),
    SET_WIN_REGION(Settings.SET_WIN_REGION_KEY),
    SET_ITEMS(Settings.SET_ITEMS_KEY),
    SET_JOIN(Settings.SET_JOIN_KEY),
    SET_EXIT(Settings.SET_EXIT_KEY);

    private final String key;

    CommandKeys(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static CommandKeys getCommandKey(String str) {
        for (CommandKeys keys : values()) {
            if (StringUtils.equalsIgnoreCase(keys.getKey(), str)) return keys;
        }

        return null;
    }
}