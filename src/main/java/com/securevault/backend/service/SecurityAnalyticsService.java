package com.securevault.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.securevault.backend.dto.SecurityAnalyticsResponse;
import com.securevault.backend.dto.SecurityEventResponse;
import com.securevault.backend.entity.SecurityEvent.LoginStatus;
import com.securevault.backend.repository.SecurityEventRepository;

@Service
public class SecurityAnalyticsService {

    private final SecurityEventRepository securityEventRepository;

    public SecurityAnalyticsService(
            SecurityEventRepository securityEventRepository
    ) {
        this.securityEventRepository = securityEventRepository;
    }

    // =========================================================
    // GET SECURITY ANALYTICS
    // =========================================================

    public SecurityAnalyticsResponse getAnalytics(
            Long userId,
            String period,
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID cannot be null"
            );
        }

        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        String selectedPeriod;

        // =====================================================
        // TODAY
        // =====================================================

        if ("today".equalsIgnoreCase(period)) {

            LocalDate today = LocalDate.now();

            startDateTime =
                    today.atStartOfDay();

            endDateTime =
                    today.atTime(LocalTime.MAX);

            selectedPeriod = "today";
        }

        // =====================================================
        // LAST 7 DAYS
        // =====================================================

        else if ("last7days".equalsIgnoreCase(period)) {

            LocalDate today = LocalDate.now();

            startDateTime =
                    today.minusDays(6)
                            .atStartOfDay();

            endDateTime =
                    today.atTime(LocalTime.MAX);

            selectedPeriod = "last7days";
        }

        // =====================================================
        // CUSTOM DATE RANGE
        // =====================================================

        else if (startDate != null && endDate != null) {

            if (endDate.isBefore(startDate)) {

                throw new IllegalArgumentException(
                        "End date cannot be before start date"
                );
            }

            startDateTime =
                    startDate.atStartOfDay();

            endDateTime =
                    endDate.atTime(LocalTime.MAX);

            selectedPeriod = "custom";
        }

        // =====================================================
        // DEFAULT → LAST 7 DAYS
        // =====================================================

        else {

            LocalDate today = LocalDate.now();

            startDateTime =
                    today.minusDays(6)
                            .atStartOfDay();

            endDateTime =
                    today.atTime(LocalTime.MAX);

            selectedPeriod = "last7days";
        }

        // =====================================================
        // GET SECURITY EVENTS
        // =====================================================

        List<SecurityEventResponse> recentActivity =
                securityEventRepository
                        .findByUserIdAndEventTimeBetweenOrderByEventTimeDesc(
                                userId,
                                startDateTime,
                                endDateTime
                        )
                        .stream()
                        .map(event ->
                                new SecurityEventResponse(
                                        event.getId(),
                                        event.getEventType().name(),
                                        event.getLoginStatus(),
                                        event.getEventTime(),
                                        event.getIpAddress(),
                                        event.getUserAgent()
                                )
                        )
                        .toList();

        // =====================================================
        // SUCCESSFUL LOGINS
        // =====================================================

        long successfulLogins =
                securityEventRepository
                        .countByUserIdAndLoginStatusAndEventTimeBetween(
                                userId,
                                LoginStatus.SUCCESS,
                                startDateTime,
                                endDateTime
                        );

        // =====================================================
        // FAILED LOGINS
        // =====================================================

        long failedLogins =
                securityEventRepository
                        .countByUserIdAndLoginStatusAndEventTimeBetween(
                                userId,
                                LoginStatus.FAILURE,
                                startDateTime,
                                endDateTime
                        );

        // =====================================================
        // TOTAL LOGIN ATTEMPTS
        // =====================================================

        long totalLoginAttempts =
                securityEventRepository
                        .countByUserIdAndEventTimeBetween(
                                userId,
                                startDateTime,
                                endDateTime
                        );

        // =====================================================
        // SUSPICIOUS ACTIVITIES
        //
        // ANOMALOUS_LOGIN events are treated as
        // suspicious activities.
        // =====================================================

        long suspiciousActivities =
                recentActivity
                        .stream()
                        .filter(event ->
                                "ANOMALOUS_LOGIN"
                                        .equals(event.getEventType())
                        )
                        .count();

        // =====================================================
        // SECURITY ALERTS
        //
        // SecurityAlert module is not being used.
        // Therefore we do not create fake alert data.
        // =====================================================

        long activeSecurityAlerts = 0;

        // =====================================================
        // RETURN ANALYTICS
        // =====================================================

        return new SecurityAnalyticsResponse(
                successfulLogins,
                failedLogins,
                totalLoginAttempts,
                suspiciousActivities,
                activeSecurityAlerts,
                selectedPeriod,
                startDateTime,
                endDateTime,
                recentActivity
        );
    }
}