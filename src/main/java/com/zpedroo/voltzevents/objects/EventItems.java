package com.zpedroo.voltzevents.objects;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data
public class EventItems {

    private final ItemStack[] inventoryItems;
    private final ItemStack[] armorItems;
}