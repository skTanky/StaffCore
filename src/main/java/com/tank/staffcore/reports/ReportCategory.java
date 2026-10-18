package com.tank.staffcore.reports;

import java.util.Locale;

public enum ReportCategory {

    COMBAT_MOVEMENT("Combat/Movement"),
    OTHER("Other"),
    CHAT("Chat");

    private final String displayName;

    ReportCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getKey() {
        return name().toLowerCase(Locale.ROOT);
    }
}