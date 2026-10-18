package com.tank.staffcore.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.tank.staffcore.managers.StaffModeManager;
import com.tank.staffcore.util.Lang;

public class StaffModeJoinQuitListener implements Listener {

    private final StaffModeManager staffModeManager;

    public StaffModeJoinQuitListener(StaffModeManager staffModeManager) {
        this.staffModeManager = staffModeManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (staffModeManager.isInStaffMode(player)) {
            staffModeManager.restoreInventory(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (staffModeManager.hasSavedInventory(player) && staffModeManager.restoreInventory(player)) {
            Lang.send(player, "staffmode.inventory-restored");
        }
    }
}