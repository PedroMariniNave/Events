package com.zpedroo.voltzevents.listeners;

import com.zpedroo.voltzevents.api.PlayerLeaveEvent;
import com.zpedroo.voltzevents.enums.LeaveReason;
import com.zpedroo.voltzevents.types.Event;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EventListener implements Listener {

    @EventHandler
    public void onLeave(PlayerLeaveEvent event) {
        Player player = event.getPlayer();
        Event eventLeft = event.getEvent();
        LeaveReason leaveReason = event.getLeaveReason();
        int participantsAmountWhenPlayerLeft = event.getParticipantsAmountWhenPlayerLeft();

        switch (leaveReason) {
            case ELIMINATED:
                eventLeft.checkIfPlayerIsWinner(player, participantsAmountWhenPlayerLeft);
                break;
            case WINNER:
                eventLeft.winEvent(player, 1);
                break;
            case EVENT_FINISHED:
            case QUIT:
                // ignore
                break;
        }
    }
}