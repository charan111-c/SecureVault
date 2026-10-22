package com.securevault.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securevault.backend.dto.NotificationResponse;
import com.securevault.backend.entity.Notification;
import com.securevault.backend.entity.User;
import com.securevault.backend.enums.NotificationType;
import com.securevault.backend.repository.NotificationRepository;
import com.securevault.backend.repository.UserRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;
    private final PushNotificationService pushNotificationService;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            EmailNotificationService emailNotificationService,
            PushNotificationService pushNotificationService) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.emailNotificationService = emailNotificationService;
        this.pushNotificationService = pushNotificationService;
    }

    // =========================================================
    // LOGIN ALERT
    // =========================================================

    @Transactional
    public Notification notifyLogin(
            User user,
            String ipAddress,
            String userAgent) {

        String message =
                "A successful login was detected on your "
                + "SecureVault account.\n\n"
                + "IP Address: "
                + safe(ipAddress)
                + "\n"
                + "Device: "
                + safe(userAgent)
                + "\n\n"
                + "If this was not you, secure your account immediately.";

        return createAndDeliver(
                user,
                NotificationType.LOGIN_ALERT,
                "New Login Detected",
                message
        );
    }

    // =========================================================
    // SECURITY ALERT
    // =========================================================

    @Transactional
    public Notification notifySecurity(
            User user,
            String message) {

        return createAndDeliver(
                user,
                NotificationType.SECURITY_ALERT,
                "Security Alert",
                message
        );
    }

    // =========================================================
    // RISK ALERT
    // =========================================================

    @Transactional
    public Notification notifyRisk(
            User user,
            String ipAddress,
            String userAgent) {

        String message =
                "SecureVault detected unusual login activity "
                + "on your account.\n\n"
                + "IP Address: "
                + safe(ipAddress)
                + "\n"
                + "Device: "
                + safe(userAgent)
                + "\n\n"
                + "Please review your account security.";

        return createAndDeliver(
                user,
                NotificationType.RISK_ALERT,
                "Suspicious Login Activity",
                message
        );
    }

    // =========================================================
    // SHARING NOTIFICATION
    // =========================================================

    @Transactional
    public Notification notifySharing(
            User user,
            String eventMessage) {

        return createAndDeliver(
                user,
                NotificationType.SHARING_NOTIFICATION,
                "Vault Sharing Notification",
                safe(eventMessage)
        );
    }

    // =========================================================
    // PASSWORD EXPIRATION
    // =========================================================

    @Transactional
    public Notification notifyPasswordExpiration(
            User user,
            String message) {

        return createAndDeliver(
                user,
                NotificationType.PASSWORD_EXPIRATION,
                "Password Expiration Alert",
                safe(message)
        );
    }

    // =========================================================
    // CREATE AND DELIVER
    // =========================================================

    @Transactional
    public Notification createAndDeliver(
            User user,
            NotificationType type,
            String title,
            String message) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "Notification recipient cannot be null"
            );
        }

        if (type == null) {
            throw new IllegalArgumentException(
                    "Notification type cannot be null"
            );
        }

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setNotificationType(type);
        notification.setTitle(safe(title));
        notification.setMessage(safe(message));

        /*
         * Save the notification first.
         *
         * Even if email or push delivery fails,
         * the notification remains stored.
         */
        notification =
                notificationRepository.save(notification);

        // =====================================================
        // EMAIL
        // =====================================================

        try {

            emailNotificationService.send(
                    user,
                    title,
                    message
            );

            notification.setEmailSent(true);
            notification.setEmailError(null);

        } catch (Exception exception) {

            notification.setEmailSent(false);

            notification.setEmailError(
                    getErrorMessage(exception)
            );
        }

        // =====================================================
        // PUSH
        // =====================================================

        /*
         * Push notification is optional.
         *
         * If the user does not have an FCM token,
         * the email/in-app notification should still work.
         */
        if (user.getFcmToken() != null
                && !user.getFcmToken().isBlank()) {

            try {

                pushNotificationService.send(
                        user,
                        title,
                        message
                );

                notification.setPushSent(true);
                notification.setPushError(null);

            } catch (Exception exception) {

                notification.setPushSent(false);

                notification.setPushError(
                        getErrorMessage(exception)
                );
            }

        } else {

            notification.setPushSent(false);

            notification.setPushError(
                    "No FCM device token registered"
            );
        }

        return notificationRepository.save(notification);
    }

    // =========================================================
    // GET ALL NOTIFICATIONS
    // =========================================================

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(
            String email) {

        User user = getUser(email);

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET UNREAD NOTIFICATIONS
    // =========================================================

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(
            String email) {

        User user = getUser(email);

        return notificationRepository
                .findByUserAndReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // MARK ONE AS READ
    // =========================================================

    @Transactional
    public NotificationResponse markAsRead(
            Long notificationId,
            String email) {

        User user = getUser(email);

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification not found"
                                )
                        );

        checkOwnership(notification, user);

        notification.setRead(true);

        notification =
                notificationRepository.save(notification);

        return toResponse(notification);
    }

    // =========================================================
    // MARK ALL AS READ
    // =========================================================

    @Transactional
    public void markAllAsRead(String email) {

        User user = getUser(email);

        List<Notification> notifications =
                notificationRepository
                        .findByUserOrderByCreatedAtDesc(user);

        for (Notification notification : notifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(notifications);
    }

    // =========================================================
    // DELETE ONE NOTIFICATION
    // =========================================================

    @Transactional
    public void deleteNotification(
            Long notificationId,
            String email) {

        User user = getUser(email);

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification not found"
                                )
                        );

        checkOwnership(notification, user);

        notificationRepository.delete(notification);
    }

    // =========================================================
    // DELETE ALL NOTIFICATIONS
    // =========================================================

    @Transactional
    public void deleteAllNotifications(String email) {

        User user = getUser(email);

        List<Notification> notifications =
                notificationRepository
                        .findByUserOrderByCreatedAtDesc(user);

        notificationRepository.deleteAll(notifications);
    }

    // =========================================================
    // ENTITY -> RESPONSE
    // =========================================================

    private NotificationResponse toResponse(
            Notification notification) {

        return new NotificationResponse(
                notification.getId(),
                notification.getNotificationType() != null
                        ? notification
                                .getNotificationType()
                                .name()
                        : null,
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }

    // =========================================================
    // GET USER
    // =========================================================

    private User getUser(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );
    }

    // =========================================================
    // OWNERSHIP CHECK
    // =========================================================

    private void checkOwnership(
            Notification notification,
            User user) {

        if (notification.getUser() == null
                || notification.getUser().getId() == null
                || user.getId() == null
                || !notification
                        .getUser()
                        .getId()
                        .equals(user.getId())) {

            throw new IllegalArgumentException(
                    "You do not have permission to access this notification"
            );
        }
    }

    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safe(String value) {

        if (value == null || value.isBlank()) {
            return "Unknown";
        }

        return value;
    }

    // =========================================================
    // ERROR MESSAGE
    // =========================================================

    private String getErrorMessage(
            Exception exception) {

        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return exception
                    .getClass()
                    .getSimpleName();
        }

        return message.length() > 1000
                ? message.substring(0, 1000)
                : message;
    }
}