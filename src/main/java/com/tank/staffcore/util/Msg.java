package com.tank.staffcore.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

public class Msg {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    public static String color(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static String sanitize(String text) {
        return HEX_PATTERN.matcher(text).replaceAll("").replace("&", "").replace("§", "");
    }
}