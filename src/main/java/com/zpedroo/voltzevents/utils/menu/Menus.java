package com.zpedroo.voltzevents.utils.menu;

import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.objects.PlayerData;
import com.zpedroo.voltzevents.utils.FileUtils;
import com.zpedroo.voltzevents.utils.builder.InventoryBuilder;
import com.zpedroo.voltzevents.utils.builder.InventoryUtils;
import com.zpedroo.voltzevents.utils.builder.ItemBuilder;
import com.zpedroo.voltzevents.utils.color.Colorize;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Menus extends InventoryUtils {

    private static Menus instance;
    public static Menus getInstance() { return instance; }

    public Menus() {
        instance = this;
    }

    public void openMainMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.MAIN;

        String title = Colorize.getColored(FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);
        PlayerData data = DataManager.getInstance().getPlayerData(player);

        for (String items : FileUtils.get().getSection(file, "Inventory.items")) {
            String action = FileUtils.get().getString(file, "Inventory.items." + items + ".action");
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + items, new String[]{
                    "{player}",
                    "{wins}",
                    "{participations}",
                    "{win_rate}"
            }, new String[]{
                    player.getName(),
                    String.valueOf(data.getWinsAmount()),
                    String.valueOf(data.getParticipationsAmount()),
                    String.format("%.0f", data.getWinRate())
            }).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + items + ".slot");

            inventory.addItem(item, slot, () -> {
                switch (action.toUpperCase()) {
                    case "EVENTS":
                        openEventsMenu(player);
                        break;
                    case "TOP":
                        openTopMenu(player);
                        break;
                }
            }, InventoryUtils.ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }

    public void openEventsMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.EVENTS;

        String title = Colorize.getColored(FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String items : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + items).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + items + ".slot");

            inventory.addItem(item, slot);
        }

        inventory.open(player);
    }

    public void openTopMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.TOP;

        String title = Colorize.getColored(FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        int pos = 0;
        String[] slots = FileUtils.get().getString(file, "Inventory.slots").replace(" ", "").split(",");

        for (PlayerData data : DataManager.getInstance().getTopWins()) {
            if (++pos > slots.length) break;

            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Item", new String[]{
                    "{player}",
                    "{wins}",
                    "{participations}",
                    "{win_rate}",
                    "{pos}"
            }, new String[]{
                    Bukkit.getOfflinePlayer(data.getUniqueId()).getName(),
                    String.valueOf(data.getWinsAmount()),
                    String.valueOf(data.getParticipationsAmount()),
                    String.format("%.0f", data.getWinRate()),
                    String.valueOf(pos)
            }).build();

            int slot = Integer.parseInt(slots[pos-1]);

            inventory.addItem(item, slot);
        }

        inventory.open(player);
    }
}