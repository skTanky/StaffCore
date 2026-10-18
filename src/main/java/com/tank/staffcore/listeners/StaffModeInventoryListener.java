package com.tank.staffcore.listeners;

import com.tank.staffcore.managers.StaffModeManager;
import com.tank.staffcore.util.StaffModeItems;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class StaffModeInventoryListener implements Listener {

    private final StaffModeManager staffModeManager;
    private final StaffModeItems staffModeItems;

    public StaffModeInventoryListener(StaffModeManager staffModeManager, StaffModeItems staffModeItems) {
        this.staffModeManager = staffModeManager;
        this.staffModeItems = staffModeItems;
    }

    private boolean isKitItem(ItemStack item) {
        return staffModeItems.getItemId(item) != null;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!staffModeManager.isInStaffMode(player)) {
            return;
        }

        if (isKitItem(event.getCurrentItem()) || isKitItem(event.getCursor())) {
            event.setCancelled(true);
            return;
        }

        if (event.getClick() == ClickType.NUMBER_KEY) {
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
            if (isKitItem(hotbarItem)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        if (!staffModeManager.isInStaffMode(event.getPlayer())) {
            return;
        }

        if (isKitItem(event.getMainHandItem()) || isKitItem(event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }
}