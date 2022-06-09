package com.zpedroo.voltzevents.events.fastcraft.listeners;

import com.zpedroo.voltzevents.events.fastcraft.FastCraftEvent;
import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.objects.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class FastCraftListeners implements Listener {

    private final FastCraftEvent fastCraftEvent = FastCraftEvent.getInstance();
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraft(CraftItemEvent event) {
        if (!fastCraftEvent.isHappening()) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() != fastCraftEvent.getCraftItem()) return;

        Player player = (Player) event.getWhoClicked();
        PlayerData data = DataManager.getInstance().getPlayerData(player);
        data.addParticipation(1);

        int position = 1;
        for (int pos = 1; pos <= FastCraftEvent.Settings.WINNERS_AMOUNT; ++pos) {
            if (!fastCraftEvent.hasWinner(pos)) {
                position = pos;
                break;
            }
        }

        fastCraftEvent.winEvent(player, position);

        if (position == fastCraftEvent.getWinnersAmount()) {
            fastCraftEvent.finishEvent(true);
        }
    }
}