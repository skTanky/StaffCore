package com.tank.staffcore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tank.staffcore.discord.DiscordLogger;
import com.tank.staffcore.discord.DiscordLogger.Field;
import com.tank.staffcore.discord.LogType;
import com.tank.staffcore.managers.MutechatManager;
import com.tank.staffcore.util.Lang;
import com.tank.staffcore.util.StaffNotifier;

public class MutechatCommand implements CommandExecutor {

    private final MutechatManager mutechatManager;
    private final DiscordLogger discordLogger;

    public MutechatCommand(MutechatManager mutechatManager, DiscordLogger discordLogger) {
        this.mutechatManager = mutechatManager;
        this.discordLogger = discordLogger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Lang.get("general.console"));
            return true;
        }

        boolean nowMuted = !mutechatManager.isMuted();
        mutechatManager.setMuted(nowMuted);

        discordLogger.log(LogType.MUTECHAT, nowMuted ? "Chat Muted" : "Chat Unmuted",
                nowMuted ? DiscordLogger.MUTECHAT : DiscordLogger.ON,
                new Field("Staff", player.getName()));

        if (nowMuted) {
            StaffNotifier.broadcast("staff.chat", Lang.get("mutechat.staff-muted", "player", player.getName()));
            Bukkit.broadcastMessage(Lang.get("mutechat.public-muted"));
        } else {
            StaffNotifier.broadcast("staff.chat", Lang.get("mutechat.staff-unmuted", "player", player.getName()));
            Bukkit.broadcastMessage(Lang.get("mutechat.public-unmuted"));
        }

        return true;
    }
}