package com.tank.staffcore.menus;

import com.tank.staffcore.managers.StaffModeManager;
import com.tank.staffcore.managers.VanishManager;
import com.tank.staffcore.util.Lang;
import com.tank.staffcore.util.MenuItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OnlineStaffMenu {

    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final StaffModeManager staffModeManager;
    private final VanishManager vanishManager;

    public OnlineStaffMenu(StaffModeManager staffModeManager, VanishManager vanishManager) {
        this.staffModeManager = staffModeManager;
        this.vanishManager = vanishManager;
    }

    public void open(Player viewer) {
        ClickMenu menu = new ClickMenu(5, Lang.get("online-staff.title"));
        menu.fill(MenuItems.filler(Material.STONE_BUTTON), SLOTS);

        List<Player> staff = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("staff.chat") && !online.equals(viewer)) {
                staff.add(online);
            }
        }

        for (int i = 0; i < staff.size() && i < SLOTS.length; i++) {
            Player member = staff.get(i);
            UUID memberId = member.getUniqueId();

            String vanished = Lang.get(vanishManager.isVanished(member) ? "online-staff.yes" : "online-staff.no");
            String staffMode = Lang.get(staffModeManager.isInStaffMode(member) ? "online-staff.yes" : "online-staff.no");

            menu.set(SLOTS[i],
                    MenuItems.renderedHead(member,
                            Lang.item("online-staff.head-name", "player", member.getName()),
                            Lang.itemLines("online-staff.head-lore", "vanished", vanished, "staffmode", staffMode)),
                    clicker -> teleport(clicker, memberId));
        }

        menu.open(viewer);
    }

    private void teleport(Player player, UUID targetId) {
        player.closeInventory();

        Player target = Bukkit.getPlayer(targetId);
        if (target == null) {
            Lang.send(player, "online-staff.not-online");
            return;
        }

        player.teleport(target);
        Lang.send(player, "online-staff.teleported", "player", target.getName());
    }
}