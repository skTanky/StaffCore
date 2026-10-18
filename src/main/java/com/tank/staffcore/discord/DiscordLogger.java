package com.tank.staffcore.discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class DiscordLogger {

    public static final int ON = 0x4FF522;
    public static final int OFF = 0xBF3636;
    public static final int REPORT = 0xFF3333;
    public static final int SLOWCHAT = 0x2798F5;
    public static final int MUTECHAT = 0xF71B1B;

    private static final List<String> ALLOWED_PREFIXES = List.of(
            "https://discord.com/api/webhooks/",
            "https://discordapp.com/api/webhooks/",
            "https://canary.discord.com/api/webhooks/",
            "https://ptb.discord.com/api/webhooks/");

    private static final int MAX_ATTEMPTS = 3;

    public record Field(String name, String value) {
    }

    private final Plugin plugin;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "StaffCore-Discord");
        thread.setDaemon(true);
        return thread;
    });

    private volatile String webhookUrl;
    private volatile String username = "StaffCore";
    private volatile String avatarUrl = "";
    private volatile Set<LogType> enabled = EnumSet.noneOf(LogType.class);
    private volatile boolean warned;

    public DiscordLogger(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration config = plugin.getConfig();

        String url = config.getString("discord.webhook-url", "");
        url = url == null ? "" : url.trim();

        boolean valid = isValidWebhook(url);
        if (!url.isEmpty() && !valid) {
            plugin.getLogger().warning("discord.webhook-url is not a valid Discord webhook URL, so Discord logging is disabled.");
        }
        this.webhookUrl = valid ? url : null;

        String name = config.getString("discord.username", "StaffCore");
        this.username = name == null || name.isBlank() ? "StaffCore" : name.trim();

        String avatar = config.getString("discord.avatar-url", "");
        this.avatarUrl = avatar == null ? "" : avatar.trim();

        Set<LogType> types = EnumSet.noneOf(LogType.class);
        for (LogType type : LogType.values()) {
            if (config.getBoolean("discord.logging." + type.getConfigKey(), true)) {
                types.add(type);
            }
        }
        this.enabled = types;
        this.warned = false;
    }

    public boolean isActive() {
        return webhookUrl != null;
    }

    public void log(LogType type, String title, int color, Field... fields) {
        String url = webhookUrl;
        if (url == null || !enabled.contains(type) || executor.isShutdown()) {
            return;
        }

        String payload = buildPayload(title, color, fields);

        try {
            executor.execute(() -> send(url, payload));
        } catch (RejectedExecutionException ignored) {
        }
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private boolean isValidWebhook(String url) {
        if (url.isEmpty()) {
            return false;
        }
        for (String prefix : ALLOWED_PREFIXES) {
            if (url.startsWith(prefix) && url.length() > prefix.length()) {
                return true;
            }
        }
        return false;
    }

    private String buildPayload(String title, int color, Field[] fields) {
        JsonArray fieldArray = new JsonArray();
        for (int i = 0; i < fields.length && i < 25; i++) {
            JsonObject field = new JsonObject();
            field.addProperty("name", truncate(fields[i].name(), 256));
            field.addProperty("value", formatValue(fields[i].value()));
            field.addProperty("inline", true);
            fieldArray.add(field);
        }

        JsonObject footer = new JsonObject();
        footer.addProperty("text", "StaffCore");

        JsonObject embed = new JsonObject();
        embed.addProperty("title", truncate(title, 256));
        embed.addProperty("color", color);
        embed.addProperty("timestamp", Instant.now().toString());
        embed.add("fields", fieldArray);
        embed.add("footer", footer);

        JsonArray embeds = new JsonArray();
        embeds.add(embed);

        JsonObject mentions = new JsonObject();
        mentions.add("parse", new JsonArray());

        JsonObject payload = new JsonObject();
        payload.addProperty("username", truncate(username, 80));
        if (!avatarUrl.isEmpty()) {
            payload.addProperty("avatar_url", avatarUrl);
        }
        payload.add("allowed_mentions", mentions);
        payload.add("embeds", embeds);

        return payload.toString();
    }

    private String formatValue(String value) {
        String cleaned = value == null ? "" : value.replace('`', '\'').trim();
        if (cleaned.isEmpty()) {
            cleaned = "N/A";
        }
        return "`" + truncate(cleaned, 1000) + "`";
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max - 1) + "…";
    }

    private void send(String url, String payload) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();

                HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
                int status = response.statusCode();

                if (status == 429) {
                    Thread.sleep(retryDelayMillis(response));
                    continue;
                }

                if (status < 200 || status >= 300) {
                    warnOnce("Discord rejected a log message (HTTP " + status + "). Check discord.webhook-url in config.yml.");
                }
                return;
            } catch (IOException exception) {
                warnOnce("Could not reach Discord (" + exception.getClass().getSimpleName() + ").");
                return;
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private long retryDelayMillis(HttpResponse<?> response) {
        double seconds = 2.0;
        String header = response.headers().firstValue("Retry-After").orElse(null);

        if (header != null) {
            try {
                seconds = Double.parseDouble(header);
            } catch (NumberFormatException ignored) {
            }
        }

        return (long) (Math.min(Math.max(seconds, 0.5), 10.0) * 1000);
    }

    private void warnOnce(String message) {
        if (!warned) {
            warned = true;
            plugin.getLogger().warning(message);
        }
    }
}