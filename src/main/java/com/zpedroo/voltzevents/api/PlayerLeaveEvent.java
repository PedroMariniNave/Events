package com.zpedroo.voltzevents.api;

import com.zpedroo.voltzevents.types.Event;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PlayerLeaveEvent extends org.bukkit.event.Event {

    private final Player player;
    private final Event event;
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public PlayerLeaveEvent(Player player, Event event) {
        this.player = player;
        this.event = event;
    }

    public Player getPlayer() {
        return player;
    }

    public Event getEvent() {
        return event;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}