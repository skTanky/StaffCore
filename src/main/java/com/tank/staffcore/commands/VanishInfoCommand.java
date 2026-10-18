package com.tank.staffcore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tank.staffcore.managers.VanishManager;
import com.tank.staffcore.util.Lang;

public class VanishInfoCommand implements CommandExecutor {
    
    private final VanishManager vanishManager;

    public VanishInfoCommand(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Lang.get("general.console"));
            return true;
        }

        Player target;
        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                Lang.send(player, "general.player-not-online");
                return true;
            }
        } else {
            target = player;
        }

        String key = vanishManager.isVanished(target) ? "vanish.info-vanished" : "vanish.info-visible";
        Lang.send(player, key, "player", target.getName());
        return true;
    }
}
