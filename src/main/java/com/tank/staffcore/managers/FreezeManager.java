package com.tank.staffcore.managers;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FreezeManager {

    private final Set<UUID> frozen = ConcurrentHashMap.newKeySet();

    public boolean isFrozen(Player player) {
        return frozen.contains(player.getUniqueId());
    }

    public void setFrozen(Player player, boolean state) {
        if (state) {
            frozen.add(player.getUniqueId());
        } else {
            frozen.remove(player.getUniqueId());
        }
    }
}