package com.tank.staffcore.menus;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.tank.staffcore.discord.DiscordLogger;
import com.tank.staffcore.discord.DiscordLogger.Field;
import com.tank.staffcore.discord.LogType;
import com.tank.staffcore.managers.ReasonManager;
import com.tank.staffcore.managers.ReportManager;
import com.tank.staffcore.reports.Report;
import com.tank.staffcore.reports.ReportCategory;
import com.tank.staffcore.util.Lang;
import com.tank.staffcore.util.MenuItems;

public class ReportsMenu {

    private static final String DELETE_PERMISSION = "staff.reports.delete";
    private static final String DELETE_ALL_PERMISSION = "staff.reports.deleteall";
    private static final String CREATE_REASON_PERMISSION = "staff.reports.createreasonreport";

    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    private final ReportManager reportManager;
    private final ReasonManager reasonManager;
    private final AnvilPrompt anvilPrompt;
    private final DiscordLogger discordLogger;

    public ReportsMenu(ReportManager reportManager, ReasonManager reasonManager, AnvilPrompt anvilPrompt, DiscordLogger discordLogger) {
        this.reportManager = reportManager;
        this.reasonManager = reasonManager;
        this.anvilPrompt = anvilPrompt;
        this.discordLogger = discordLogger;
    }

    public void open(Player player, int requestedPage) {
        List<Report> all = reportManager.getAll();
        int totalPages = Math.max(1, (int) Math.ceil(all.size() / (double) SLOTS.length));
        int page = Math.min(Math.max(requestedPage, 1), totalPages);

        ClickMenu menu = new ClickMenu(6, Lang.get("reports.staff-menu.title", "page", page, "pages", totalPages));
        menu.fillAll(MenuItems.filler(Material.GRAY_STAINED_GLASS_PANE));
        menu.fill(MenuItems.filler(Material.STONE_BUTTON), SLOTS);

        if (page > 1) {
            menu.set(45, MenuItems.rendered(Material.ARROW, Lang.item("reports.staff-menu.previous")),
                    clicker -> open(clicker, page - 1));
        } else {
            menu.set(45, MenuItems.rendered(Material.ARROW, Lang.item("reports.staff-menu.no-previous")));
        }

        if (page < totalPages) {
            menu.set(53, MenuItems.rendered(Material.ARROW, Lang.item("reports.staff-menu.next")),
                    clicker -> open(clicker, page + 1));
        } else {
            menu.set(53, MenuItems.rendered(Material.ARROW, Lang.item("reports.staff-menu.no-next")));
        }

        if (player.hasPermission(CREATE_REASON_PERMISSION)) {
            menu.set(46, MenuItems.rendered(Material.WRITABLE_BOOK,
                            Lang.item("reports.staff-menu.create-reason-name"),
                            Lang.itemLines("reports.staff-menu.create-reason-lore")),
                    clicker -> startCreateReason(clicker, page));
        }

        menu.set(49, MenuItems.rendered(Material.TNT, Lang.item("reports.staff-menu.delete-all")),
                this::deleteAll);

        int start = (page - 1) * SLOTS.length;
        for (int i = 0; i < SLOTS.length && start + i < all.size(); i++) {
            Report report = all.get(start + i);
            menu.set(SLOTS[i], buildBook(report), clicker -> delete(clicker, report, page));
        }

        menu.open(player);
    }

    private ItemStack buildBook(Report report) {
        String date = DATE_FORMAT.format(Instant.ofEpochMilli(report.submittedAt()));

        return MenuItems.rendered(Material.BOOK,
                Lang.item("reports.staff-menu.report-name", "id", report.id()),
                Lang.itemLines("reports.staff-menu.report-lore",
                        "reported", report.reportedName(),
                        "reporter", report.reporterName(),
                        "reason", report.reason(),
                        "staff", report.staffOnline(),
                        "date", date));
    }

    private void delete(Player player, Report report, int page) {
        if (!player.hasPermission(DELETE_PERMISSION)) {
            Lang.send(player, "reports.staff-menu.delete-no-permission");
            return;
        }

        if (reportManager.remove(report.id())) {
            discordLogger.log(LogType.REPORT_DELETED, "Report Deleted #" + report.id(), DiscordLogger.OFF,
                    new Field("Report ID", "#" + report.id()),
                    new Field("Reported", report.reportedName()),
                    new Field("Reporter", report.reporterName()),
                    new Field("Reason", report.reason()),
                    new Field("Deleted By", player.getName()));
        }

        Lang.send(player, "reports.staff-menu.deleted", "id", report.id());
        open(player, page);
    }

    private void deleteAll(Player player) {
        if (!player.hasPermission(DELETE_ALL_PERMISSION)) {
            Lang.send(player, "reports.staff-menu.delete-all-no-permission");
            return;
        }

        int count = reportManager.size();
        reportManager.removeAll();

        if (count > 0) {
            discordLogger.log(LogType.REPORTS_CLEARED, "All Reports Deleted", DiscordLogger.OFF,
                    new Field("Deleted By", player.getName()),
                    new Field("Reports Deleted", String.valueOf(count)));
        }

        Lang.send(player, "reports.staff-menu.deleted-all");
        open(player, 1);
    }

    private void startCreateReason(Player player, int page) {
        if (!player.hasPermission(CREATE_REASON_PERMISSION)) {
            Lang.send(player, "reports.staff-menu.create-no-permission");
            return;
        }

        anvilPrompt.open(player, Lang.get("reports.staff-menu.anvil-title"), reason -> {
            if (reasonManager.exists(reason)) {
                Lang.send(player, "reports.staff-menu.reason-exists");
                open(player, page);
                return;
            }
            pickCategory(player, reason, page);
        });
    }

    private void pickCategory(Player player, String reason, int page) {
        ClickMenu menu = new ClickMenu(3, Lang.get("reports.staff-menu.category-title"));
        menu.fillAll(MenuItems.filler(Material.GRAY_STAINED_GLASS_PANE));

        menu.set(11, categoryItem(ReportCategory.CHAT, reason),
                clicker -> register(clicker, reason, ReportCategory.CHAT, page));
        menu.set(13, categoryItem(ReportCategory.COMBAT_MOVEMENT, reason),
                clicker -> register(clicker, reason, ReportCategory.COMBAT_MOVEMENT, page));
        menu.set(15, categoryItem(ReportCategory.OTHER, reason),
                clicker -> register(clicker, reason, ReportCategory.OTHER, page));
        menu.set(22, MenuItems.rendered(Material.ARROW, Lang.item("reports.staff-menu.category-cancel")),
                clicker -> open(clicker, page));

        menu.open(player);
    }

    private ItemStack categoryItem(ReportCategory category, String reason) {
        return MenuItems.rendered(Material.CHEST,
                Lang.item("reports.category-item." + category.getKey()),
                Lang.itemLines("reports.staff-menu.category-lore", "reason", reason));
    }

    private void register(Player player, String reason, ReportCategory category, int page) {
        String categoryName = Lang.get("reports.categories." + category.getKey());

        if (reasonManager.isFull(category)) {
            Lang.send(player, "reports.staff-menu.category-full", "category", categoryName);
        } else if (reasonManager.add(category, reason)) {
            Lang.send(player, "reports.staff-menu.registered", "reason", reason, "category", categoryName);
        } else {
            Lang.send(player, "reports.staff-menu.reason-exists");
        }

        open(player, page);
    }
}