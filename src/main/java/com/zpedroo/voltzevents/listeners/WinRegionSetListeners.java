package com.zpedroo.voltzevents.listeners;

import com.zpedroo.voltzevents.managers.WinRegionManager;
import com.zpedroo.voltzevents.utils.config.Messages;
import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class WinRegionSetListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (!WinRegionManager.getInstance().isBuildingWinRegion(event.getPlayer())) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType().equals(Material.AIR)) return;

        NBTItem nbt = new NBTItem(item);
        if (!nbt.hasKey("WinRegionBuilderAction")) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        String action = nbt.getString("WinRegionBuilderAction");
        switch (action.toUpperCase()) {
            case "POS":
                Block clickedBlock = event.getClickedBlock();
                if (clickedBlock == null || clickedBlock.getType().equals(Material.AIR)) break;

                Location clickedLocation = clickedBlock.getLocation();
                switch (event.getAction()) {
                    case LEFT_CLICK_BLOCK:
                        WinRegionManager.getInstance().setFirstPosition(player, clickedLocation);
                        player.sendMessage(StringUtils.replace(Messages.POSITION_SET, "{pos}", String.valueOf(1)));
                        break;
                    case RIGHT_CLICK_BLOCK:
                        WinRegionManager.getInstance().setSecondPosition(player, clickedLocation);
                        player.sendMessage(StringUtils.replace(Messages.POSITION_SET, "{pos}", String.valueOf(2)));
                        break;
                }
                break;
            case "CONFIRM":
                if (!WinRegionManager.getInstance().canFinish(player)) {
                    player.sendMessage(Messages.INVALID_POSITION);
                    break;
                }

                WinRegionManager.getInstance().finishRegionSet(player);
                break;
            case "CANCEL":
                WinRegionManager.getInstance().cancelRegionSet(player);
                break;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDropItem(PlayerDropItemEvent event) {
        if (WinRegionManager.getInstance().isBuildingWinRegion(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickupItem(PlayerPickupItemEvent event) {
        if (WinRegionManager.getInstance().isBuildingWinRegion(event.getPlayer())) event.setCancelled(true);
    }
}