package com.tank.staffcore.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import com.tank.staffcore.managers.VanishManager;

public class VanishJoinQuitListener implements Listener {
    
    private final Plugin plugin;
    private final VanishManager vanishManager;

    public VanishJoinQuitListener(Plugin plugin, VanishManager vanishManager) {
        this.plugin = plugin;
        this.vanishManager = vanishManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();

        if (joined.hasPermission("staff.vanish")) {
            return;
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (vanishManager.isVanished(online)) {
                joined.hidePlayer(plugin, online);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player quitter = event.getPlayer();
        if (vanishManager.isVanished(quitter)) {
            vanishManager.setVanished(quitter, false);
        }
    }
}
