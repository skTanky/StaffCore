package com.tank.staffcore.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.tank.staffcore.managers.ReasonManager;
import com.tank.staffcore.managers.ReportManager;
import com.tank.staffcore.menus.ReportFlow;
import com.tank.staffcore.util.Lang;
import com.tank.staffcore.util.Msg;

public class ReportCommand implements CommandExecutor, TabCompleter {
    
    private final ReportManager reportManager;
    private final ReasonManager reasonManager;
    private final ReportFlow reportFlow;

    public ReportCommand(ReportManager reportManager, ReasonManager reasonManager, ReportFlow reportFlow) {
        this.reportManager = reportManager;
        this.reasonManager = reasonManager;
        this.reportFlow = reportFlow;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Lang.get("general.console"));
            return true;
        }

        if (args.length == 0) {
            Lang.send(player, "reports.usage-player");
            return true;
        }

        OfflinePlayer target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            target = Bukkit.getOfflinePlayerIfCached(args[0]);
        }
        if (target == null) {
            Lang.send(player, "reports.player-not-found");
            return true;
        }

        if (reportManager.isOnCooldown(player)) {
            Lang.send(player, "reports.cooldown");
            return true;
        }
        reportManager.startCooldown(player);

        String reason = null;
        if (args.length > 1) {
            String typed = Msg.sanitize(String.join(" ", Arrays.copyOfRange(args, 1, args.length))).trim();
            if (!typed.isEmpty()) {
                reason = typed;
            }
        }

        reportFlow.start(player, target, reason);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return null;
        }

        if (args.length == 2) {
            String typed = args[1].toLowerCase();
            List<String> matches = new ArrayList<>();
            for (String reason : reasonManager.getAllReasons()) {
                if (reason.toLowerCase().startsWith(typed)) {
                    matches.add(reason);
                }
            }
            return matches;
        }

        return List.of();
    }
}
