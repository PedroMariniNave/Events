package com.zpedroo.voltzevents.managers;

import com.zpedroo.voltzevents.objects.WinRegionBuilder;
import com.zpedroo.voltzevents.types.ArenaEvent;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.builder.ItemBuilder;
import com.zpedroo.voltzevents.utils.storer.InventoryStorer;
import com.zpedroo.voltzevents.utils.region.CuboidRegion;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class WinRegionManager {

    private static WinRegionManager instance;
    public static WinRegionManager getInstance() { return instance; }

    private final Map<ItemStack, Integer> setRegionItems = getSetRegionItemsAndSlotsFromConfig();
    private final Map<Player, WinRegionBuilder> playersBuildingRegion = new HashMap<>(2);

    public WinRegionManager() {
        instance = this;
    }

    public void startRegionSet(Player player, ArenaEvent event) {
        WinRegionBuilder winRegionBuilder = new WinRegionBuilder(event);
        playersBuildingRegion.put(player, winRegionBuilder);

        InventoryStorer.storePlayerInventory(player);

        for (Map.Entry<ItemStack, Integer> entry : setRegionItems.entrySet()) {
            ItemStack item = entry.getKey();
            int slot = entry.getValue();

            player.getInventory().setItem(slot, item);
        }
    }

    public void finishRegionSet(Player player) {
        WinRegionBuilder winRegionBuilder = playersBuildingRegion.remove(player);
        if (winRegionBuilder == null) return;

        InventoryStorer.restorePlayerInventory(player);
        CuboidRegion winRegion = winRegionBuilder.build();
        if (winRegion == null) return;

        ArenaEvent event = winRegionBuilder.getEvent();
        event.setWinRegion(winRegion);
    }

    public void cancelRegionSet(Player player) {
        WinRegionBuilder winRegionBuilder = playersBuildingRegion.remove(player);
        if (winRegionBuilder != null) InventoryStorer.restorePlayerInventory(player);
    }

    public void setFirstPosition(Player player, Location location1) {
        WinRegionBuilder winRegionBuilder = playersBuildingRegion.get(player);
        if (winRegionBuilder != null) winRegionBuilder.setLocation1(location1);
    }

    public void setSecondPosition(Player player, Location location2) {
        WinRegionBuilder winRegionBuilder = playersBuildingRegion.get(player);
        if (winRegionBuilder != null) winRegionBuilder.setLocation2(location2);
    }

    public boolean canFinish(Player player) {
        WinRegionBuilder winRegionBuilder = playersBuildingRegion.get(player);

        return winRegionBuilder != null && winRegionBuilder.getLocation1() != null & winRegionBuilder.getLocation2() != null;
    }

    public boolean isBuildingWinRegion(Player player) {
        return playersBuildingRegion.containsKey(player);
    }

    private Map<ItemStack, Integer> getSetRegionItemsAndSlotsFromConfig() {
        Map<ItemStack, Integer> ret = new HashMap<>(4);
        FileUtils.Files file = FileUtils.Files.CONFIG;
        for (String items : FileUtils.get().getSection(file, "Set-Region-Items")) {
            int slot = FileUtils.get().getInt(file, "Set-Region-Items." + items + ".slot");
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Set-Region-Items." + items).build();
            String action = FileUtils.get().getString(file, "Set-Region-Items." + items + ".action");

            NBTItem nbt = new NBTItem(item);
            nbt.setString("WinRegionBuilderAction", action);

            ret.put(nbt.getItem(), slot);
        }


        return ret;
    }
}