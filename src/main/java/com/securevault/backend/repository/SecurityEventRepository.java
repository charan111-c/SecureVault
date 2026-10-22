package com.securevault.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securevault.backend.entity.SecurityEvent;
import com.securevault.backend.entity.SecurityEvent.LoginStatus;

public interface SecurityEventRepository
        extends JpaRepository<SecurityEvent, Long> {

    // Existing login history
    List<SecurityEvent> findByUserIdOrderByEventTimeDesc(Long userId);

    // Events between two dates
    List<SecurityEvent> findByUserIdAndEventTimeBetweenOrderByEventTimeDesc(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    // Count all login events in a date range
    long countByUserIdAndEventTimeBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    // Count SUCCESS / FAILURE events in a date range
    long countByUserIdAndLoginStatusAndEventTimeBetween(
            Long userId,
            LoginStatus loginStatus,
            LocalDateTime start,
            LocalDateTime end
    );
}