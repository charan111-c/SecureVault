package com.securevault.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securevault.backend.entity.AuditLog;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.AuditLogRepository;
import com.securevault.backend.repository.UserRepository;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository) {

        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // RECORD AUDIT EVENT
    // =========================================================

    @Transactional
    public AuditLog record(
            String email,
            String action,
            String resource,
            String description,
            String ipAddress,
            String userAgent,
            boolean successful) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        AuditLog auditLog = new AuditLog();

        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setResource(resource);
        auditLog.setDescription(description);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLog.setSuccessful(successful);

        return auditLogRepository.save(auditLog);
    }

    // =========================================================
    // SIMPLE RECORD METHOD
    // =========================================================

    @Transactional
    public AuditLog record(
            String email,
            String action,
            String resource,
            String description) {

        return record(
                email,
                action,
                resource,
                description,
                null,
                null,
                true
        );
    }
}