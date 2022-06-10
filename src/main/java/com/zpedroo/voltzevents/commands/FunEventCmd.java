package com.zpedroo.voltzevents.commands;

import com.zpedroo.voltzevents.enums.CommandKeys;
import com.zpedroo.voltzevents.types.FunEvent;
import com.zpedroo.voltzevents.utils.config.Messages;
import com.zpedroo.voltzevents.utils.config.Settings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FunEventCmd implements CommandExecutor {

    private final FunEvent event;

    public FunEventCmd(FunEvent event) {
        this.event = event;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        if (args.length > 0) {
            CommandKeys key = CommandKeys.getCommandKey(args[0]);
            if (key != null) {
                switch (key) {
                    case START:
                        if (!sender.hasPermission(Settings.ADMIN_PERMISSION)) break;
                        if (event.isHappening()) {
                            sender.sendMessage(Messages.ALREADY_STARTED);
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

        return player == null;

//        event.join(player);
    }
}
