package com.tank.staffcore.managers;

import com.tank.staffcore.reports.Report;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ReportManager {
    
    private static final long COOLDOWN_MILLIS = 30_0000L;

    private final Plugin plugin;
    private final File file;
    private final Map<String, Report> reports = new LinkedHashMap<>();
    private final Map<UUID, Long> lastReport = new HashMap<>();

    public ReportManager(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "reports.yml");
        load();
    }

    public List<Report> getAll() {
        return new ArrayList<>(reports.values());
    }

    public Report get(String id) {
        return reports.get(id);
    }

    public int size() {
        return reports.size();
    }

    public void add(Report report) {
        reports.put(report.id(), report);
        save();
    }

    public boolean remove(String id) {
        boolean removed = reports.remove(id) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public void removeAll() {
        reports.clear();
        save();
    }

    public String generateId() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String id;
        do {
            char first = (char) ('A' + random.nextInt(26));
            char second = (char) ('A' + random.nextInt(26));
            id = "" + first + second + random.nextInt(1111, 10000);
        } while (reports.containsKey(id));
        return id;
    }

    public boolean isOnCooldown(Player player) {
        Long last = lastReport.get(player.getUniqueId());
        return last != null && System.currentTimeMillis() - last < COOLDOWN_MILLIS;
    }

    public void startCooldown(Player player) {
        lastReport.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private void load() {
        if (!file.exists()) {
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("reports");
        if (section == null) {
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(id);
            if (entry == null) {
                continue;
            }

            reports.put(id, new Report(
                    id,
                    entry.getString("reported", "Unknown"),
                    entry.getString("reporter", "Unknown"),
                    entry.getString("reason", "Unknown"),
                    entry.getInt("staff-online"),
                    entry.getLong("submitted-at")));
        }
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();

        for (Report report : reports.values()) {
            String path = "reports." + report.id();
            yaml.set(path + ".reported", report.reportedName());
            yaml.set(path + ".reporter", report.reporterName());
            yaml.set(path + ".reason", report.reason());
            yaml.set(path + ".staff-online", report.staffOnline());
            yaml.set(path + ".submitted-at", report.submittedAt());
        }

        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save reports file: " + exception.getMessage());
        }
    }
}
