package com.tank.staffcore.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.tank.staffcore.managers.SlowchatManager;
import com.tank.staffcore.util.Lang;

public class SlowchatListener implements Listener {
    
    private final SlowchatManager slowchatManager;

    public SlowchatListener(SlowchatManager slowchatManager) {
        this.slowchatManager = slowchatManager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (slowchatManager.getSeconds() <= 0) {
            return;
        }

        Player player = event.getPlayer();
        if (player.hasPermission("staff.slowchat.bypass")) {
            return;
        }

        if (slowchatManager.isOnCooldown(player)) {
            event.setCancelled(true);
            Lang.send(player, "slowchat.cooldown", "seconds", slowchatManager.getSeconds());
            return;
        }

        slowchatManager.recordMessage(player);
    }
}
