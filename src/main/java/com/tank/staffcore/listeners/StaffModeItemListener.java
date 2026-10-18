package com.tank.staffcore.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.tank.staffcore.managers.InvseeManager;
import com.tank.staffcore.managers.StaffModeManager;
import com.tank.staffcore.managers.VanishManager;
import com.tank.staffcore.menus.OnlineStaffMenu;
import com.tank.staffcore.util.Lang;
import com.tank.staffcore.util.StaffModeItems;

public class StaffModeItemListener implements Listener {

    private static final int TARGET_RANGE = 20;
    private static final long VANISH_COOLDOWN_MILLIS = 2000L;

    private final StaffModeManager staffModeManager;
    private final StaffModeItems staffModeItems;
    private final VanishManager vanishManager;
    private final OnlineStaffMenu onlineStaffMenu;
    private final InvseeManager invseeManager;
    private final Map<UUID, Long> vanishCooldowns = new HashMap<>();

    public StaffModeItemListener(StaffModeManager staffModeManager, StaffModeItems staffModeItems, VanishManager vanishManager, OnlineStaffMenu onlineStaffMenu, InvseeManager invseeManager) {
        this.staffModeManager = staffModeManager;
        this.staffModeItems = staffModeItems;
        this.vanishManager = vanishManager;
        this.onlineStaffMenu = onlineStaffMenu;
        this.invseeManager = invseeManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (!staffModeManager.isInStaffMode(player)) {
            return;
        }

        String id = staffModeItems.getItemId(event.getItem());
        if (id == null && !vanishManager.isVanished(player)) {
            return;
        }

        event.setCancelled(true);

        if (id == null) {
            return;
        }

        Entity looking = player.getTargetEntity(TARGET_RANGE);
        Player target = looking instanceof Player targetPlayer ? targetPlayer : null;
        handle(player, id, target);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        if (!staffModeManager.isInStaffMode(player)) {
            return;
        }

        String id = staffModeItems.getItemId(player.getInventory().getItemInMainHand());
        if (id == null && !vanishManager.isVanished(player)) {
            return;
        }

        event.setCancelled(true);

        if (id == null) {
            return;
        }

        Player target = event.getRightClicked() instanceof Player clicked ? clicked : null;
        handle(player, id, target);
    }

    private void handle(Player player, String id, Player target) {
        switch (id) {
            case StaffModeItems.TELEPORT -> teleportRandom(player);
            case StaffModeItems.REPORTS -> player.performCommand("reports");
            case StaffModeItems.FREEZE -> {
                if (target != null) {
                    player.performCommand("freeze " + target.getName());
                }
            }
            case StaffModeItems.INVSEE -> {
                if (target != null) {
                    invseeManager.open(player, target);
                }
            }
            case StaffModeItems.VANISH_TOGGLE -> toggleVanish(player);
            case StaffModeItems.ONLINE_STAFF -> onlineStaffMenu.open(player);
            default -> { }
        }
    }

    private void teleportRandom(Player player) {
        List<Player> others = new ArrayList<>(Bukkit.getOnlinePlayers());
        others.remove(player);

        if (others.isEmpty()) {
            Lang.send(player, "staffmode.nobody-to-teleport");
            return;
        }

        Player chosen = others.get(ThreadLocalRandom.current().nextInt(others.size()));
        player.teleport(chosen);
    }

    private void toggleVanish(Player player) {
        long now = System.currentTimeMillis();
        Long last = vanishCooldowns.get(player.getUniqueId());
        if (last != null && now - last < VANISH_COOLDOWN_MILLIS) {
            return;
        }
        vanishCooldowns.put(player.getUniqueId(), now);

        player.performCommand("vanish");
        player.getInventory().setItemInMainHand(staffModeItems.vanishToggleItem(vanishManager.isVanished(player)));
    }
}
