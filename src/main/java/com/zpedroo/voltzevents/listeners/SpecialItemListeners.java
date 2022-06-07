package com.zpedroo.voltzevents.listeners;

import com.zpedroo.voltzevents.enums.LeaveReason;
import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.objects.player.PlayerData;
import com.zpedroo.voltzevents.objects.event.SpecialItem;
import com.zpedroo.voltzevents.types.ArenaEvent;
import com.zpedroo.voltzevents.types.Event;
import com.zpedroo.voltzevents.utils.config.Messages;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SpecialItemListeners implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (event.getItem() == null || event.getItem().getType().equals(Material.AIR)) return;

        ItemStack item = event.getItem().clone();
        NBTItem nbt = new NBTItem(item);
        if (!nbt.hasKey("SpecialItem")) return;

        event.setCancelled(true);

        String identifier = nbt.getString("SpecialItem");
        SpecialItem specialItem = DataManager.getInstance().getSpecialItem(identifier);
        if (specialItem == null) return;

        Player player = event.getPlayer();
        Event participatingEvent = DataManager.getInstance().getPlayerParticipatingEvent(player);
        if (participatingEvent == null) return;

        String action = specialItem.getAction();
        switch (action.toUpperCase()) {
            case "SWITCH_VISIBILITY":
                PlayerData data = DataManager.getInstance().getPlayerData(player);
                boolean status = data.getSpecialItemStatus(specialItem);

                data.setSpecialItemsStatus(specialItem, !status);
                participatingEvent.updatePlayerView(player);
                break;
            case "CHECKPOINT":
                if (!(participatingEvent instanceof ArenaEvent)) break;
                if (!participatingEvent.isStarted()) {
                    player.sendMessage(Messages.INVALID_CHECKPOINT);
                    return;
                }

                ArenaEvent arenaEvent = (ArenaEvent) participatingEvent;
                player.teleport(arenaEvent.getArenaLocation());
                break;
            case "LEAVE":
                participatingEvent.leave(player, LeaveReason.QUIT, true);
                return;
        }

        participatingEvent.setPlayerSpecialItems(player);
    }
}