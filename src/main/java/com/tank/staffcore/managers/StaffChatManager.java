package com.tank.staffcore.managers;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

public class StaffChatManager {
    
    private final Set<UUID> toggled = ConcurrentHashMap.newKeySet();

    public boolean isToggled(Player player) {
        return toggled.contains(player.getUniqueId());
    }

    public void setToggled(Player player, boolean state) {
        if (state) {
            toggled.add(player.getUniqueId());
        } else {
            toggled.remove(player.getUniqueId());
        }
    }
}
