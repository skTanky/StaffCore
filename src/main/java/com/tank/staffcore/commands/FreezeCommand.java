package com.tank.staffcore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tank.staffcore.discord.DiscordLogger;
import com.tank.staffcore.discord.DiscordLogger.Field;
import com.tank.staffcore.discord.LogType;
import com.tank.staffcore.managers.FreezeManager;
import com.tank.staffcore.util.FreezeMessages;
import com.tank.staffcore.util.Lang;
import com.tank.staffcore.util.StaffNotifier;

public class FreezeCommand implements CommandExecutor {

    private final FreezeManager freezeManager;
    private final DiscordLogger discordLogger;

    public FreezeCommand(FreezeManager freezeManager, DiscordLogger discordLogger) {
        this.freezeManager = freezeManager;
        this.discordLogger = discordLogger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Lang.get("general.console"));
            return true;
        }

        if (args.length == 0) {
            Lang.send(player, "freeze.usage");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            Lang.send(player, "general.player-not-online");
            return true;
        }

        boolean nowFrozen = !freezeManager.isFrozen(target);
        freezeManager.setFrozen(target, nowFrozen);

        discordLogger.log(LogType.FREEZE, nowFrozen ? "Player Frozen" : "Player Unfrozen",
                nowFrozen ? DiscordLogger.ON : DiscordLogger.OFF,
                new Field("Staff", player.getName()),
                new Field("Target", target.getName()));

        if (nowFrozen) {
            FreezeMessages.sendFrozenNotice(target);

            StaffNotifier.broadcast("staff.freeze", Lang.get("freeze.broadcast-frozen", "staffer", player.getName(), "target", target.getName()));
        } else {
            target.resetTitle();
            Lang.send(target, "freeze.unfrozen");

            StaffNotifier.broadcast("staff.freeze", Lang.get("freeze.broadcast-unfrozen", "staffer", player.getName(), "target", target.getName()));
        }

        return true;
    }
}