package com.tank.staffcore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tank.staffcore.discord.DiscordLogger;
import com.tank.staffcore.discord.DiscordLogger.Field;
import com.tank.staffcore.discord.LogType;
import com.tank.staffcore.managers.SlowchatManager;
import com.tank.staffcore.util.Lang;
import com.tank.staffcore.util.StaffNotifier;

public class SlowchatCommand implements CommandExecutor {
    
    private static final int MAX_SECONDS = 5;

    private final SlowchatManager slowchatManager;
    private final DiscordLogger discordLogger;

    public SlowchatCommand(SlowchatManager slowchatManager, DiscordLogger discordLogger) {
        this.slowchatManager = slowchatManager;
        this.discordLogger = discordLogger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Lang.get("general.console"));
            return true;
        }

        if (args.length == 0) {
            slowchatManager.setSeconds(0);

            discordLogger.log(LogType.SLOWCHAT, "Slowchat Disabled", DiscordLogger.OFF,
                    new Field("Staff", player.getName()));

            StaffNotifier.broadcast("staff.chat", Lang.get("slowchat.staff-disabled", "player", player.getName()));
            Bukkit.broadcastMessage(Lang.get("slowchat.public-disabled"));
            return true;
        }

        int value;
        try {
            value = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            Lang.send(player, "slowchat.invalid-number");
            return true;
        }

        if (value < 0 || value > MAX_SECONDS) {
            Lang.send(player, "slowchat.max-exceeded", "max", MAX_SECONDS);
            return true;
        }

        slowchatManager.setSeconds(value);

        discordLogger.log(LogType.SLOWCHAT, "Slowchat Set", DiscordLogger.SLOWCHAT,
                new Field("Staff", player.getName()),
                new Field("Delay", value + " seconds"));

        StaffNotifier.broadcast("staff.chat", Lang.get("slowchat.staff-set", "player", player.getName(), "value", value));
        Bukkit.broadcastMessage(Lang.get("slowchat.public-set", "value", value));

        return true;
    }
}