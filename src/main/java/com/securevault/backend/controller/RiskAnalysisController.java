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

@RestController
@RequestMapping("/api/risk")
public class RiskAnalysisController {

    private final RiskAnalysisService riskAnalysisService;
    private final UserRepository userRepository;

    public RiskAnalysisController(
            RiskAnalysisService riskAnalysisService,
            UserRepository userRepository) {

        this.riskAnalysisService =
                riskAnalysisService;

        this.userRepository =
                userRepository;
    }

    // ==========================================
    // RECENT RISK EVENTS
    // ==========================================

    @GetMapping("/events")
    public ResponseEntity<List<EventRiskResult>> getRiskEvents(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        LocalDateTime end =
                LocalDateTime.now();

        LocalDateTime start =
                end.minusDays(7);

        List<EventRiskResult> results =
                riskAnalysisService.analyzeRecentEvents(
                        user.getId(),
                        start,
                        end
                );

        return ResponseEntity.ok(results);
    }

    // ==========================================
    // RISK EVENTS BY DATE RANGE
    // ==========================================

    @GetMapping("/events/range")
    public ResponseEntity<List<EventRiskResult>> getRiskEventsByDate(
            Authentication authentication,

            @RequestParam String startDate,

            @RequestParam String endDate) {

        User user = getAuthenticatedUser(authentication);

        LocalDate start =
                LocalDate.parse(startDate);

        LocalDate end =
                LocalDate.parse(endDate);

        LocalDateTime startDateTime =
                start.atStartOfDay();

        LocalDateTime endDateTime =
                end.atTime(LocalTime.MAX);

        List<EventRiskResult> results =
                riskAnalysisService.analyzeRecentEvents(
                        user.getId(),
                        startDateTime,
                        endDateTime
                );

        return ResponseEntity.ok(results);
    }

    // ==========================================
    // GET AUTHENTICATED USER
    // ==========================================

    private User getAuthenticatedUser(
            Authentication authentication) {

        String email =
                authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        )
                );
    }
}