package com.tank.staffcore.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import com.tank.staffcore.managers.VanishManager;

public class VanishProtectionListener implements Listener {
    
    private final VanishManager vanishManager;

    public VanishProtectionListener(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player victim && vanishManager.isVanished(victim)) {
            event.setCancelled(true);
            return;
        }

        if (event instanceof EntityDamageByEntityEvent damageByEntity
                && damageByEntity.getDamager() instanceof Player attacker
                && vanishManager.isVanished(attacker)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (vanishManager.isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (vanishManager.isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        if (vanishManager.isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (vanishManager.isVanished(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
