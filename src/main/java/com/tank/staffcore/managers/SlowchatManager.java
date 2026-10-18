package com.tank.staffcore.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.entity.Player;

public class SlowchatManager {

    private final AtomicInteger seconds = new AtomicInteger(0);
    private final Map<UUID, Long> lastMessage = new ConcurrentHashMap<>();

    public int getSeconds() {
        return seconds.get();
    }

    public void setSeconds(int value) {
        seconds.set(value);
    }

    public boolean isOnCooldown(Player player) {
        Long last = lastMessage.get(player.getUniqueId());
        if (last == null) {
            return false;
        }
        long elapsedMillis = System.currentTimeMillis() - last;
        return elapsedMillis < (seconds.get() * 1000L);
    }

    public void recordMessage(Player player) {
        lastMessage.put(player.getUniqueId(), System.currentTimeMillis());
    }
}