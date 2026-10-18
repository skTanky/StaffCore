package com.tank.staffcore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.tank.staffcore.commands.ClearchatCommand;
import com.tank.staffcore.commands.FreezeCommand;
import com.tank.staffcore.commands.MutechatCommand;
import com.tank.staffcore.commands.ReportCommand;
import com.tank.staffcore.commands.ReportsCommand;
import com.tank.staffcore.commands.SlowchatCommand;
import com.tank.staffcore.commands.StaffChatCommand;
import com.tank.staffcore.commands.StaffCoreCommand;
import com.tank.staffcore.commands.StaffModeCommand;
import com.tank.staffcore.commands.VanishCommand;
import com.tank.staffcore.commands.VanishInfoCommand;
import com.tank.staffcore.discord.DiscordLogger;
import com.tank.staffcore.listeners.FreezeListener;
import com.tank.staffcore.listeners.MenuListener;
import com.tank.staffcore.listeners.MutechatListener;
import com.tank.staffcore.listeners.SlowchatListener;
import com.tank.staffcore.listeners.StaffChatListener;
import com.tank.staffcore.listeners.StaffModeInventoryListener;
import com.tank.staffcore.listeners.StaffModeItemListener;
import com.tank.staffcore.listeners.StaffModeJoinQuitListener;
import com.tank.staffcore.listeners.StaffModeProtectionListener;
import com.tank.staffcore.listeners.VanishJoinQuitListener;
import com.tank.staffcore.listeners.VanishProtectionListener;
import com.tank.staffcore.managers.FreezeManager;
import com.tank.staffcore.managers.InvseeManager;
import com.tank.staffcore.managers.MutechatManager;
import com.tank.staffcore.managers.ReasonManager;
import com.tank.staffcore.managers.ReportManager;
import com.tank.staffcore.managers.SlowchatManager;
import com.tank.staffcore.managers.StaffChatManager;
import com.tank.staffcore.managers.StaffModeManager;
import com.tank.staffcore.managers.VanishManager;
import com.tank.staffcore.menus.AnvilPrompt;
import com.tank.staffcore.menus.OnlineStaffMenu;
import com.tank.staffcore.menus.ReportFlow;
import com.tank.staffcore.menus.ReportsMenu;
import com.tank.staffcore.tasks.FreezeReminderTask;
import com.tank.staffcore.tasks.VanishReminderTask;
import com.tank.staffcore.util.StaffModeItems;
import com.tank.staffcore.util.Lang;

public class StaffCore extends JavaPlugin {

    private StaffModeManager staffModeManager;
    private DiscordLogger discordLogger;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Lang.init(this);
        discordLogger = new DiscordLogger(this);
        discordLogger = new DiscordLogger(this);

        VanishManager vanishManager = new VanishManager();
        StaffChatManager staffChatManager = new StaffChatManager();
        SlowchatManager slowchatManager = new SlowchatManager();
        MutechatManager mutechatManager = new MutechatManager();
        StaffModeItems staffModeItems = new StaffModeItems(this);
        staffModeManager = new StaffModeManager(this, staffModeItems);
        InvseeManager invseeManager = new InvseeManager(staffModeItems);
        ReasonManager reasonManager = new ReasonManager(this);
        ReportManager reportManager = new ReportManager(this);

        OnlineStaffMenu onlineStaffMenu = new OnlineStaffMenu(staffModeManager, vanishManager);

        StaffChatCommand staffChatCommand = new StaffChatCommand(staffChatManager);
        ReportFlow reportFlow = new ReportFlow(this, reportManager, reasonManager, discordLogger);
        ReportCommand reportCommand = new ReportCommand(reportManager, reasonManager, reportFlow);

        AnvilPrompt anvilPrompt = new AnvilPrompt(this);
        ReportsMenu reportsMenu = new ReportsMenu(reportManager, reasonManager, anvilPrompt, discordLogger);

        FreezeManager freezeManager = new FreezeManager();
        StaffCoreCommand staffCoreCommand = new StaffCoreCommand(this, discordLogger);

        getCommand("vanish").setExecutor(new VanishCommand(this, vanishManager, discordLogger));
        getCommand("vanishinfo").setExecutor(new VanishInfoCommand(vanishManager));
        getCommand("staffchat").setExecutor(staffChatCommand);
        getCommand("slowchat").setExecutor(new SlowchatCommand(slowchatManager, discordLogger));
        getCommand("mutechat").setExecutor(new MutechatCommand(mutechatManager, discordLogger));
        getCommand("clearchat").setExecutor(new ClearchatCommand());
        getCommand("staffmode").setExecutor(new StaffModeCommand(this, staffModeManager, staffModeItems, vanishManager, discordLogger));
        getCommand("report").setExecutor(reportCommand);
        getCommand("report").setTabCompleter(reportCommand);
        getCommand("reports").setExecutor(new ReportsCommand(reportsMenu));
        getCommand("freeze").setExecutor(new FreezeCommand(freezeManager, discordLogger));
        getCommand("staffcore").setExecutor(staffCoreCommand);
        getCommand("staffcore").setTabCompleter(staffCoreCommand);

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new VanishProtectionListener(vanishManager), this);
        pluginManager.registerEvents(new VanishJoinQuitListener(this, vanishManager), this);
        pluginManager.registerEvents(new StaffChatListener(this, staffChatManager, staffChatCommand), this);
        pluginManager.registerEvents(new MutechatListener(mutechatManager), this);
        pluginManager.registerEvents(new SlowchatListener(slowchatManager), this);
        pluginManager.registerEvents(new StaffModeProtectionListener(staffModeManager, vanishManager), this);
        pluginManager.registerEvents(new StaffModeInventoryListener(staffModeManager, staffModeItems), this);
        pluginManager.registerEvents(new StaffModeJoinQuitListener(staffModeManager), this);
        pluginManager.registerEvents(new StaffModeItemListener(staffModeManager, staffModeItems, vanishManager, onlineStaffMenu, invseeManager), this);
        pluginManager.registerEvents(invseeManager, this);
        pluginManager.registerEvents(new MenuListener(this), this);
        pluginManager.registerEvents(anvilPrompt, this);
        pluginManager.registerEvents(new FreezeListener(freezeManager), this);

        new VanishReminderTask(vanishManager).runTaskTimer(this, 0L, 20L);
        new FreezeReminderTask(freezeManager).runTaskTimer(this, 0L, 20L);

        getLogger().info("StaffCore has successfully loaded!");
    }

    @Override
    public void onDisable() {
        if (staffModeManager != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (staffModeManager.isInStaffMode(player)) {
                    staffModeManager.restoreInventory(player);
                }
            }
        }

        if (discordLogger != null) {
            discordLogger.shutdown();
        }

        getLogger().info("StaffCore has been disabled.");
    }
}