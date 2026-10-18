package com.tank.staffcore.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.tank.staffcore.managers.FreezeManager;
import com.tank.staffcore.util.FreezeMessages;
import com.tank.staffcore.util.Lang;
import com.tank.staffcore.util.StaffNotifier;

public class FreezeListener implements Listener {

    private final FreezeManager freezeManager;

    public FreezeListener(FreezeManager freezeManager) {
        this.freezeManager = freezeManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!freezeManager.isFrozen(player)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }

        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            Location locked = from.clone();
            locked.setYaw(to.getYaw());
            locked.setPitch(to.getPitch());
            event.setTo(locked);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player victim && freezeManager.isFrozen(victim)) {
            event.setCancelled(true);
            return;
        }

        if (event instanceof EntityDamageByEntityEvent damageByEntity
                && damageByEntity.getDamager() instanceof Player attacker
                && freezeManager.isFrozen(attacker)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (freezeManager.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (freezeManager.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (freezeManager.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (freezeManager.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        if (freezeManager.isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!freezeManager.isFrozen(player) || player.hasPermission("staff.freeze")) {
            return;
        }

        event.setCancelled(true);
        Lang.send(player, "freeze.commands-blocked");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (freezeManager.isFrozen(player)) {
            FreezeMessages.sendFrozenNotice(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!freezeManager.isFrozen(player)) {
            return;
        }

        StaffNotifier.broadcast("staff.freeze", Lang.get("freeze.logged-out", "player", player.getName()));
    }
}