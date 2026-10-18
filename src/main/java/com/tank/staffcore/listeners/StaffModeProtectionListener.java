package com.tank.staffcore.listeners;

import com.tank.staffcore.managers.StaffModeManager;
import com.tank.staffcore.managers.VanishManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class StaffModeProtectionListener implements Listener {

    private final StaffModeManager staffModeManager;
    private final VanishManager vanishManager;

    private static final int STORAGE_START = 9;
    private static final int STORAGE_END = 35;

    public StaffModeProtectionListener(StaffModeManager staffModeManager, VanishManager vanishManager) {
        this.staffModeManager = staffModeManager;
        this.vanishManager = vanishManager;
    }

    private boolean isRestricted(Player player) {
        return staffModeManager.isInStaffMode(player) && vanishManager.isVanished(player);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player victim && staffModeManager.isInStaffMode(victim)) {
            event.setCancelled(true);
            return;
        }

        if (event instanceof EntityDamageByEntityEvent damageByEntity
                && damageByEntity.getDamager() instanceof Player attacker
                && isRestricted(attacker)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (isRestricted(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (isRestricted(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();

        if (isRestricted(player)) {
            event.setCancelled(true);
            return;
        }

        if (!staffModeManager.isInStaffMode(player)) {
            return;
        }

        event.setCancelled(true);

        Item dropped = event.getItem();
        ItemStack stack = dropped.getItemStack();
        int leftover = addToStorage(player, stack);

        if (leftover == stack.getAmount()) {
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2f, 1.0f);

        if (leftover <= 0) {
            dropped.remove();
        } else {
            stack.setAmount(leftover);
            dropped.setItemStack(stack);
        }
    }

    private int addToStorage(Player player, ItemStack stack) {
        PlayerInventory inventory = player.getInventory();
        int remaining = stack.getAmount();
        int maxSize = stack.getMaxStackSize();

        for (int slot = STORAGE_START; slot <= STORAGE_END && remaining > 0; slot++) {
            ItemStack existing = inventory.getItem(slot);
            if (existing != null && existing.isSimilar(stack) && existing.getAmount() < maxSize) {
                int moved = Math.min(maxSize - existing.getAmount(), remaining);
                existing.setAmount(existing.getAmount() + moved);
                inventory.setItem(slot, existing);
                remaining -= moved;
            }
        }

        for (int slot = STORAGE_START; slot <= STORAGE_END && remaining > 0; slot++) {
            ItemStack existing = inventory.getItem(slot);
            if (existing == null || existing.getType().isAir()) {
                int moved = Math.min(maxSize, remaining);
                ItemStack placed = stack.clone();
                placed.setAmount(moved);
                inventory.setItem(slot, placed);
                remaining -= moved;
            }
        }

        return remaining;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (staffModeManager.isInStaffMode(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}