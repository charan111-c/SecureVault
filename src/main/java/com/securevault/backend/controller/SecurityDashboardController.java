package com.securevault.backend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.service.RiskAnalysisService;
import com.securevault.backend.service.RiskAnalysisService.EventRiskResult;
import com.securevault.backend.service.ThreatMonitoringService;
import com.securevault.backend.service.ThreatMonitoringService.ThreatMonitoringResult;

@RestController
@RequestMapping("/api/security")
public class SecurityDashboardController {

    private final ThreatMonitoringService threatMonitoringService;
    private final RiskAnalysisService riskAnalysisService;
    private final UserRepository userRepository;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public SecurityDashboardController(
            ThreatMonitoringService threatMonitoringService,
            RiskAnalysisService riskAnalysisService,
            UserRepository userRepository) {

        this.threatMonitoringService = threatMonitoringService;
        this.riskAnalysisService = riskAnalysisService;
        this.userRepository = userRepository;
    }

    // =====================================================
    // SECURITY DASHBOARD
    // =====================================================

    @GetMapping("/dashboard")
    public ResponseEntity<ThreatMonitoringResult> getDashboard(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(7);

        ThreatMonitoringResult result =
                threatMonitoringService.getThreatOverview(
                        user,
                        start,
                        end
                );

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // SECURITY DASHBOARD BY DATE RANGE
    // =====================================================

    @GetMapping("/dashboard/range")
    public ResponseEntity<ThreatMonitoringResult> getDashboardByRange(
            Authentication authentication,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        User user = getAuthenticatedUser(authentication);

        LocalDateTime start =
                LocalDate.parse(startDate)
                        .atStartOfDay();

        LocalDateTime end =
                LocalDate.parse(endDate)
                        .atTime(LocalTime.MAX);

        ThreatMonitoringResult result =
                threatMonitoringService.getThreatOverview(
                        user,
                        start,
                        end
                );

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // RECENT RISK EVENTS
    // =====================================================

    @GetMapping("/dashboard/risk-events")
    public ResponseEntity<List<EventRiskResult>> getRiskEvents(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(7);

        // IMPORTANT:
        // Use the Spring-injected RiskAnalysisService.
        // DO NOT use new RiskAnalysisService(null).

        List<EventRiskResult> results =
                riskAnalysisService.analyzeRecentEvents(
                        user.getId(),
                        start,
                        end
                );

        return ResponseEntity.ok(results);
    }

    // =====================================================
    // RISK EVENTS BY DATE RANGE
    // =====================================================

    @GetMapping("/dashboard/risk-events/range")
    public ResponseEntity<List<EventRiskResult>> getRiskEventsByRange(
            Authentication authentication,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        User user = getAuthenticatedUser(authentication);

        LocalDateTime start =
                LocalDate.parse(startDate)
                        .atStartOfDay();

        LocalDateTime end =
                LocalDate.parse(endDate)
                        .atTime(LocalTime.MAX);

        List<EventRiskResult> results =
                riskAnalysisService.analyzeRecentEvents(
                        user.getId(),
                        start,
                        end
                );

        return ResponseEntity.ok(results);
    }

    // =====================================================
    // GET AUTHENTICATED USER
    // =====================================================

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null) {

            throw new RuntimeException(
                    "Authentication required"
            );
        }

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        )
                );
    }
}