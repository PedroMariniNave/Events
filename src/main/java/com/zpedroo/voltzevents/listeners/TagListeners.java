package com.zpedroo.voltzevents.listeners;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import com.zpedroo.voltzevents.managers.DataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TagListeners implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(ChatMessageEvent event) {
        Player player = event.getSender();
        DataManager.getInstance().getCache().getEvents().forEach(events -> {
            if (events.getWinnerPosition(player.getName()) != 1) return;

            event.setTagValue(events.getName().toLowerCase(), events.getWinnerTag());
        });
    }
}