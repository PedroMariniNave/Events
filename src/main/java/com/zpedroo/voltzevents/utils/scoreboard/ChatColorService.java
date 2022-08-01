package com.zpedroo.voltzevents.utils.scoreboard;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ChatColorService {

    private static final List<ChatColor> COLORS = Arrays.asList(ChatColor.values());

    public static String getRandomColors(int index) {
        StringBuilder color = new StringBuilder();
        for (int i = 0; i < ThreadLocalRandom.current().nextInt(0, 5); ++i) {
            generateRandomColor(index, color);
        }

        color.append(COLORS.get(index));
        return color.toString();
    }

    private static void generateRandomColor(int index, StringBuilder color) {
        if (index > 15) {
            if (index < 32) {
                color.append(ChatColor.BOLD);
            } else if (index < 47) {
                color.append(ChatColor.ITALIC);
            } else if (index < 63) {
                color.append(ChatColor.STRIKETHROUGH);
            } else if (index < 79) {
                color.append(ChatColor.UNDERLINE);
            } else if (index < 95) {
                color.append(ChatColor.MAGIC);
            }
        }
    }
}