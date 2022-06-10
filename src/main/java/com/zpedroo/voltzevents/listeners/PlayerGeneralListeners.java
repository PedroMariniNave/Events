package com.zpedroo.voltzevents.listeners;

import com.zpedroo.voltzevents.VoltzEvents;
import com.zpedroo.voltzevents.enums.LeaveReason;
import com.zpedroo.voltzevents.managers.DataManager;
import com.zpedroo.voltzevents.types.ArenaEvent;
import com.zpedroo.voltzevents.types.Event;
import com.zpedroo.voltzevents.utils.config.Messages;
import com.zpedroo.voltzevents.utils.config.Settings;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

public class PlayerGeneralListeners implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        DataManager.getInstance().savePlayerData(player);
        Event participatingEvent = DataManager.getInstance().getPlayerParticipatingEvent(player);
        if (participatingEvent != null) participatingEvent.leave(player, LeaveReason.QUIT, true, true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!Settings.DISABLE_INVENTORY_INTERACTION) return;

        Player player = (Player) event.getWhoClicked();
        Event participatingEvent = DataManager.getInstance().getPlayerParticipatingEvent(player);
        if (participatingEvent == null || !participatingEvent.isSavePlayerInventory()) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDropItem(PlayerDropItemEvent event) {
        if (!Settings.DISABLE_ITEM_DROP) return;

        Event participatingEvent = DataManager.getInstance().getPlayerParticipatingEvent(event.getPlayer());
        if (participatingEvent == null || !participatingEvent.isSavePlayerInventory()) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPickupItem(PlayerPickupItemEvent event) {
        if (!Settings.DISABLE_ITEM_PICKUP) return;

        Event participatingEvent = DataManager.getInstance().getPlayerParticipatingEvent(event.getPlayer());
        if (participatingEvent == null || !participatingEvent.isSavePlayerInventory()) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!Settings.DISABLE_BLOCK_PLACE) return;

        Event participatingEvent = DataManager.getInstance().getPlayerParticipatingEvent(event.getPlayer());
        if (participatingEvent != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!Settings.DISABLE_BLOCK_BREAK) return;

        Event participatingEvent = DataManager.getInstance().getPlayerParticipatingEvent(event.getPlayer());
        if (participatingEvent != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Event participatingEvent = DataManager.getInstance().getPlayerParticipatingEvent(player);
        if (participatingEvent != null && !participatingEvent.isStarted()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Event participatingEvent = DataManager.getInstance().getPlayerParticipatingEvent(player);
        if (participatingEvent != null && !participatingEvent.isStarted()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVoidDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID) return;

        Player player = (Player) event.getEntity();
        Event participatingEvent = DataManager.getInstance().getPlayerParticipatingEvent(player);
        if (participatingEvent == null) return;

        event.setCancelled(true);

        if (participatingEvent instanceof ArenaEvent && participatingEvent.isStarted()) {
            ArenaEvent arenaEvent = (ArenaEvent) participatingEvent;
            player.teleport(arenaEvent.getArenaLocation());
        } else {
            player.teleport(participatingEvent.getJoinLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTeleport(PlayerTeleportEvent event) {
        Event participatingEvent = DataManager.getInstance().getPlayerParticipatingEvent(event.getPlayer());
        if (participatingEvent != null && !participatingEvent.isStarted()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {
        Event participatingEvent = DataManager.getInstance().getPlayerParticipatingEvent(event.getEntity());
        if (participatingEvent != null && participatingEvent.isSavePlayerInventory()) event.getDrops().clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Event participatingEvent = DataManager.getInstance().getPlayerParticipatingEvent(player);
        if (participatingEvent == null || player.hasPermission(Settings.ADMIN_PERMISSION)) return;

        String executedCommand = event.getMessage().split(" ")[0].replace("/", "").toLowerCase();
        if (participatingEvent.getWhitelistedCommands().contains(executedCommand)) return;

        PluginCommand pluginCommand = Bukkit.getPluginCommand(executedCommand);
        if (pluginCommand != null && pluginCommand.getPlugin() instanceof VoltzEvents) return;

        event.setCancelled(true);
        player.sendMessage(Messages.BLACKLISTED_COMMAND);
    }
}