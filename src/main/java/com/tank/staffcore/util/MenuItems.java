package com.tank.staffcore.util;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class MenuItems {

    public static ItemStack filler(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Msg.color("&b"));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack rendered(Material material, String name) {
        return rendered(material, name, List.of());
    }

    public static ItemStack rendered(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        if (!lore.isEmpty()) {
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack renderedHead(OfflinePlayer owner, String name, List<String> lore) {
        ItemStack item = rendered(Material.PLAYER_HEAD, name, lore);
        if (item.getItemMeta() instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(owner);
            item.setItemMeta(skullMeta);
        }
        return item;
    }
}