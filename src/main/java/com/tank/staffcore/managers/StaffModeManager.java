package com.tank.staffcore.managers;

import com.tank.staffcore.util.StaffModeItems;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StaffModeManager {

    private final Plugin plugin;
    private final StaffModeItems staffModeItems;
    private final File folder;
    private final Set<UUID> inStaffMode = ConcurrentHashMap.newKeySet();

    public StaffModeManager(Plugin plugin, StaffModeItems staffModeItems) {
        this.plugin = plugin;
        this.staffModeItems = staffModeItems;
        this.folder = new File(plugin.getDataFolder(), "savedInventories");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    private File fileFor(UUID uuid) {
        return new File(folder, uuid + ".yml");
    }

    public boolean isInStaffMode(Player player) {
        return inStaffMode.contains(player.getUniqueId());
    }

    public Set<UUID> getStaffModeUuids() {
        return inStaffMode;
    }

    public boolean hasSavedInventory(Player player) {
        return fileFor(player.getUniqueId()).exists();
    }

    public boolean saveInventory(Player player) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("contents", player.getInventory().getContents());

        try {
            yaml.save(fileFor(player.getUniqueId()));
        } catch (IOException exception) {
            plugin.getLogger().severe("Failed to save inventory for " + player.getName() + ": " + exception.getMessage());
            return false;
        }

        inStaffMode.add(player.getUniqueId());
        return true;
    }

    public boolean restoreInventory(Player player) {
        UUID uuid = player.getUniqueId();
        File file = fileFor(uuid);

        if (!file.exists()) {
            inStaffMode.remove(uuid);
            return false;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        List<?> list = yaml.getList("contents");
        if (list == null) {
            plugin.getLogger().severe("Saved inventory file for " + player.getName() + " is unreadable, leaving it in place: " + file.getName());
            return false;
        }

        ItemStack[] contents = new ItemStack[list.size()];
        for (int i = 0; i < list.size(); i++) {
            contents[i] = list.get(i) instanceof ItemStack stack ? stack : null;
        }

        List<ItemStack> extras = collectExtras(player);

        player.getInventory().setContents(contents);

        for (ItemStack extra : extras) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(extra);
            for (ItemStack overflow : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), overflow);
            }
        }

        inStaffMode.remove(uuid);

        if (!file.delete()) {
            plugin.getLogger().warning("Could not delete " + file.getName() + " after restoring it.");
        }
        return true;
    }

    private List<ItemStack> collectExtras(Player player) {
        boolean hasKit = false;
        List<ItemStack> extras = new ArrayList<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) {
                continue;
            }

            if (staffModeItems.getItemId(item) != null) {
                hasKit = true;
            } else {
                extras.add(item.clone());
            }
        }

        return hasKit ? extras : List.of();
    }
}