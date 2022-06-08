package com.zpedroo.voltzevents.utils.config;

import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.color.Colorize;

public class Messages {

    public static final String BLACKLISTED_COMMAND = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.blacklisted-command"));

    public static final String ALREADY_STARTED = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.already-started"));

    public static final String NOT_STARTED = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.not-started"));

    public static final String ALREADY_PARTICIPATING = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.already-participating"));

    public static final String INVALID_LOCATION = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.invalid-location"));

    public static final String INVALID_POSITION = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.invalid-position"));

    public static final String INVALID_CHECKPOINT = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.invalid-checkpoint"));

    public static final String POSITION_SET = Colorize.getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.position-set"));
}