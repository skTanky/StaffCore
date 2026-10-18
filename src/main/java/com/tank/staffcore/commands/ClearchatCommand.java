package com.tank.staffcore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tank.staffcore.util.Lang;

public class ClearchatCommand implements CommandExecutor {
    
    private static final int BLANK_LINES = 1000;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < BLANK_LINES; i++) {
                online.sendMessage(" ");
            }
        }

        Bukkit.broadcastMessage(Lang.get("clearchat.cleared", "player", sender.getName()));
        return true;
    }
}
