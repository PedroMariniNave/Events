package com.zpedroo.voltzevents.objects.event;

import com.zpedroo.voltzevents.types.ArenaEvent;
import com.zpedroo.voltzevents.utils.region.CuboidRegion;
import lombok.Data;
import org.bukkit.Location;

@Data
public class WinRegionBuilder {

    private final ArenaEvent event;
    private Location location1;
    private Location location2;

    public CuboidRegion build() {
        if (location1 == null || location2 == null) return null;

        return new CuboidRegion(location1, location2);
    }
}