package com.securevault.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.securevault.backend.entity.AuditLog;
import com.securevault.backend.entity.User;

@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserOrderByCreatedAtDesc(User user);

    List<AuditLog> findByUserAndSuccessfulOrderByCreatedAtDesc(
            User user,
            boolean successful
    );

    long countByUser(User user);

    long countByUserAndSuccessful(
            User user,
            boolean successful
    );
}