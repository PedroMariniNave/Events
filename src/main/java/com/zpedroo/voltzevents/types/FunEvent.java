package com.zpedroo.voltzevents.types;

import com.zpedroo.voltzevents.objects.EventItems;
import com.zpedroo.voltzevents.utils.FileUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class FunEvent extends Event {

    public FunEvent(String eventName, FileUtils.Files file, String winnerTag, HashMap<String, List<String>> messages, Map<Integer, String> winnersPosition, int winnersAmount) {
        this(eventName, file, winnerTag, messages, winnersPosition, winnersAmount, true, null);
    }

    public FunEvent(String eventName, FileUtils.Files file, String winnerTag, HashMap<String, List<String>> messages, Map<Integer, String> winnersPosition, int winnersAmount, boolean savePlayerInventory, EventItems eventItems) {
        super(eventName, file, winnerTag, messages, winnersPosition, winnersAmount, 0, savePlayerInventory, false, eventItems, null, null);
    }

    @Override
    public void teleportPlayersToArenaAndExecuteEventActions() {
    }

    @Override
    public void resetAllValues() {
    }
}
