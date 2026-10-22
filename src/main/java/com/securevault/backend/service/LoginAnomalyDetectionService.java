package com.securevault.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.securevault.backend.entity.SecurityEvent;
import com.securevault.backend.entity.SecurityEvent.LoginStatus;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.SecurityEventRepository;

@Service
public class LoginAnomalyDetectionService {

    private final SecurityEventRepository securityEventRepository;

    public LoginAnomalyDetectionService(
            SecurityEventRepository securityEventRepository
    ) {
        this.securityEventRepository = securityEventRepository;
    }

    public boolean isNewDevice(
            User user,
            String userAgent
    ) {

        if (user == null || userAgent == null || userAgent.isBlank()) {
            return false;
        }

        List<SecurityEvent> events =
                securityEventRepository
                        .findByUserIdOrderByEventTimeDesc(user.getId());

        return events.stream()
                .filter(event ->
                        event.getLoginStatus() == LoginStatus.SUCCESS
                )
                .noneMatch(event ->
                        userAgent.equals(event.getUserAgent())
                );
    }

    public boolean isNewIpAddress(
            User user,
            String ipAddress
    ) {

        if (user == null || ipAddress == null || ipAddress.isBlank()) {
            return false;
        }

        List<SecurityEvent> events =
                securityEventRepository
                        .findByUserIdOrderByEventTimeDesc(user.getId());

        return events.stream()
                .filter(event ->
                        event.getLoginStatus() == LoginStatus.SUCCESS
                )
                .noneMatch(event ->
                        ipAddress.equals(event.getIpAddress())
                );
    }

    public boolean hasMultipleFailedLogins(
            User user
    ) {

        if (user == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime start =
                now.minusMinutes(15);

        long failedAttempts =
                securityEventRepository
                        .countByUserIdAndLoginStatusAndEventTimeBetween(
                                user.getId(),
                                LoginStatus.FAILURE,
                                start,
                                now
                        );

        return failedAttempts >= 3;
    }

    public boolean isAnomalousLogin(
            User user,
            String ipAddress,
            String userAgent
    ) {

        if (user == null) {
            return false;
        }

        boolean newDevice =
                isNewDevice(user, userAgent);

        boolean newIp =
                isNewIpAddress(user, ipAddress);

        boolean multipleFailures =
                hasMultipleFailedLogins(user);

        return newDevice || newIp || multipleFailures;
    }
}