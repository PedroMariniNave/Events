package com.zpedroo.voltzevents.utils.scoreboard;

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.util.Iterator;

@Getter
@Setter
public class ScoreboardLine {

    private String line = "";
    private String prefix = "";
    private String suffix = "";
    private int index;

    public ScoreboardLine(String line, int index) {
        setIndex(index);
        setLine(line);
    }

    public void setLine(String line) {
        this.line = line;

        if (line.isEmpty()) {
            this.line = "" + ChatColorService.getRandomColors(index);
            return;
        }

        Iterator<String> it = Splitter.fixedLength(16).split(line).iterator();
        prefix = it.next();
        if (it.hasNext()) {
            suffix = createSuffix();
            if (suffix.length() > 16) suffix = suffix.substring(0, 16);
        }
    }

    private String createSuffix() {
        int maxLength = 16;
        int index = line.charAt(maxLength - 1) == ChatColor.COLOR_CHAR ? (maxLength - 1) : maxLength;
        prefix = line.substring(0, index);
        String suffixTmp = line.substring(index);
        ChatColor chatColor = null;

        if (suffixTmp.length() >= 2 && suffixTmp.charAt(0) == ChatColor.COLOR_CHAR) {
            chatColor = ChatColor.getByChar(suffixTmp.charAt(1));
        }

        String color = ChatColor.getLastColors(prefix);
        boolean addColor = chatColor == null || chatColor.isFormat();

        return (addColor ? (color.isEmpty() ? ChatColor.RESET.toString() : color) : "") + suffixTmp;
    }
}