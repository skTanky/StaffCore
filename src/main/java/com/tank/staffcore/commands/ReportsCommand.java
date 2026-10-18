package com.tank.staffcore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tank.staffcore.menus.ReportsMenu;
import com.tank.staffcore.util.Lang;

public class ReportsCommand implements CommandExecutor {

    private final ReportsMenu reportsMenu;

    public ReportsCommand(ReportsMenu reportsMenu) {
        this.reportsMenu = reportsMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Lang.get("general.console"));
            return true;
        }

        if (!player.hasPermission("staff.reports.view")) {
            Lang.send(player, "general.no-permission");
            return true;
        }

        reportsMenu.open(player, 1);
        return true;
    }
}