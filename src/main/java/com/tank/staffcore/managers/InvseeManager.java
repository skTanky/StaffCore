package com.tank.staffcore.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.tank.staffcore.util.Lang;
import com.tank.staffcore.util.StaffModeItems;

public class InvseeManager implements Listener {

    private static final String VIEW_PERMISSION = "staff.invsee";
    private static final String MODIFY_PERMISSION = "staff.invsee.modify";

    private final StaffModeItems staffModeItems;
    private final Map<UUID, UUID> viewing = new HashMap<>();

    public InvseeManager(StaffModeItems staffModeItems) {
        this.staffModeItems = staffModeItems;
    }

    public void open(Player viewer, Player target) {
        if (!viewer.hasPermission(VIEW_PERMISSION)) {
            Lang.send(viewer, "invsee.no-permission");
            return;
        }

        if (viewer.equals(target)) {
            Lang.send(viewer, "invsee.self");
            return;
        }

        viewer.openInventory(target.getInventory());
        viewing.put(viewer.getUniqueId(), target.getUniqueId());
    }

    private boolean isViewing(Player player) {
        return viewing.containsKey(player.getUniqueId());
    }

    private boolean isKitItem(ItemStack item) {
        return staffModeItems.getItemId(item) != null;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player clicker) || !isViewing(clicker)) {
            return;
        }

        if (isKitItem(event.getCurrentItem())
                || isKitItem(event.getCursor())
                || !clicker.hasPermission(MODIFY_PERMISSION)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player clicker) || !isViewing(clicker)) {
            return;
        }

        if (isKitItem(event.getOldCursor()) || !clicker.hasPermission(MODIFY_PERMISSION)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        viewing.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID quitter = event.getPlayer().getUniqueId();
        viewing.remove(quitter);

        for (Map.Entry<UUID, UUID> entry : new ArrayList<>(viewing.entrySet())) {
            if (entry.getValue().equals(quitter)) {
                Player viewer = Bukkit.getPlayer(entry.getKey());
                if (viewer != null) {
                    viewer.closeInventory();
                }
            }
        }
    }
}