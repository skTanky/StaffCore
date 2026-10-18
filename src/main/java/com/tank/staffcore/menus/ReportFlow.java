package com.tank.staffcore.menus;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.tank.staffcore.discord.DiscordLogger;
import com.tank.staffcore.discord.DiscordLogger.Field;
import com.tank.staffcore.discord.LogType;
import com.tank.staffcore.managers.ReasonManager;
import com.tank.staffcore.managers.ReportManager;
import com.tank.staffcore.reports.Report;
import com.tank.staffcore.reports.ReportCategory;
import com.tank.staffcore.util.Lang;
import com.tank.staffcore.util.MenuItems;

public class ReportFlow {

    private static final String VIEW_PERMISSION = "staff.reports.view";

    private static final int[] REASON_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final int[] CANCEL_SLOTS = {0, 1, 2, 9, 10, 11, 18, 19, 20};
    private static final int[] CONFIRM_SLOTS = {6, 7, 8, 15, 16, 17, 24, 25, 26};

    private final Plugin plugin;
    private final ReportManager reportManager;
    private final ReasonManager reasonManager;
    private final DiscordLogger discordLogger;

    public ReportFlow(Plugin plugin, ReportManager reportManager, ReasonManager reasonManager, DiscordLogger discordLogger) {
        this.plugin = plugin;
        this.reportManager = reportManager;
        this.reasonManager = reasonManager;
        this.discordLogger = discordLogger;
    }

    public void start(Player player, OfflinePlayer reported, String reason) {
        if (player.getUniqueId().equals(reported.getUniqueId())) {
            Lang.send(player, "reports.cannot-report-self");
            return;
        }

        if (reason != null) {
            confirm(player, reported, reason);
        } else if (reasonManager.getAllReasons().isEmpty()) {
            Lang.send(player, "reports.no-reasons");
        } else {
            selectCategory(player, reported);
        }
    }

    private void selectCategory(Player player, OfflinePlayer reported) {
        ClickMenu menu = new ClickMenu(3, Lang.get("reports.menus.category-title"));
        menu.fillAll(MenuItems.filler(Material.GRAY_STAINED_GLASS_PANE));

        menu.set(11, categoryItem(ReportCategory.CHAT),
                clicker -> selectReason(clicker, reported, ReportCategory.CHAT));
        menu.set(13, categoryItem(ReportCategory.COMBAT_MOVEMENT),
                clicker -> selectReason(clicker, reported, ReportCategory.COMBAT_MOVEMENT));
        menu.set(15, categoryItem(ReportCategory.OTHER),
                clicker -> selectReason(clicker, reported, ReportCategory.OTHER));

        menu.open(player);
    }

    private ItemStack categoryItem(ReportCategory category) {
        return MenuItems.rendered(Material.CHEST, Lang.item("reports.category-item." + category.getKey()));
    }

    private void selectReason(Player player, OfflinePlayer reported, ReportCategory category) {
        List<String> reasons = reasonManager.getReasons(category);
        String categoryName = Lang.get("reports.categories." + category.getKey());

        if (reasons.isEmpty()) {
            Lang.send(player, "reports.empty-category", "category", categoryName);
            return;
        }

        ClickMenu menu = new ClickMenu(6, Lang.get("reports.menus.reason-title", "category", categoryName));
        menu.fillAll(MenuItems.filler(Material.GRAY_STAINED_GLASS_PANE));
        menu.fill(MenuItems.filler(Material.STONE_BUTTON), REASON_SLOTS);

        menu.set(49, MenuItems.rendered(Material.ARROW, Lang.item("reports.menus.back")),
                clicker -> selectCategory(clicker, reported));

        for (int i = 0; i < reasons.size() && i < REASON_SLOTS.length; i++) {
            String reason = reasons.get(i);
            menu.set(REASON_SLOTS[i],
                    MenuItems.rendered(Material.BOOK,
                            Lang.item("reports.menus.reason-item", "reason", reason, "reason-upper", reason.toUpperCase())),
                    clicker -> confirm(clicker, reported, reason));
        }

        menu.open(player);
    }

    private void confirm(Player player, OfflinePlayer reported, String reason) {
        String reportedName = nameOf(reported);

        ClickMenu menu = new ClickMenu(3, Lang.get("reports.menus.confirm-title", "player", reportedName));
        menu.fillAll(MenuItems.filler(Material.GRAY_STAINED_GLASS_PANE));

        menu.set(13, MenuItems.renderedHead(reported,
                Lang.item("reports.menus.confirm-head-name", "player", reportedName),
                Lang.itemLines("reports.menus.confirm-head-lore", "reason", reason)));

        ItemStack cancel = MenuItems.rendered(Material.RED_STAINED_GLASS_PANE, Lang.item("reports.menus.cancel"));
        for (int slot : CANCEL_SLOTS) {
            menu.set(slot, cancel, Player::closeInventory);
        }

        ItemStack confirmPane = MenuItems.rendered(Material.LIME_STAINED_GLASS_PANE, Lang.item("reports.menus.confirm"));
        for (int slot : CONFIRM_SLOTS) {
            menu.set(slot, confirmPane, clicker -> {
                submit(clicker, reportedName, reason);
                clicker.closeInventory();
            });
        }

        menu.open(player);
    }

    private void submit(Player reporter, String reportedName, String reason) {
        String id = reportManager.generateId();

        Lang.send(reporter, "reports.thank-you");

        List<Player> staff = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission(VIEW_PERMISSION)) {
                staff.add(online);
            }
        }

        List<String> alert = Lang.getList("reports.staff-alert",
                "id", id,
                "reported", reportedName,
                "reporter", reporter.getName(),
                "reason", reason);

        for (Player member : staff) {
            for (String line : alert) {
                member.sendMessage(line);
            }
            playAlert(member);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player member : staff) {
                if (member.isOnline()) {
                    playAlert(member);
                }
            }
        }, 10L);

        reportManager.add(new Report(id, reportedName, reporter.getName(), reason, staff.size(), System.currentTimeMillis()));

        discordLogger.log(LogType.REPORT_CREATED, "New Report #" + id, DiscordLogger.REPORT,
                new Field("Report ID", "#" + id),
                new Field("Reported", reportedName),
                new Field("Reporter", reporter.getName()),
                new Field("Reason", reason),
                new Field("Staff Online", String.valueOf(staff.size())));
    }

    private void playAlert(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3f, 2.0f);
    }

    private String nameOf(OfflinePlayer player) {
        return player.getName() != null ? player.getName() : "Unknown";
    }
}