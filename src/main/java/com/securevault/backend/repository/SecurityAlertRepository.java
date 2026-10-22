package com.securevault.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securevault.backend.entity.SecurityAlert;

public interface SecurityAlertRepository
        extends JpaRepository<SecurityAlert, Long> {

    // ==========================================
    // ALL ALERTS FOR USER
    // ==========================================

    List<SecurityAlert> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    // ==========================================
    // UNREAD ALERTS
    // ==========================================

    List<SecurityAlert> findByUserIdAndReadFalseOrderByCreatedAtDesc(
            Long userId
    );

    // ==========================================
    // UNREAD COUNT
    // ==========================================

    long countByUserIdAndReadFalse(
            Long userId
    );

    // ==========================================
    // CHECK DUPLICATE EVENT ALERT
    // ==========================================

    boolean existsBySecurityEventIdAndAlertType(
            Long securityEventId,
            SecurityAlert.AlertType alertType
    );

    // ==========================================
    // FIND USER ALERT
    // ==========================================

    java.util.Optional<SecurityAlert> findByIdAndUserId(
            Long id,
            Long userId
    );
}