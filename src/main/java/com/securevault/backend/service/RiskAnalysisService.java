package com.securevault.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.securevault.backend.entity.SecurityEvent;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.SecurityEventRepository;
import com.securevault.backend.security.RiskLevel;

@Service
public class RiskAnalysisService {

    private final SecurityEventRepository securityEventRepository;

    public RiskAnalysisService(
            SecurityEventRepository securityEventRepository
    ) {
        this.securityEventRepository =
                securityEventRepository;
    }

    // =====================================================
    // LOGIN RISK ANALYSIS
    // =====================================================

    public RiskLevel analyzeLoginRisk(
            User user,
            String ipAddress,
            String userAgent
    ) {

        if (user == null) {
            return RiskLevel.HIGH;
        }

        List<SecurityEvent> history =
                securityEventRepository
                        .findByUserIdOrderByEventTimeDesc(
                                user.getId()
                        );

        /*
         * First login:
         * No previous security history exists.
         */
        if (history.isEmpty()) {
            return RiskLevel.LOW;
        }

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime last24Hours =
                now.minusHours(24);

        long totalEvents =
                securityEventRepository
                        .countByUserIdAndEventTimeBetween(
                                user.getId(),
                                last24Hours,
                                now
                        );

        long failedEvents =
                securityEventRepository
                        .countByUserIdAndLoginStatusAndEventTimeBetween(
                                user.getId(),
                                SecurityEvent.LoginStatus.FAILURE,
                                last24Hours,
                                now
                        );

        int riskScore = 0;

        // =================================================
        // RECENT ACTIVITY
        // =================================================

        if (totalEvents >= 10) {

            riskScore += 30;

        } else if (totalEvents >= 5) {

            riskScore += 15;
        }

        // =================================================
        // FAILED LOGIN ATTEMPTS
        // =================================================

        if (failedEvents >= 5) {

            riskScore += 40;

        } else if (failedEvents >= 3) {

            riskScore += 25;

        } else if (failedEvents >= 1) {

            riskScore += 10;
        }

        // =================================================
        // IP ADDRESS CHANGE
        // =================================================

        SecurityEvent latestEvent =
                history.get(0);

        if (latestEvent.getIpAddress() != null
                && ipAddress != null
                && !latestEvent
                        .getIpAddress()
                        .equals(ipAddress)) {

            riskScore += 20;
        }

        // =================================================
        // USER AGENT CHANGE
        // =================================================

        if (latestEvent.getUserAgent() != null
                && userAgent != null
                && !latestEvent
                        .getUserAgent()
                        .equals(userAgent)) {

            riskScore += 10;
        }

        // =================================================
        // CONVERT SCORE TO RISK LEVEL
        // =================================================

        return convertScoreToRiskLevel(riskScore);
    }

    // =====================================================
    // CHECK WHETHER RISK IS ANOMALOUS
    // =====================================================

    public boolean isAnomalous(
            RiskLevel riskLevel
    ) {

        if (riskLevel == null) {
            return false;
        }

        return riskLevel == RiskLevel.HIGH
                || riskLevel == RiskLevel.CRITICAL;
    }

    // =====================================================
    // ANALYZE EVENTS
    // =====================================================

    public List<EventRiskResult> analyzeRecentEvents(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    ) {

        List<SecurityEvent> events =
                securityEventRepository
                        .findByUserIdAndEventTimeBetweenOrderByEventTimeDesc(
                                userId,
                                start,
                                end
                        );

        List<EventRiskResult> results =
                new ArrayList<>();

        for (SecurityEvent event : events) {

            RiskLevel riskLevel =
                    calculateEventRisk(event);

            results.add(
                    new EventRiskResult(
                            event,
                            riskLevel
                    )
            );
        }

        return results;
    }

    // =====================================================
    // CALCULATE EVENT RISK
    // =====================================================

    private RiskLevel calculateEventRisk(
            SecurityEvent event
    ) {

        int score = 0;

        // Failed login
        if (event.getLoginStatus()
                == SecurityEvent.LoginStatus.FAILURE) {

            score += 40;
        }

        // Anomalous login
        if (event.getEventType()
                == SecurityEvent.EventType.ANOMALOUS_LOGIN) {

            score += 40;
        }

        return convertScoreToRiskLevel(score);
    }

    // =====================================================
    // SCORE → RISK LEVEL
    // =====================================================

    private RiskLevel convertScoreToRiskLevel(
            int score
    ) {

        if (score >= 70) {

            return RiskLevel.CRITICAL;

        } else if (score >= 50) {

            return RiskLevel.HIGH;

        } else if (score >= 25) {

            return RiskLevel.MEDIUM;

        } else {

            return RiskLevel.LOW;
        }
    }

    // =====================================================
    // EVENT RISK RESULT
    // =====================================================

    public static class EventRiskResult {

        private Long eventId;

        private String eventType;

        private SecurityEvent.LoginStatus loginStatus;

        private LocalDateTime eventTime;

        private String ipAddress;

        private String userAgent;

        private RiskLevel riskLevel;

        public EventRiskResult() {
        }

        public EventRiskResult(
                SecurityEvent event,
                RiskLevel riskLevel
        ) {

            this.eventId =
                    event.getId();

            this.eventType =
                    event.getEventType()
                            .name();

            this.loginStatus =
                    event.getLoginStatus();

            this.eventTime =
                    event.getEventTime();

            this.ipAddress =
                    event.getIpAddress();

            this.userAgent =
                    event.getUserAgent();

            this.riskLevel =
                    riskLevel;
        }

        public Long getEventId() {
            return eventId;
        }

        public String getEventType() {
            return eventType;
        }

        public SecurityEvent.LoginStatus getLoginStatus() {
            return loginStatus;
        }

        public LocalDateTime getEventTime() {
            return eventTime;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public RiskLevel getRiskLevel() {
            return riskLevel;
        }
    }
}