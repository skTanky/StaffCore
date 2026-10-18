package com.tank.staffcore.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.tank.staffcore.managers.VanishManager;
import com.tank.staffcore.util.Lang;

public class VanishReminderTask extends BukkitRunnable {

    private final VanishManager vanishManager;

    public VanishReminderTask(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (vanishManager.isVanished(player)) {
                player.sendActionBar(Lang.get("vanish.actionbar"));
            }
        }
    }
}