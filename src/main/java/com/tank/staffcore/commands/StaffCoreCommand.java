package com.tank.staffcore.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import com.tank.staffcore.discord.DiscordLogger;
import com.tank.staffcore.util.Lang;

public class StaffCoreCommand implements CommandExecutor, TabCompleter {

    private final Plugin plugin;
    private final DiscordLogger discordLogger;

    public StaffCoreCommand(Plugin plugin, DiscordLogger discordLogger) {
        this.plugin = plugin;
        this.discordLogger = discordLogger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            Lang.send(sender, "staffcore.usage");
            return true;
        }

        if (!sender.hasPermission("staff.reload")) {
            Lang.send(sender, "general.no-permission");
            return true;
        }

        plugin.reloadConfig();
        Lang.reload();
        discordLogger.reload();

        Lang.send(sender, "staffcore.reloaded", "state", Lang.get(discordLogger.isActive() ? "staffcore.state-active" : "staffcore.state-disabled"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("staff.reload")) {
            return List.of("reload");
        }
        return List.of();
    }
}