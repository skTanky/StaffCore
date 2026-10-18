package com.tank.staffcore.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.tank.staffcore.managers.MutechatManager;
import com.tank.staffcore.util.Lang;

public class MutechatListener implements Listener {
    
    private final MutechatManager mutechatManager;

    public MutechatListener(MutechatManager mutechatManager) {
        this.mutechatManager = mutechatManager;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!mutechatManager.isMuted()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.hasPermission("staff.mutechat.bypass")) {
            return;
        }

        event.setCancelled(true);
        Lang.send(player, "mutechat.blocked");
    }
}
