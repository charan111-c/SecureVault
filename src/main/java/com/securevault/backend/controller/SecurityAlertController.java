package com.securevault.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.service.SecurityAlertService;
import com.securevault.backend.service.SecurityAlertService.SecurityAlertResponse;

@RestController
@RequestMapping("/api/security-alerts")
public class SecurityAlertController {

    private final SecurityAlertService securityAlertService;

    private final UserRepository userRepository;

    public SecurityAlertController(
            SecurityAlertService securityAlertService,
            UserRepository userRepository
    ) {

        this.securityAlertService =
                securityAlertService;

        this.userRepository =
                userRepository;
    }

    // ==========================================
    // GET ALL ALERTS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<SecurityAlertResponse>> getAlerts(
            Authentication authentication
    ) {

        User user =
                getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                securityAlertService.getAlerts(user)
        );
    }

    // ==========================================
    // GET UNREAD ALERTS
    // ==========================================

    @GetMapping("/unread")
    public ResponseEntity<List<SecurityAlertResponse>> getUnreadAlerts(
            Authentication authentication
    ) {

        User user =
                getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                securityAlertService.getUnreadAlerts(user)
        );
    }

    // ==========================================
    // GET UNREAD COUNT
    // ==========================================

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(
            Authentication authentication
    ) {

        User user =
                getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                securityAlertService.getUnreadCount(user)
        );
    }

    // ==========================================
    // MARK ONE AS READ
    // ==========================================

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {

        User user =
                getAuthenticatedUser(authentication);

        boolean updated =
                securityAlertService.markAsRead(
                        id,
                        user
                );

        if (!updated) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                java.util.Map.of(
                        "success", true,
                        "message", "Security alert marked as read"
                )
        );
    }

    // ==========================================
    // MARK ALL AS READ
    // ==========================================

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(
            Authentication authentication
    ) {

        User user =
                getAuthenticatedUser(authentication);

        int count =
                securityAlertService.markAllAsRead(user);

        return ResponseEntity.ok(
                java.util.Map.of(
                        "success", true,
                        "updatedAlerts", count,
                        "message",
                        "All security alerts marked as read"
                )
        );
    }

    // ==========================================
    // AUTHENTICATED USER
    // ==========================================

    private User getAuthenticatedUser(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "Authentication required"
            );
        }

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