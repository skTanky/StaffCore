package com.tank.staffcore.listeners;

import com.tank.staffcore.commands.StaffChatCommand;
import com.tank.staffcore.managers.StaffChatManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.EventPriority;

public class StaffChatListener implements Listener {
    
    private final Plugin plugin;
    private final StaffChatManager staffChatManager;
    private final StaffChatCommand staffChatCommand;

    public StaffChatListener(Plugin plugin, StaffChatManager staffChatManager, StaffChatCommand staffChatCommand) {
        this.plugin = plugin;
        this.staffChatManager = staffChatManager;
        this.staffChatCommand = staffChatCommand;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!staffChatManager.isToggled(player)) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage();

        Bukkit.getScheduler().runTask(plugin, () -> staffChatCommand.broadcast(player, message));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("staff.chat")) {
            staffChatManager.setToggled(player, false);
        }
    }
}
