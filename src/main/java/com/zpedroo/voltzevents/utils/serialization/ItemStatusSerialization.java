package com.zpedroo.voltzevents.utils.serialization;

import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.objects.event.SpecialItem;

import java.util.HashMap;
import java.util.Map;

public class ItemStatusSerialization {

    public static String serialize(Map<SpecialItem, Boolean> specialItemsStatus) {
        StringBuilder serialized = new StringBuilder(8);
        for (Map.Entry<SpecialItem, Boolean> entry : specialItemsStatus.entrySet()) {
            SpecialItem specialItem = entry.getKey();
            boolean status = entry.getValue();

            serialized.append(specialItem.getIdentifier()).append(":");
            serialized.append(status).append(",");
        }

        return serialized.toString();
    }

    public static Map<SpecialItem, Boolean> deserialize(String serialized) {
        if (serialized == null || serialized.isEmpty()) return new HashMap<>(2);

        Map<SpecialItem, Boolean> ret = new HashMap<>(2);
        String[] split = serialized.split(",");
        for (String str : split) {
            String[] itemSplit = str.split(":");
            if (itemSplit.length < 2) continue;

            SpecialItem specialItem = DataManager.getInstance().getSpecialItem(itemSplit[0]);
            boolean status = Boolean.getBoolean(itemSplit[1]);

            ret.put(specialItem, status);
        }

        return ret;
    }
}