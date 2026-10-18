package com.tank.staffcore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.tank.staffcore.discord.DiscordLogger;
import com.tank.staffcore.discord.DiscordLogger.Field;
import com.tank.staffcore.discord.LogType;
import com.tank.staffcore.managers.StaffModeManager;
import com.tank.staffcore.managers.VanishManager;
import com.tank.staffcore.util.Lang;
import com.tank.staffcore.util.StaffModeItems;
import com.tank.staffcore.util.StaffNotifier;

public class StaffModeCommand implements CommandExecutor {
    
    private final Plugin plugin;
    private final StaffModeManager staffModeManager;
    private final StaffModeItems staffModeItems;
    private final VanishManager vanishManager;
    private final DiscordLogger discordLogger;

    public StaffModeCommand(Plugin plugin, StaffModeManager staffModeManager, StaffModeItems staffModeItems, VanishManager vanishManager, DiscordLogger discordLogger) {
        this.plugin = plugin;
        this.staffModeManager = staffModeManager;
        this.staffModeItems = staffModeItems;
        this.vanishManager = vanishManager;
        this.discordLogger = discordLogger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Lang.get("general.console"));
            return true;
        }

        if (staffModeManager.isInStaffMode(player)) {
            exit(player);
        } else {
            enter(player);
        }

        return true;
    }

    private void enter(Player player) {
        if (!staffModeManager.saveInventory(player)) {
            Lang.send(player, "staffmode.backup-failed");
            return;
        }

        player.getInventory().clear();
        vanishManager.applyVisibility(plugin, player, true);
        staffModeItems.giveKit(player, true);

        Lang.send(player, "staffmode.toggled-on");
        StaffNotifier.broadcast("staff.mode", Lang.get("staffmode.broadcast-enabled", "player", player.getName()));

        discordLogger.log(LogType.STAFFMODE, "Staffmode Enabled", DiscordLogger.ON,
                new Field("Staff", player.getName()));
    }

    private void exit(Player player) {
        if (!staffModeManager.restoreInventory(player)) {
            Lang.send(player, "staffmode.restore-failed");
            return;
        }

        vanishManager.applyVisibility(plugin, player, false);

        Lang.send(player, "staffmode.toggled-off");
        StaffNotifier.broadcast("staff.mode", Lang.get("staffmode.broadcast-disabled", "player", player.getName()));

        discordLogger.log(LogType.STAFFMODE, "Staffmode Disabled", DiscordLogger.OFF,
                new Field("Staff", player.getName()));
    }
}
