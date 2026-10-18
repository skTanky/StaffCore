package com.tank.staffcore.listeners;

import com.tank.staffcore.menus.ClickMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class MenuListener implements Listener {

    private final Plugin plugin;
    
    public MenuListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ClickMenu menu)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= menu.getInventory().getSize()) {
            return;
        }

        Consumer<Player> action = menu.getAction(slot);
        if (action == null) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.getOpenInventory().getTopInventory().getHolder() != menu) {
                return;
            }
            action.accept(player);
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ClickMenu) {
            event.setCancelled(true);
        }
    }
}
