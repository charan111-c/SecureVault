package com.securevault.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securevault.backend.entity.SecurityNotification;

public interface SecurityNotificationRepository
        extends JpaRepository<SecurityNotification, Long> {

    List<SecurityNotification>
    findByUserIdOrderByCreatedAtDesc(Long userId);

    List<SecurityNotification>
    findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);
}