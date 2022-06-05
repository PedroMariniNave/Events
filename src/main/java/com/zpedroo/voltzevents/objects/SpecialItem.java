package com.zpedroo.voltzevents.objects;

import de.tr7zw.nbtapi.NBTItem;
import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data
public class SpecialItem {

    private final String identifier;
    private final String action;
    private final ItemStack defaultItem;
    private final ItemStack secondaryItem;

    public ItemStack getDefaultItem() {
        if (defaultItem == null) return null;

        NBTItem nbt = new NBTItem(defaultItem.clone());
        nbt.setString("SpecialItem", identifier);

        return nbt.getItem();
    }

    public ItemStack getSecondaryItem() {
        if (secondaryItem == null) return null;

        NBTItem nbt = new NBTItem(secondaryItem.clone());
        nbt.setString("SpecialItem", identifier);

        return nbt.getItem();
    }
}