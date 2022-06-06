package com.zpedroo.voltzevents.types;

import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.objects.player.EventItems;
import com.zpedroo.voltzevents.utils.FileUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class PvPEvent extends Event {

    @Nullable
    private Location pos1Location;
    @Nullable
    private Location pos2Location;

    private Player player1;
    private Player player2;

    public PvPEvent(String eventName, FileUtils.Files file, String winnerTag, HashMap<String, List<String>> messages, Map<Integer, String> winnersPosition, int winnersAmount, int minimumPlayers, boolean savePlayerInventory, boolean additionalVoidChecker, EventItems eventItems, Location joinLocation, Location exitLocation, Location pos1Location, Location pos2Location) {
        super(eventName, file, winnerTag, messages, winnersPosition, winnersAmount, minimumPlayers, savePlayerInventory, additionalVoidChecker, eventItems, joinLocation, exitLocation);

        this.pos1Location = pos1Location;
        this.pos2Location = pos2Location;
    }

    public boolean isFighting(Player player) {
        if (player1 == null || player2 == null) return false;

        return player1.equals(player) || player2.equals(player);
    }

    public void setPos2Location(Location pos2Location) {
        this.pos2Location = pos2Location;
        DataManager.getInstance().saveLocationsInFile(this);
    }

    public void setPos1Location(Location pos1Location) {
        this.pos1Location = pos1Location;
        DataManager.getInstance().saveLocationsInFile(this);
    }
}