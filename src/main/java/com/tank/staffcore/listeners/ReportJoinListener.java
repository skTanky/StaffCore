package com.tank.staffcore.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import com.tank.staffcore.managers.ReportManager;
import com.tank.staffcore.util.Lang;

public class ReportJoinListener implements Listener {

    private final Plugin plugin;
    private final ReportManager reportManager;

    public ReportJoinListener(Plugin plugin, ReportManager reportManager) {
        this.plugin = plugin;
        this.reportManager = reportManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("staff.reports.view")) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int count = reportManager.size();
            if (!player.isOnline() || count == 0) {
                return;
            }

            Lang.send(player, count == 1 ? "reports.join-reminder-one" : "reports.join-reminder-many", "count", count);
        }, 40L);
    }
}