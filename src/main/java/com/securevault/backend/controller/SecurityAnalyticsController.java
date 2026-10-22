package com.securevault.backend.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.securevault.backend.dto.SecurityAnalyticsResponse;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.service.SecurityAnalyticsService;

@RestController
@RequestMapping("/api/analytics")
public class SecurityAnalyticsController {

    private final SecurityAnalyticsService securityAnalyticsService;
    private final UserRepository userRepository;

    public SecurityAnalyticsController(
            SecurityAnalyticsService securityAnalyticsService,
            UserRepository userRepository
    ) {
        this.securityAnalyticsService = securityAnalyticsService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<SecurityAnalyticsResponse> getAnalytics(
            Authentication authentication,

            @RequestParam(
                    required = false,
                    defaultValue = "last7days"
            )
            String period,

            @RequestParam(required = false)
            LocalDate startDate,

            @RequestParam(required = false)
            LocalDate endDate
    ) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        )
                );

        SecurityAnalyticsResponse response =
                securityAnalyticsService.getAnalytics(
                        user.getId(),
                        period,
                        startDate,
                        endDate
                );

        return ResponseEntity.ok(response);
    }
}