package com.securevault.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.securevault.backend.dto.SecurityEventResponse;
import com.securevault.backend.entity.SecurityEvent;
import com.securevault.backend.entity.SecurityEvent.EventType;
import com.securevault.backend.entity.SecurityEvent.LoginStatus;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.SecurityEventRepository;

@Service
public class SecurityEventService {

    private final SecurityEventRepository securityEventRepository;

    private final NotificationService notificationService;

    public SecurityEventService(
            SecurityEventRepository securityEventRepository,
            NotificationService notificationService
    ) {

        this.securityEventRepository =
                securityEventRepository;

        this.notificationService =
                notificationService;
    }

    // ==========================================
    // RECORD NORMAL LOGIN EVENT
    // ==========================================

    public void recordLoginEvent(
            User user,
            LoginStatus loginStatus,
            String ipAddress,
            String userAgent
    ) {

        SecurityEvent event =
                new SecurityEvent();

        event.setUser(user);

        event.setEventType(
                EventType.LOGIN
        );

        event.setLoginStatus(
                loginStatus
        );

        event.setIpAddress(
                ipAddress
        );

        event.setUserAgent(
                userAgent
        );

        securityEventRepository.save(event);

        /*
         * Only successful logins generate
         * LOGIN_ALERT notifications.
         *
         * Failed logins remain security events,
         * but we don't send an email for every
         * failed attempt.
         */
        if (user != null &&
                loginStatus == LoginStatus.SUCCESS) {

            notificationService.notifyLogin(
                    user,
                    ipAddress,
                    userAgent
            );
        }
    }

    // ==========================================
    // RECORD ANOMALOUS LOGIN EVENT
    // ==========================================

    public void recordAnomalousLoginEvent(
            User user,
            String ipAddress,
            String userAgent
    ) {

        SecurityEvent event =
                new SecurityEvent();

        event.setUser(user);

        event.setEventType(
                EventType.ANOMALOUS_LOGIN
        );

        /*
         * Authentication succeeded.
         * The anomaly represents suspicious behavior.
         */
        event.setLoginStatus(
                LoginStatus.SUCCESS
        );

        event.setIpAddress(
                ipAddress
        );

        event.setUserAgent(
                userAgent
        );

        securityEventRepository.save(event);

        /*
         * Send a separate RISK_ALERT.
         */
        if (user != null) {

            notificationService.notifyRisk(
                    user,
                    ipAddress,
                    userAgent
            );
        }
    }

    // ==========================================
    // GET USER LOGIN HISTORY
    // ==========================================

    public List<SecurityEventResponse> getLoginHistory(
            Long userId
    ) {

        List<SecurityEvent> events =
                securityEventRepository
                        .findByUserIdOrderByEventTimeDesc(
                                userId
                        );

        return events.stream()
                .map(this::toResponse)
                .toList();
    }

    // ==========================================
    // ENTITY → RESPONSE
    // ==========================================

    private SecurityEventResponse toResponse(
            SecurityEvent event
    ) {

        return new SecurityEventResponse(
                event.getId(),
                event.getEventType().name(),
                event.getLoginStatus(),
                event.getEventTime(),
                event.getIpAddress(),
                event.getUserAgent()
        );
    }
}