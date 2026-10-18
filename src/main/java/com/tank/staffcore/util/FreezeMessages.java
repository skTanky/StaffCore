package com.tank.staffcore.util;

import org.bukkit.entity.Player;

public class FreezeMessages {

    public static void sendFrozenNotice(Player target) {
        sendChat(target);
        sendTitle(target);
    }

    public static void sendChat(Player target) {
        Lang.sendLines(target, "freeze.notice");
    }

    public static void sendTitle(Player target) {
        target.sendTitle(Lang.get("freeze.title"), Lang.get("freeze.subtitle"), 0, 40, 10);
    }
}