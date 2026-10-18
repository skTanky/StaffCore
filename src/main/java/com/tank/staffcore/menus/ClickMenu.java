package com.tank.staffcore.menus;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class ClickMenu implements InventoryHolder {

    private final Inventory inventory;
    private final Map<Integer, Consumer<Player>> actions = new HashMap<>();

    public ClickMenu(int rows, String title) {
        this.inventory = Bukkit.createInventory(this, rows * 9, Component.text(title));
    }

    public static int[] range(int from, int to) {
        return IntStream.rangeClosed(from, to).toArray();
    }

    public void set(int slot, ItemStack item) {
        inventory.setItem(slot, item);
        actions.remove(slot);
    }

    public void set(int slot, ItemStack item, Consumer<Player> action) {
        inventory.setItem(slot, item);
        actions.put(slot, action);
    }

    public void fill(ItemStack item, int... slots) {
        for (int slot : slots) {
            set(slot, item);
        }
    }

    public void fillAll(ItemStack item) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            set(slot, item);
        }
    }

    public Consumer<Player> getAction(int slot) {
        return actions.get(slot);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}