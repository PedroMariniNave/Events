package com.zpedroo.voltzevents.commands;

import com.zpedroo.voltzevents.enums.CommandKeys;
import com.zpedroo.voltzevents.enums.LeaveReason;
import com.zpedroo.voltzevents.events.hotpotato.HotPotatoEvent;
import com.zpedroo.voltzevents.managers.WinRegionManager;
import com.zpedroo.voltzevents.types.ArenaEvent;
import com.zpedroo.voltzevents.utils.config.Messages;
import com.zpedroo.voltzevents.utils.config.Settings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArenaEventCmd implements CommandExecutor {

    private final ArenaEvent event;

    public ArenaEventCmd(ArenaEvent event) {
        this.event = event;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        if (args.length > 0) {
            CommandKeys key = CommandKeys.getCommandKey(args[0]);
            if (key != null) {
                switch (key) {
                    case SET_JOIN:
                        if (player == null || !player.hasPermission(Settings.ADMIN_PERMISSION)) break;

                        event.setJoinLocation(player.getLocation());
                        return true;
                    case SET_EXIT:
                        if (player == null || !player.hasPermission(Settings.ADMIN_PERMISSION)) break;

                        event.setExitLocation(player.getLocation());
                        return true;
                    case SET_ARENA:
                        if (player == null || !player.hasPermission(Settings.ADMIN_PERMISSION)) break;

                        event.setArenaLocation(player.getLocation());
                        return true;
                    case SET_WIN_REGION:
                        if (player == null || !player.hasPermission(Settings.ADMIN_PERMISSION)) break;

                        if (WinRegionManager.getInstance().isBuildingWinRegion(player)) {
                            WinRegionManager.getInstance().cancelRegionSet(player);
                        } else {
                            WinRegionManager.getInstance().startRegionSet(player, event);
                        }
                        return true;
                    case SET_ITEMS:
                        if (player == null || !player.hasPermission(Settings.ADMIN_PERMISSION)) break;

                        ItemStack[] inventoryItems = player.getInventory().getContents();
                        ItemStack[] armorItems = player.getInventory().getArmorContents();

                        event.setEventItems(inventoryItems, armorItems);
                        player.getInventory().clear();
                        player.getInventory().setArmorContents(new ItemStack[4]);
                        return true;
                    case START:
                        if (!sender.hasPermission(Settings.ADMIN_PERMISSION)) break;
                        if (event.isHappening()) {
                            sender.sendMessage(Messages.ALREADY_STARTED);
                            return true;
                        }

                        if (event.getJoinLocation() == null || event.getExitLocation() == null || (event.getArenaLocation() == null && !(event instanceof HotPotatoEvent))) {
                            sender.sendMessage(Messages.INVALID_LOCATION);
                            return true;
                        }

                        event.startEvent();
                        return true;
                    case CANCEL:
                        if (!sender.hasPermission(Settings.ADMIN_PERMISSION)) break;
                        if (!event.isHappening()) {
                            sender.sendMessage(Messages.NOT_STARTED);
                            return true;
                        }

                        event.cancelEvent();
                        return true;
                }
            }
        }

        if (!event.isParticipating(player)) {
            event.join(player);
        } else {
            event.leave(player, LeaveReason.QUIT, true, true);
        }
        return false;
    }
}