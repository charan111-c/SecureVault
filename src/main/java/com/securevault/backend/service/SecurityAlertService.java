package com.securevault.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securevault.backend.entity.SecurityAlert;
import com.securevault.backend.entity.SecurityAlert.AlertType;
import com.securevault.backend.entity.SecurityAlert.RiskLevel;
import com.securevault.backend.entity.SecurityEvent;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.SecurityAlertRepository;

@Service
public class SecurityAlertService {

    private final SecurityAlertRepository securityAlertRepository;

    public SecurityAlertService(
            SecurityAlertRepository securityAlertRepository
    ) {
        this.securityAlertRepository =
                securityAlertRepository;
    }

    // ==========================================
    // CREATE ANOMALOUS LOGIN ALERT
    // ==========================================

    @Transactional
    public SecurityAlert createAnomalousLoginAlert(
            User user,
            SecurityEvent event
    ) {

        if (user == null || event == null) {
            throw new IllegalArgumentException(
                    "User and security event are required"
            );
        }

        boolean alreadyExists =
                securityAlertRepository
                        .existsBySecurityEventIdAndAlertType(
                                event.getId(),
                                AlertType.ANOMALOUS_LOGIN
                        );

        if (alreadyExists) {
            return null;
        }

        SecurityAlert alert =
                new SecurityAlert();

        alert.setUser(user);

        alert.setSecurityEvent(event);

        alert.setAlertType(
                AlertType.ANOMALOUS_LOGIN
        );

        alert.setRiskLevel(
                RiskLevel.HIGH
        );

        alert.setMessage(
                "An anomalous login was detected for your account."
        );

        alert.setIpAddress(
                event.getIpAddress()
        );

        alert.setUserAgent(
                event.getUserAgent()
        );

        alert.setRead(false);

        return securityAlertRepository.save(alert);
    }

    // ==========================================
    // CREATE NEW DEVICE ALERT
    // ==========================================

    @Transactional
    public SecurityAlert createNewDeviceAlert(
            User user,
            SecurityEvent event
    ) {

        if (user == null || event == null) {
            throw new IllegalArgumentException(
                    "User and security event are required"
            );
        }

        boolean alreadyExists =
                securityAlertRepository
                        .existsBySecurityEventIdAndAlertType(
                                event.getId(),
                                AlertType.NEW_DEVICE
                        );

        if (alreadyExists) {
            return null;
        }

        SecurityAlert alert =
                new SecurityAlert();

        alert.setUser(user);

        alert.setSecurityEvent(event);

        alert.setAlertType(
                AlertType.NEW_DEVICE
        );

        alert.setRiskLevel(
                RiskLevel.MEDIUM
        );

        alert.setMessage(
                "A successful login from a new device was detected."
        );

        alert.setIpAddress(
                event.getIpAddress()
        );

        alert.setUserAgent(
                event.getUserAgent()
        );

        alert.setRead(false);

        return securityAlertRepository.save(alert);
    }

    // ==========================================
    // CREATE REPEATED FAILURE ALERT
    // ==========================================

    @Transactional
    public SecurityAlert createRepeatedFailureAlert(
            User user,
            SecurityEvent event
    ) {

        if (user == null || event == null) {
            throw new IllegalArgumentException(
                    "User and security event are required"
            );
        }

        boolean alreadyExists =
                securityAlertRepository
                        .existsBySecurityEventIdAndAlertType(
                                event.getId(),
                                AlertType.REPEATED_LOGIN_FAILURES
                        );

        if (alreadyExists) {
            return null;
        }

        SecurityAlert alert =
                new SecurityAlert();

        alert.setUser(user);

        alert.setSecurityEvent(event);

        alert.setAlertType(
                AlertType.REPEATED_LOGIN_FAILURES
        );

        alert.setRiskLevel(
                RiskLevel.HIGH
        );

        alert.setMessage(
                "Multiple failed login attempts were detected."
        );

        alert.setIpAddress(
                event.getIpAddress()
        );

        alert.setUserAgent(
                event.getUserAgent()
        );

        alert.setRead(false);

        return securityAlertRepository.save(alert);
    }

    // ==========================================
    // GET ALL ALERTS
    // ==========================================

    @Transactional(readOnly = true)
    public List<SecurityAlertResponse> getAlerts(
            User user
    ) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

        return securityAlertRepository
                .findByUserIdOrderByCreatedAtDesc(
                        user.getId()
                )
                .stream()
                .map(SecurityAlertResponse::new)
                .toList();
    }

    // ==========================================
    // GET UNREAD ALERTS
    // ==========================================

    @Transactional(readOnly = true)
    public List<SecurityAlertResponse> getUnreadAlerts(
            User user
    ) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

        return securityAlertRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        user.getId()
                )
                .stream()
                .map(SecurityAlertResponse::new)
                .toList();
    }

    // ==========================================
    // GET UNREAD COUNT
    // ==========================================

    @Transactional(readOnly = true)
    public long getUnreadCount(
            User user
    ) {

        if (user == null) {
            return 0;
        }

        return securityAlertRepository
                .countByUserIdAndReadFalse(
                        user.getId()
                );
    }

    // ==========================================
    // MARK ONE ALERT AS READ
    // ==========================================

    @Transactional
    public boolean markAsRead(
            Long alertId,
            User user
    ) {

        if (alertId == null || user == null) {
            return false;
        }

        return securityAlertRepository
                .findByIdAndUserId(
                        alertId,
                        user.getId()
                )
                .map(alert -> {

                    alert.setRead(true);

                    securityAlertRepository.save(alert);

                    return true;

                })
                .orElse(false);
    }

    // ==========================================
    // MARK ALL ALERTS AS READ
    // ==========================================

    @Transactional
    public int markAllAsRead(
            User user
    ) {

        if (user == null) {
            return 0;
        }

        List<SecurityAlert> alerts =
                securityAlertRepository
                        .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                                user.getId()
                        );

        for (SecurityAlert alert : alerts) {
            alert.setRead(true);
        }

        securityAlertRepository.saveAll(alerts);

        return alerts.size();
    }

    // ==========================================
    // RESPONSE DTO
    // ==========================================

    public static class SecurityAlertResponse {

        private final Long id;

        private final String alertType;

        private final String riskLevel;

        private final String message;

        private final String ipAddress;

        private final String userAgent;

        private final boolean read;

        private final LocalDateTime createdAt;

        private final Long securityEventId;

        public SecurityAlertResponse(
                SecurityAlert alert
        ) {

            this.id =
                    alert.getId();

            this.alertType =
                    alert.getAlertType().name();

            this.riskLevel =
                    alert.getRiskLevel().name();

            this.message =
                    alert.getMessage();

            this.ipAddress =
                    alert.getIpAddress();

            this.userAgent =
                    alert.getUserAgent();

            this.read =
                    alert.isRead();

            this.createdAt =
                    alert.getCreatedAt();

            this.securityEventId =
                    alert.getSecurityEvent() != null
                            ? alert.getSecurityEvent().getId()
                            : null;
        }

        public Long getId() {
            return id;
        }

        public String getAlertType() {
            return alertType;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public String getMessage() {
            return message;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public boolean isRead() {
            return read;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public Long getSecurityEventId() {
            return securityEventId;
        }
    }
}