package com.tank.staffcore.discord;

public enum LogType {
    
    REPORT_CREATED("reports"),
    REPORT_DELETED("delete-report"),
    REPORTS_CLEARED("delete-all-reports"),
    STAFFMODE("staffmode"),
    VANISH("vanish"),
    FREEZE("freeze"),
    SLOWCHAT("slowchat"),
    MUTECHAT("mutechat");

    private final String configKey;

    LogType(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }
}
