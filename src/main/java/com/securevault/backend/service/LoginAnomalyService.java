package com.securevault.backend.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.securevault.backend.entity.User;
import com.securevault.backend.entity.SecurityEvent.LoginStatus;
import com.securevault.backend.repository.SecurityEventRepository;

@Service
public class LoginAnomalyService {

    /*
     * Five failed attempts within fifteen minutes
     * will be considered suspicious.
     */
    private static final int FAILURE_THRESHOLD = 5;

    private static final int FAILURE_WINDOW_MINUTES = 15;

    private final SecurityEventRepository securityEventRepository;

    private final DeviceTrackingService deviceTrackingService;

    public LoginAnomalyService(
            SecurityEventRepository securityEventRepository,
            DeviceTrackingService deviceTrackingService
    ) {

        this.securityEventRepository =
                securityEventRepository;

        this.deviceTrackingService =
                deviceTrackingService;
    }

    // ==========================================
    // CHECK NEW DEVICE
    // ==========================================

    public boolean isNewDevice(
            User user,
            String ipAddress,
            String userAgent
    ) {

        if (user == null) {
            return false;
        }

        return !deviceTrackingService.isKnownDevice(
                user.getId(),
                ipAddress,
                userAgent
        );
    }

    // ==========================================
    // CHECK REPEATED LOGIN FAILURES
    // ==========================================

    public boolean hasRepeatedFailures(
            Long userId
    ) {

        if (userId == null) {
            return false;
        }

        LocalDateTime end =
                LocalDateTime.now();

        LocalDateTime start =
                end.minusMinutes(
                        FAILURE_WINDOW_MINUTES
                );

        long failureCount =
                securityEventRepository
                        .countByUserIdAndLoginStatusAndEventTimeBetween(
                                userId,
                                LoginStatus.FAILURE,
                                start,
                                end
                        );

        return failureCount >= FAILURE_THRESHOLD;
    }

    // ==========================================
    // COMPLETE LOGIN ANALYSIS
    // ==========================================

    public LoginAnomalyResult analyzeLogin(
            User user,
            String ipAddress,
            String userAgent
    ) {

        if (user == null) {

            return new LoginAnomalyResult(
                    false,
                    false,
                    false,
                    "UNKNOWN_USER"
            );
        }

        boolean newDevice =
                isNewDevice(
                        user,
                        ipAddress,
                        userAgent
                );

        boolean repeatedFailures =
                hasRepeatedFailures(
                        user.getId()
                );

        boolean anomalous =
                newDevice ||
                repeatedFailures;

        String reason = "NORMAL";

        if (newDevice && repeatedFailures) {

            reason =
                    "NEW_DEVICE_AND_REPEATED_FAILURES";

        } else if (newDevice) {

            reason =
                    "NEW_DEVICE";

        } else if (repeatedFailures) {

            reason =
                    "REPEATED_LOGIN_FAILURES";
        }

        return new LoginAnomalyResult(
                anomalous,
                newDevice,
                repeatedFailures,
                reason
        );
    }

    // ==========================================
    // RESULT CLASS
    // ==========================================

    public static class LoginAnomalyResult {

        private final boolean anomalous;

        private final boolean newDevice;

        private final boolean repeatedFailures;

        private final String reason;

        public LoginAnomalyResult(
                boolean anomalous,
                boolean newDevice,
                boolean repeatedFailures,
                String reason
        ) {

            this.anomalous = anomalous;

            this.newDevice = newDevice;

            this.repeatedFailures =
                    repeatedFailures;

            this.reason = reason;
        }

        public boolean isAnomalous() {
            return anomalous;
        }

        public boolean isNewDevice() {
            return newDevice;
        }

        public boolean isRepeatedFailures() {
            return repeatedFailures;
        }

        public String getReason() {
            return reason;
        }
    }
}