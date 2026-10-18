package com.tank.staffcore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tank.staffcore.managers.StaffChatManager;
import com.tank.staffcore.util.Lang;

public class StaffChatCommand implements CommandExecutor {
    
    private final StaffChatManager staffChatManager;

    public StaffChatCommand(StaffChatManager staffChatManager) {
        this.staffChatManager = staffChatManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Lang.get("general.console"));
            return true;
        }

        if (args.length == 0) {
            boolean nowToggled = !staffChatManager.isToggled(player);
            staffChatManager.setToggled(player, nowToggled);

            Lang.send(player, nowToggled ? "staffchat.toggled-on" : "staffchat.toggled-off");
            return true;
        }

        String message = String.join(" ", args);
        broadcast(player, message);
        return true;
    }

    public void broadcast(Player player, String message) {
        String line = Lang.get("staffchat.format", "player", player.getName(), "message", message);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("staff.chat")) {
                online.sendMessage(line);
            }
        }
    }
}
