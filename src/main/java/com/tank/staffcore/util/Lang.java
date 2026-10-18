package com.tank.staffcore.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class Lang {

    private static final Set<String> reportedMissing = ConcurrentHashMap.newKeySet();

    private static Plugin plugin;
    private static File file;
    private static volatile YamlConfiguration messages = new YamlConfiguration();
    private static volatile Map<String, String> palette = new HashMap<>();

    private Lang() {
    }

    public static void init(Plugin owner) {
        plugin = owner;
        file = new File(owner.getDataFolder(), "messages.yml");

        if (!file.exists()) {
            owner.saveResource("messages.yml", false);
        }

        reload();
    }

    public static void reload() {
        YamlConfiguration defaults = loadDefaults();
        YamlConfiguration loaded = YamlConfiguration.loadConfiguration(file);
        loaded.setDefaults(defaults);

        Map<String, String> tokens = new HashMap<>();
        for (YamlConfiguration source : List.of(defaults, loaded)) {
            readSection(source, "colors", tokens);
            readSection(source, "tokens", tokens);
        }

        messages = loaded;
        palette = tokens;
        reportedMissing.clear();
    }

    public static String get(String key, Object... placeholders) {
        String template = messages.getString(key);
        if (template == null) {
            warnMissing(key);
            return key;
        }
        return render(template, placeholders);
    }

    public static List<String> getList(String key, Object... placeholders) {
        if (!messages.contains(key)) {
            warnMissing(key);
        }

        List<String> rendered = new ArrayList<>();
        for (String template : messages.getStringList(key)) {
            rendered.add(render(template, placeholders));
        }
        return rendered;
    }

    public static void send(CommandSender target, String key, Object... placeholders) {
        target.sendMessage(get(key, placeholders));
    }

    public static void sendLines(CommandSender target, String key, Object... placeholders) {
        for (String line : getList(key, placeholders)) {
            target.sendMessage(line);
        }
    }

    public static String item(String key, Object... placeholders) {
        return itemText(get(key, placeholders));
    }

    public static List<String> itemLines(String key, Object... placeholders) {
        List<String> lines = new ArrayList<>();
        for (String line : getList(key, placeholders)) {
            lines.add(itemText(line));
        }
        return lines;
    }

    private static String itemText(String rendered) {
        return rendered.isEmpty() ? rendered : Msg.color("&r") + rendered;
    }

    private static String render(String template, Object[] placeholders) {
        String text = template;

        for (Map.Entry<String, String> token : palette.entrySet()) {
            text = text.replace("{" + token.getKey() + "}", token.getValue());
        }

        text = Msg.color(text);

        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            text = text.replace("{" + placeholders[i] + "}", String.valueOf(placeholders[i + 1]));
        }

        return text;
    }

    private static void readSection(YamlConfiguration source, String name, Map<String, String> into) {
        ConfigurationSection section = source.getConfigurationSection(name);
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            into.put(key, section.getString(key, ""));
        }
    }

    private static YamlConfiguration loadDefaults() {
        try (InputStream stream = plugin.getResource("messages.yml")) {
            if (stream != null) {
                return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
            }
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not read the default messages.yml: " + exception.getMessage());
        }
        return new YamlConfiguration();
    }

    private static void warnMissing(String key) {
        if (reportedMissing.add(key)) {
            plugin.getLogger().warning("Missing message in messages.yml: " + key);
        }
    }
}