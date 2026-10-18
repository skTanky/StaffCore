package com.tank.staffcore.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class StaffModeItems {

    public static final String TELEPORT = "teleport";
    public static final String REPORTS = "reports";
    public static final String FREEZE = "freeze";
    public static final String INVSEE = "invsee";
    public static final String ONLINE_STAFF = "online_staff";
    public static final String VANISH_TOGGLE = "vanish_toggle";

    private final NamespacedKey key;

    public StaffModeItems(Plugin plugin) {
        this.key = new NamespacedKey(plugin, "staffmode_item");
    }

    public void giveKit(Player player, boolean vanished) {
        PlayerInventory inventory = player.getInventory();

        inventory.setItem(0, build(Material.RECOVERY_COMPASS, TELEPORT, "teleport"));
        inventory.setItem(1, build(Material.BOOK, REPORTS, "reports"));
        inventory.setItem(3, build(Material.ICE, FREEZE, "freeze"));
        inventory.setItem(4, build(Material.CHEST, INVSEE, "invsee"));

        ItemStack head = build(Material.PLAYER_HEAD, ONLINE_STAFF, "online-staff");
        if (head.getItemMeta() instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
            head.setItemMeta(skullMeta);
        }
        inventory.setItem(7, head);

        inventory.setItem(8, vanishToggleItem(vanished));
    }

    public ItemStack vanishToggleItem(boolean vanished) {
        if (vanished) {
            return build(Material.GRAY_DYE, VANISH_TOGGLE, "vanish-disable");
        }
        return build(Material.LIME_DYE, VANISH_TOGGLE, "vanish-enable");
    }

    public String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    private ItemStack build(Material material, String id, String configKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(Lang.item("staffmode-items." + configKey + ".name"));
        meta.setLore(Lang.itemLines("staffmode-items." + configKey + ".lore"));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);

        item.setItemMeta(meta);
        return item;
    }
}