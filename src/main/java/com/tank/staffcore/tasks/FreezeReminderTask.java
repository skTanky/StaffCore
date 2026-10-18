package com.tank.staffcore.tasks;

import com.tank.staffcore.managers.FreezeManager;
import com.tank.staffcore.util.FreezeMessages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FreezeReminderTask extends BukkitRunnable {

    private static final int CHAT_INTERVAL_SECONDS = 5;

    private final FreezeManager freezeManager;
    private final Map<UUID, Integer> secondsFrozen = new HashMap<>();

    public FreezeReminderTask(FreezeManager freezeManager) {
        this.freezeManager = freezeManager;
    }

    @Override
    public void run() {
        secondsFrozen.keySet().removeIf(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            return player == null || !freezeManager.isFrozen(player);
        });

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!freezeManager.isFrozen(player)) {
                continue;
            }

            int seconds = secondsFrozen.merge(player.getUniqueId(), 1, Integer::sum);

            FreezeMessages.sendTitle(player);

            if (seconds % CHAT_INTERVAL_SECONDS == 0) {
                FreezeMessages.sendChat(player);
            }
        }
    }
}