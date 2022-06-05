package com.zpedroo.voltzevents.types;

import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.objects.EventItems;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.region.CuboidRegion;
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
public abstract class ArenaEvent extends Event {

    @Nullable
    private Location arenaLocation;

    private Player player1;
    private Player player2;

    public ArenaEvent(String eventName, FileUtils.Files file, String winnerTag, HashMap<String, List<String>> messages, Map<Integer, String> winnersPosition, int winnersAmount, int minimumPlayers, boolean savePlayerInventory, boolean additionalVoidChecker, EventItems eventItems, Location joinLocation, Location exitLocation, Location arenaLocation) {
        super(eventName, file, winnerTag, messages, winnersPosition, winnersAmount, minimumPlayers, savePlayerInventory, additionalVoidChecker, eventItems, joinLocation, exitLocation);

        this.arenaLocation = arenaLocation;
    }

    public abstract CuboidRegion getWinRegion();

    public abstract void setWinRegion(CuboidRegion winRegion);

    public void setArenaLocation(Location arenaLocation) {
        this.arenaLocation = arenaLocation;
        DataManager.getInstance().saveLocationsInFile(this);
    }
}