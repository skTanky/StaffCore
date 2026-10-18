package com.tank.staffcore.menus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import com.tank.staffcore.util.Lang;
import com.tank.staffcore.util.MenuItems;
import com.tank.staffcore.util.Msg;

import net.kyori.adventure.text.Component;

public class AnvilPrompt implements Listener {

    private static final int RESULT_SLOT = 2;

    private final Plugin plugin;
    private final NamespacedKey promptKey;
    private final Map<UUID, Consumer<String>> pending = new HashMap<>();

    public AnvilPrompt(Plugin plugin) {
        this.plugin = plugin;
        this.promptKey = new NamespacedKey(plugin, "anvil_prompt");
    }

    public void open(Player player, String title, Consumer<String> onSubmit) {
        AnvilView view = MenuType.ANVIL.create(player, Component.text(title));
        view.setItem(0, promptItem());
        player.openInventory(view);
        pending.put(player.getUniqueId(), onSubmit);
    }

    private ItemStack promptItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        meta.getPersistentDataContainer().set(promptKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    private boolean isPromptItem(ItemStack item) {
        return item != null
                && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(promptKey, PersistentDataType.BYTE);
    }

    private String clean(String text) {
        return text == null ? "" : Msg.sanitize(text).trim();
    }

    @EventHandler
    public void onPrepare(PrepareAnvilEvent event) {
        AnvilView view = event.getView();
        if (!isPromptItem(view.getItem(0))) {
            return;
        }

        String text = clean(view.getRenameText());
        if (text.isEmpty()) {
            event.setResult(null);
            return;
        }

        event.setResult(MenuItems.rendered(Material.PAPER,
                Lang.item("reports.anvil.result-name", "text", text),
                Lang.itemLines("reports.anvil.result-lore")));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView() instanceof AnvilView view) || !isPromptItem(view.getItem(0))) {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() != RESULT_SLOT || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Consumer<String> callback = pending.get(player.getUniqueId());
        if (callback == null) {
            return;
        }

        String text = clean(view.getRenameText());
        if (text.isEmpty()) {
            return;
        }

        pending.remove(player.getUniqueId());
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.closeInventory();
            callback.accept(text);
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView() instanceof AnvilView view && isPromptItem(view.getItem(0))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getView() instanceof AnvilView view) || !isPromptItem(view.getItem(0))) {
            return;
        }

        view.setItem(0, null);
        pending.remove(event.getPlayer().getUniqueId());
    }
}