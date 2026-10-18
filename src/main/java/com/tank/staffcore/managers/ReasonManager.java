package com.tank.staffcore.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.tank.staffcore.reports.ReportCategory;

public class ReasonManager {
    public static final int MAX_PER_CATEGORY = 28;

    private final Plugin plugin;
    private final File file;
    private final Map<ReportCategory, List<String>> reasons = new EnumMap<>(ReportCategory.class);

    public ReasonManager(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "reasons.yml");

        for (ReportCategory category : ReportCategory.values()) {
            reasons.put(category, new ArrayList<>());
        }

        load();
    }

    public List<String> getReasons(ReportCategory category) {
        return new ArrayList<>(reasons.get(category));
    }

    public List<String> getAllReasons() {
        List<String> all = new ArrayList<>();
        for (ReportCategory category : ReportCategory.values()) {
            all.addAll(reasons.get(category));
        }

        return all;
    }

    public boolean exists(String reason) {
        for (String existing : getAllReasons()) {
            if (existing.equalsIgnoreCase(reason)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFull(ReportCategory category) {
        return reasons.get(category).size() >= MAX_PER_CATEGORY;
    }

    public boolean add(ReportCategory category, String reason) {
        if (exists(reason) || isFull(category)) {
            return false;
        }

        reasons.get(category).add(reason);
        save();
        return true;
    }

    private void load() {
        if (!file.exists()) {
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (ReportCategory category : ReportCategory.values()) {
            reasons.get(category).addAll(yaml.getStringList(category.name()));
        }
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (ReportCategory category : ReportCategory.values()) {
            yaml.set(category.name(), reasons.get(category));
        }

        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save report reasons file: " + exception.getMessage());
        }
    }
}
