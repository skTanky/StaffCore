package com.tank.staffcore.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class StaffNotifier {
    public static void broadcast(String permission, String message) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission(permission)) {
                online.sendMessage(message);
            }
        }
    }
}
