package com.tank.staffcore.reports;

public record Report(
    String id,
    String reportedName,
    String reporterName,
    String reason,
    int staffOnline,
    long submittedAt) {
}
