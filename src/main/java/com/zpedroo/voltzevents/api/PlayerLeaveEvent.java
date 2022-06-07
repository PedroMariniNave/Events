package com.zpedroo.voltzevents.api;

import com.zpedroo.voltzevents.enums.LeaveReason;
import com.zpedroo.voltzevents.types.Event;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PlayerLeaveEvent extends org.bukkit.event.Event {

    private final Player player;
    private final Event event;
    private final LeaveReason leaveReason;
    private final int participantsAmountWhenPlayerLeft;
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public PlayerLeaveEvent(Player player, Event event, LeaveReason leaveReason, int participantsAmountWhenPlayerLeft) {
        this.player = player;
        this.event = event;
        this.leaveReason = leaveReason;
        this.participantsAmountWhenPlayerLeft = participantsAmountWhenPlayerLeft;
    }

    public Player getPlayer() {
        return player;
    }

    public Event getEvent() {
        return event;
    }

    public LeaveReason getLeaveReason() {
        return leaveReason;
    }

    public int getParticipantsAmountWhenPlayerLeft() {
        return participantsAmountWhenPlayerLeft;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}