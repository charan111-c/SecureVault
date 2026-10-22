package com.securevault.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.securevault.backend.entity.Notification;
import com.securevault.backend.entity.User;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    // Get all notifications for a user
    List<Notification> findByUserOrderByCreatedAtDesc(
            User user
    );

    // Get unread notifications for a user
    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(
            User user
    );

    // Get a specific notification belonging to a user
    java.util.Optional<Notification> findByIdAndUser(
            Long id,
            User user
    );

    // Count unread notifications
    long countByUserAndReadFalse(
            User user
    );
}