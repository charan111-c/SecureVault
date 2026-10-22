package com.securevault.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securevault.backend.entity.SecurityEvent;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.SecurityEventRepository;
import com.securevault.backend.security.RiskLevel;

@Service
public class ThreatMonitoringService {

    private final SecurityEventRepository securityEventRepository;
    private final RiskAnalysisService riskAnalysisService;

    public ThreatMonitoringService(
            SecurityEventRepository securityEventRepository,
            RiskAnalysisService riskAnalysisService
    ) {

        this.securityEventRepository =
                securityEventRepository;

        this.riskAnalysisService =
                riskAnalysisService;
    }

    // ==========================================
    // CONSOLIDATED SECURITY MONITORING
    // ==========================================

    @Transactional(readOnly = true)
    public ThreatMonitoringResult getThreatOverview(
            User user,
            LocalDateTime start,
            LocalDateTime end
    ) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

        if (start == null || end == null) {
            throw new IllegalArgumentException(
                    "Start and end time are required"
            );
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException(
                    "Start time cannot be after end time"
            );
        }

        // ==========================================
        // GET SECURITY EVENTS
        // ==========================================

        List<SecurityEvent> events =
                securityEventRepository
                        .findByUserIdAndEventTimeBetweenOrderByEventTimeDesc(
                                user.getId(),
                                start,
                                end
                        );

        // ==========================================
        // ANALYZE RISK
        // ==========================================

        List<RiskAnalysisService.EventRiskResult> riskEvents =
                riskAnalysisService.analyzeRecentEvents(
                        user.getId(),
                        start,
                        end
                );

        // ==========================================
        // SUCCESSFUL LOGINS
        // ==========================================

        long successfulLogins =
                events.stream()
                        .filter(event ->
                                event.getEventType()
                                        == SecurityEvent.EventType.LOGIN)
                        .filter(event ->
                                event.getLoginStatus()
                                        == SecurityEvent.LoginStatus.SUCCESS)
                        .count();

        // ==========================================
        // FAILED LOGINS
        // ==========================================

        long failedLogins =
                events.stream()
                        .filter(event ->
                                event.getEventType()
                                        == SecurityEvent.EventType.LOGIN)
                        .filter(event ->
                                event.getLoginStatus()
                                        == SecurityEvent.LoginStatus.FAILURE)
                        .count();

        // ==========================================
        // ANOMALOUS LOGINS
        // ==========================================

        long anomalies =
                events.stream()
                        .filter(event ->
                                event.getEventType()
                                        == SecurityEvent.EventType.ANOMALOUS_LOGIN)
                        .count();

        // ==========================================
        // HIGH RISK EVENTS
        // ==========================================

        long highRisk =
                riskEvents.stream()
                        .filter(event ->
                                event.getRiskLevel()
                                        == RiskLevel.HIGH
                                        ||
                                event.getRiskLevel()
                                        == RiskLevel.CRITICAL)
                        .count();

        // ==========================================
        // MEDIUM RISK EVENTS
        // ==========================================

        long mediumRisk =
                riskEvents.stream()
                        .filter(event ->
                                event.getRiskLevel()
                                        == RiskLevel.MEDIUM)
                        .count();

        // ==========================================
        // RETURN OVERVIEW
        // ==========================================

        return new ThreatMonitoringResult(
                events.size(),
                successfulLogins,
                failedLogins,
                anomalies,
                highRisk,
                mediumRisk,
                riskEvents
        );
    }

    // ==========================================
    // RESPONSE
    // ==========================================

    public static class ThreatMonitoringResult {

        private final long totalEvents;

        private final long successfulLogins;

        private final long failedLogins;

        private final long anomalies;

        private final long highRiskEvents;

        private final long mediumRiskEvents;

        private final List<
                RiskAnalysisService.EventRiskResult
        > events;

        public ThreatMonitoringResult(
                long totalEvents,
                long successfulLogins,
                long failedLogins,
                long anomalies,
                long highRiskEvents,
                long mediumRiskEvents,
                List<RiskAnalysisService.EventRiskResult> events
        ) {

            this.totalEvents =
                    totalEvents;

            this.successfulLogins =
                    successfulLogins;

            this.failedLogins =
                    failedLogins;

            this.anomalies =
                    anomalies;

            this.highRiskEvents =
                    highRiskEvents;

            this.mediumRiskEvents =
                    mediumRiskEvents;

            this.events =
                    events;
        }

        public long getTotalEvents() {
            return totalEvents;
        }

        public long getSuccessfulLogins() {
            return successfulLogins;
        }

        public long getFailedLogins() {
            return failedLogins;
        }

        public long getAnomalies() {
            return anomalies;
        }

        public long getHighRiskEvents() {
            return highRiskEvents;
        }

        public long getMediumRiskEvents() {
            return mediumRiskEvents;
        }

        public List<RiskAnalysisService.EventRiskResult> getEvents() {
            return events;
        }
    }
}