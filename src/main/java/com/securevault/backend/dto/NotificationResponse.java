package com.securevault.backend.dto;

import java.time.LocalDateTime;

import com.securevault.backend.entity.Notification;

public class NotificationResponse {

    private Long id;
    private String type;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    // =========================================================
    // CONSTRUCTOR 1
    // Accept Notification entity
    // =========================================================

    public NotificationResponse(Notification notification) {

        this.id = notification.getId();

        this.type = notification.getNotificationType() != null
                ? notification.getNotificationType().name()
                : null;

        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.read = notification.isRead();
        this.createdAt = notification.getCreatedAt();
    }

    // =========================================================
    // CONSTRUCTOR 2
    // Accept individual values
    // =========================================================

    public NotificationResponse(
            Long id,
            String type,
            String title,
            String message,
            boolean read,
            LocalDateTime createdAt) {

        this.id = id;
        this.type = type;
        this.title = title;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}