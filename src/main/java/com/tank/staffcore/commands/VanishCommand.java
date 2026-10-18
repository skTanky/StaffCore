package com.tank.staffcore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.tank.staffcore.discord.DiscordLogger;
import com.tank.staffcore.discord.DiscordLogger.Field;
import com.tank.staffcore.discord.LogType;
import com.tank.staffcore.managers.VanishManager;
import com.tank.staffcore.util.Lang;
import com.tank.staffcore.util.StaffNotifier;

public class VanishCommand implements CommandExecutor {
    
    private final Plugin plugin;
    private final VanishManager vanishManager;
    private final DiscordLogger discordLogger;

    public VanishCommand(Plugin plugin, VanishManager vanishManager, DiscordLogger discordLogger) {
        this.plugin = plugin;
        this.vanishManager = vanishManager;
        this.discordLogger = discordLogger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Lang.get("general.console"));
            return true;
        }

        if (args.length == 0) {
            toggleSelf(player);
        } else {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                Lang.send(player, "general.player-not-online");
                return true;
            }

            if (!player.hasPermission("staff.vanish.other")) {
                Lang.send(player, "vanish.no-permission-other");
                return true;
            }

            toggleOther(player, target);
        }

        return true;
    }

    private void toggleSelf(Player player) {
        boolean nowVanished = !vanishManager.isVanished(player);
        vanishManager.applyVisibility(plugin, player, nowVanished);

        discordLogger.log(LogType.VANISH, nowVanished ? "Vanish Enabled" : "Vanish Disabled",
                nowVanished ? DiscordLogger.ON : DiscordLogger.OFF,
                new Field("Staff", player.getName()));

        if (nowVanished) {
            player.sendActionBar(Lang.get("vanish.actionbar"));
            Lang.send(player, "vanish.self-enabled");
            StaffNotifier.broadcast("staff.vanish", Lang.get("vanish.broadcast-enabled", "player", player.getName()));
        } else {
            Lang.send(player, "vanish.self-disabled");
            StaffNotifier.broadcast("staff.vanish", Lang.get("vanish.broadcast-disabled", "player", player.getName()));
        }
    }

    private void toggleOther(Player staffer, Player target) {
        boolean nowVanished = !vanishManager.isVanished(target);
        vanishManager.applyVisibility(plugin, target, nowVanished);

        discordLogger.log(LogType.VANISH, nowVanished ? "Vanish Enabled" : "Vanish Disabled",
                nowVanished ? DiscordLogger.ON : DiscordLogger.OFF,
                new Field("Staff", staffer.getName()),
                new Field("Target", target.getName()));

        if (nowVanished) {
            target.sendActionBar(Lang.get("vanish.actionbar"));
            Lang.send(staffer, "vanish.other-staffer-enabled");
            StaffNotifier.broadcast("staff.vanish", Lang.get("vanish.other-broadcast-enabled", "staffer", staffer.getName(), "target", target.getName()));
            Lang.send(target, "vanish.other-target-enabled", "staffer", staffer.getName());
        } else {
            Lang.send(staffer, "vanish.other-staffer-disabled");
            StaffNotifier.broadcast("staff.vanish", Lang.get("vanish.other-broadcast-disabled", "staffer", staffer.getName(), "target", target.getName()));
            Lang.send(target, "vanish.other-target-disabled", "staffer", staffer.getName());
        }
    }
}
