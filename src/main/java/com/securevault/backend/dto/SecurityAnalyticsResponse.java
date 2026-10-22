package com.securevault.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SecurityAnalyticsResponse {

    private long successfulLogins;
    private long failedLogins;
    private long totalLoginAttempts;

    private long suspiciousActivities;
    private long activeSecurityAlerts;

    private String period;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private List<SecurityEventResponse> recentActivity;

    public SecurityAnalyticsResponse(
            long successfulLogins,
            long failedLogins,
            long totalLoginAttempts,
            long suspiciousActivities,
            long activeSecurityAlerts,
            String period,
            LocalDateTime startDate,
            LocalDateTime endDate,
            List<SecurityEventResponse> recentActivity
    ) {
        this.successfulLogins = successfulLogins;
        this.failedLogins = failedLogins;
        this.totalLoginAttempts = totalLoginAttempts;
        this.suspiciousActivities = suspiciousActivities;
        this.activeSecurityAlerts = activeSecurityAlerts;
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recentActivity = recentActivity;
    }

    public long getSuccessfulLogins() {
        return successfulLogins;
    }

    public long getFailedLogins() {
        return failedLogins;
    }

    public long getTotalLoginAttempts() {
        return totalLoginAttempts;
    }

    public long getSuspiciousActivities() {
        return suspiciousActivities;
    }

    public long getActiveSecurityAlerts() {
        return activeSecurityAlerts;
    }

    public String getPeriod() {
        return period;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public List<SecurityEventResponse> getRecentActivity() {
        return recentActivity;
    }
}