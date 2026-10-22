package com.securevault.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securevault.backend.dto.ReportResponse;
import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.Notification;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.CredentialRepository;
import com.securevault.backend.repository.NotificationRepository;
import com.securevault.backend.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final CredentialRepository credentialRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public ReportService(
            CredentialRepository credentialRepository,
            NotificationRepository notificationRepository,
            UserRepository userRepository) {

        this.credentialRepository = credentialRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // GET USER
    // =========================================================

    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // =========================================================
    // SECURITY REPORT
    // =========================================================

    public ReportResponse generateSecurityReport(
            String email) {

        User user = getUser(email);

        List<Credential> credentials =
                credentialRepository.findByUser(user);

        List<Notification> notifications =
                notificationRepository
                        .findByUserOrderByCreatedAtDesc(user);

        ReportResponse response =
                new ReportResponse();

        response.setReportType("SECURITY REPORT");

        response.setGeneratedAt(
                LocalDateTime.now());

        // -----------------------------------------------------
        // CREDENTIAL INFORMATION
        // -----------------------------------------------------

        response.setTotalCredentials(
                credentials.size());

        int favoriteCount = 0;

        for (Credential credential : credentials) {

            if (credential.isFavorite()) {
                favoriteCount++;
            }
        }

        response.setFavoriteCredentials(
                favoriteCount);

        // -----------------------------------------------------
        // NOTIFICATION INFORMATION
        // -----------------------------------------------------

        response.setTotalNotifications(
                notifications.size());

        int unreadCount = 0;
        int emailCount = 0;
        int pushCount = 0;

        for (Notification notification : notifications) {

            if (!notification.isRead()) {
                unreadCount++;
            }

            if (notification.isEmailSent()) {
                emailCount++;
            }

            if (notification.isPushSent()) {
                pushCount++;
            }
        }

        response.setUnreadNotifications(
                unreadCount);

        response.setEmailNotifications(
                emailCount);

        response.setPushNotifications(
                pushCount);

        // -----------------------------------------------------
        // CREDENTIAL DETAILS
        // -----------------------------------------------------

        List<Map<String, Object>> details =
                new ArrayList<>();

        for (Credential credential : credentials) {

            Map<String, Object> data =
                    new HashMap<>();

            data.put(
                    "id",
                    credential.getId());

            data.put(
                    "title",
                    credential.getTitle());

            data.put(
                    "username",
                    credential.getUsername());

            data.put(
                    "category",
                    credential.getCategory());

            data.put(
                    "credentialType",
                    credential.getCredentialType());

            data.put(
                    "favorite",
                    credential.isFavorite());

            data.put(
                    "createdAt",
                    credential.getCreatedAt());

            data.put(
                    "updatedAt",
                    credential.getUpdatedAt());

            /*
             * IMPORTANT:
             *
             * password is NOT included.
             *
             * secureNote is NOT included.
             */

            details.add(data);
        }

        response.setDetails(details);

        return response;
    }

    // =========================================================
    // PASSWORD HEALTH REPORT
    // =========================================================

    public ReportResponse generatePasswordHealthReport(
            String email) {

        User user = getUser(email);

        List<Credential> credentials =
                credentialRepository.findByUser(user);

        ReportResponse response =
                new ReportResponse();

        response.setReportType(
                "PASSWORD HEALTH REPORT");

        response.setGeneratedAt(
                LocalDateTime.now());

        response.setTotalCredentials(
                credentials.size());

        int strongCount = 0;
        int mediumCount = 0;
        int weakCount = 0;

        // -----------------------------------------------------
        // CALCULATE PASSWORD HEALTH
        // -----------------------------------------------------

        for (Credential credential : credentials) {

            String password =
                    credential.getPassword();

            int strength =
                    calculatePasswordStrength(password);

            if (strength == 3) {

                strongCount++;

            } else if (strength == 2) {

                mediumCount++;

            } else {

                weakCount++;
            }
        }

        response.setStrongPasswords(
                strongCount);

        response.setMediumPasswords(
                mediumCount);

        response.setWeakPasswords(
                weakCount);

        // -----------------------------------------------------
        // DETAILS
        // -----------------------------------------------------

        List<Map<String, Object>> details =
                new ArrayList<>();

        for (Credential credential : credentials) {

            Map<String, Object> data =
                    new HashMap<>();

            data.put(
                    "id",
                    credential.getId());

            data.put(
                    "title",
                    credential.getTitle());

            data.put(
                    "category",
                    credential.getCategory());

            int strength =
                    calculatePasswordStrength(
                            credential.getPassword());

            String health;

            if (strength == 3) {

                health = "STRONG";

            } else if (strength == 2) {

                health = "MEDIUM";

            } else {

                health = "WEAK";
            }

            data.put(
                    "passwordHealth",
                    health);

            data.put(
                    "updatedAt",
                    credential.getUpdatedAt());

            /*
             * NEVER add:
             *
             * data.put("password", ...);
             *
             * Password itself must not appear
             * in reports.
             */

            details.add(data);
        }

        response.setDetails(details);

        return response;
    }

    // =========================================================
    // USER ACTIVITY REPORT
    // =========================================================

    public ReportResponse generateUserActivityReport(
            String email) {

        User user = getUser(email);

        List<Credential> credentials =
                credentialRepository.findByUser(user);

        List<Notification> notifications =
                notificationRepository
                        .findByUserOrderByCreatedAtDesc(user);

        ReportResponse response =
                new ReportResponse();

        response.setReportType(
                "USER ACTIVITY REPORT");

        response.setGeneratedAt(
                LocalDateTime.now());

        response.setTotalCredentials(
                credentials.size());

        response.setTotalNotifications(
                notifications.size());

        int unreadCount = 0;

        for (Notification notification : notifications) {

            if (!notification.isRead()) {
                unreadCount++;
            }
        }

        response.setUnreadNotifications(
                unreadCount);

        // -----------------------------------------------------
        // ACTIVITY DETAILS
        // -----------------------------------------------------

        List<Map<String, Object>> details =
                new ArrayList<>();

        for (Notification notification :
                notifications) {

            Map<String, Object> data =
                    new HashMap<>();

            data.put(
                    "notificationId",
                    notification.getId());

            if (notification.getNotificationType()
                    != null) {

                data.put(
                        "type",
                        notification
                                .getNotificationType()
                                .name());
            }

            data.put(
                    "title",
                    notification.getTitle());

            data.put(
                    "message",
                    notification.getMessage());

            data.put(
                    "read",
                    notification.isRead());

            data.put(
                    "createdAt",
                    notification.getCreatedAt());

            /*
             * Do not include:
             *
             * password
             * passwordHash
             * secureNote
             * FCM token
             */

            details.add(data);
        }

        response.setDetails(details);

        return response;
    }

    // =========================================================
    // THREAT MONITORING REPORT
    // =========================================================

    public ReportResponse generateThreatMonitoringReport(
            String email) {

        User user = getUser(email);

        List<Notification> notifications =
                notificationRepository
                        .findByUserOrderByCreatedAtDesc(user);

        ReportResponse response =
                new ReportResponse();

        response.setReportType(
                "THREAT MONITORING REPORT");

        response.setGeneratedAt(
                LocalDateTime.now());

        int threatCount = 0;
        int unreadThreatCount = 0;

        List<Map<String, Object>> details =
                new ArrayList<>();

        for (Notification notification :
                notifications) {

            if (notification.getNotificationType()
                    == null) {

                continue;
            }

            String type =
                    notification
                            .getNotificationType()
                            .name();

            boolean securityEvent =
                    type.contains("SECURITY")
                    || type.contains("LOGIN")
                    || type.contains("RISK")
                    || type.contains("THREAT")
                    || type.contains("ALERT");

            if (!securityEvent) {
                continue;
            }

            threatCount++;

            if (!notification.isRead()) {
                unreadThreatCount++;
            }

            Map<String, Object> data =
                    new HashMap<>();

            data.put(
                    "notificationId",
                    notification.getId());

            data.put(
                    "type",
                    type);

            data.put(
                    "title",
                    notification.getTitle());

            data.put(
                    "message",
                    notification.getMessage());

            data.put(
                    "read",
                    notification.isRead());

            data.put(
                    "createdAt",
                    notification.getCreatedAt());

            details.add(data);
        }

        response.setTotalNotifications(
                threatCount);

        response.setUnreadNotifications(
                unreadThreatCount);

        response.setDetails(details);

        return response;
    }

    // =========================================================
    // PASSWORD STRENGTH CALCULATOR
    // =========================================================

    private int calculatePasswordStrength(
            String password) {

        /*
         * No password
         */
        if (password == null ||
                password.isBlank()) {

            return 1;
        }

        int score = 0;

        // Length
        if (password.length() >= 8) {
            score++;
        }

        if (password.length() >= 12) {
            score++;
        }

        // Uppercase
        if (password.matches(
                ".*[A-Z].*")) {

            score++;
        }

        // Lowercase
        if (password.matches(
                ".*[a-z].*")) {

            score++;
        }

        // Number
        if (password.matches(
                ".*[0-9].*")) {

            score++;
        }

        // Special character
        if (password.matches(
                ".*[^a-zA-Z0-9].*")) {

            score++;
        }

        // -----------------------------------------------------
        // RESULT
        // -----------------------------------------------------

        if (score >= 5) {
            return 3; // STRONG
        }

        if (score >= 3) {
            return 2; // MEDIUM
        }

        return 1; // WEAK
    }
}