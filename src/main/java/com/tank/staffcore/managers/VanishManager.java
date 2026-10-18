package com.tank.staffcore.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class VanishManager {
    
    private final Set<UUID> vanished = new HashSet<>();

    public boolean isVanished(Player player) {
        return vanished.contains(player.getUniqueId());
    }

    public void setVanished(Player player, boolean state) {
        if (state) {
            vanished.add(player.getUniqueId());
        } else {
            vanished.remove(player.getUniqueId());
        }
    }

    public void applyVisibility(Plugin plugin, Player target, boolean vanished) {
        setVanished(target, vanished);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (vanished) {
                if (!online.hasPermission("staff.vanish")) {
                    online.hidePlayer(plugin, target);
                }
            } else {
                online.showPlayer(plugin, target);
            }
        }
    }
}
