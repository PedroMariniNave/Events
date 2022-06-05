package com.zpedroo.voltzevents.utils.storer;

import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ExpStorer {

    private static final Map<Player, PlayerExp> storedExp = new HashMap<>(24);

    public static void storePlayerExp(Player player) {
        if (storedExp.containsKey(player)) return;

        storedExp.put(player, new PlayerExp(player.getLevel(), player.getExp()));
        player.setLevel(0);
        player.setExp(0);
    }

    public static void restorePlayerExp(Player player) {
        PlayerExp playerExp = storedExp.remove(player);
        if (playerExp == null) return;
        if (player.isDead()) player.spigot().respawn();

        player.setLevel(playerExp.getLevel());
        player.setExp(playerExp.getExperience());
    }

    @Data
    protected static class PlayerExp {

        private final int level;
        private final float experience;
    }
}