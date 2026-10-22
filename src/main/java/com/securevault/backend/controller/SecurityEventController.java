package com.securevault.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securevault.backend.dto.SecurityEventResponse;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.service.SecurityEventService;

@RestController
@RequestMapping("/api/security-events")
public class SecurityEventController {

    private final SecurityEventService securityEventService;
    private final UserRepository userRepository;

    public SecurityEventController(
            SecurityEventService securityEventService,
            UserRepository userRepository
    ) {
        this.securityEventService = securityEventService;
        this.userRepository = userRepository;
    }

    /**
     * Get login history for the currently authenticated user.
     *
     * GET /api/security-events
     */
    @GetMapping
    public ResponseEntity<List<SecurityEventResponse>> getMyLoginHistory(
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found")
                );

        List<SecurityEventResponse> history =
                securityEventService.getLoginHistory(user.getId());

        return ResponseEntity.ok(history);
    }
}