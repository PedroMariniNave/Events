package com.zpedroo.voltzevents.utils.storer;

import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class InventoryStorer {

    private static final Map<Player, StoredInventory> storedInventories = new HashMap<>(24);

    public static void storePlayerInventory(Player player) {
        if (storedInventories.containsKey(player)) return;

        storedInventories.put(player, new StoredInventory(player.getInventory().getContents(), player.getInventory().getArmorContents()));
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.updateInventory();
    }

    public static void restorePlayerInventory(Player player) {
        StoredInventory storedInventory = storedInventories.remove(player);
        if (storedInventory == null) return;
        if (player.isDead()) player.spigot().respawn();

        player.getInventory().setContents(storedInventory.getInventoryContents());
        player.getInventory().setArmorContents(storedInventory.getArmorContents());
        player.updateInventory();
    }

    @Data
    protected static class StoredInventory {

        private final ItemStack[] inventoryContents;
        private final ItemStack[] armorContents;
    }
}