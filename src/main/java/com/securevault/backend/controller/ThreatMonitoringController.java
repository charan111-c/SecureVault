package com.securevault.backend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.service.ThreatMonitoringService;
import com.securevault.backend.service.ThreatMonitoringService.ThreatMonitoringResult;

@RestController
@RequestMapping("/api/threat")
public class ThreatMonitoringController {

    private final ThreatMonitoringService threatMonitoringService;
    private final UserRepository userRepository;

    public ThreatMonitoringController(
            ThreatMonitoringService threatMonitoringService,
            UserRepository userRepository) {

        this.threatMonitoringService = threatMonitoringService;
        this.userRepository = userRepository;
    }

    // ==========================================
    // THREAT OVERVIEW - LAST 7 DAYS
    // ==========================================

    @GetMapping("/overview")
    public ResponseEntity<ThreatMonitoringResult> getThreatOverview(
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

    // ==========================================
    // THREAT OVERVIEW BY DATE RANGE
    // ==========================================

    @GetMapping("/overview/range")
    public ResponseEntity<ThreatMonitoringResult> getThreatOverviewByRange(
            Authentication authentication,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        User user = getAuthenticatedUser(authentication);

        LocalDate start = LocalDate.parse(startDate);

        LocalDate end = LocalDate.parse(endDate);

        LocalDateTime startDateTime =
                start.atStartOfDay();

        LocalDateTime endDateTime =
                end.atTime(LocalTime.MAX);

        ThreatMonitoringResult result =
                threatMonitoringService.getThreatOverview(
                        user,
                        startDateTime,
                        endDateTime
                );

        return ResponseEntity.ok(result);
    }

    // ==========================================
    // GET AUTHENTICATED USER
    // ==========================================

    private User getAuthenticatedUser(
            Authentication authentication) {

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