package com.securevault.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "security_alerts")
public class SecurityAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================
    // USER
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ==========================================
    // SECURITY EVENT
    // ==========================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "security_event_id")
    private SecurityEvent securityEvent;

    // ==========================================
    // ALERT TYPE
    // ==========================================

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 50)
    private AlertType alertType;

    // ==========================================
    // RISK LEVEL
    // ==========================================

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel;

    // ==========================================
    // MESSAGE
    // ==========================================

    @Column(nullable = false, length = 500)
    private String message;

    // ==========================================
    // IP ADDRESS
    // ==========================================

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    // ==========================================
    // USER AGENT
    // ==========================================

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    // ==========================================
    // READ STATUS
    // ==========================================

    @Column(name = "read_status", nullable = false)
    private boolean read = false;

    // ==========================================
    // CREATED TIME
    // ==========================================

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public SecurityAlert() {
    }

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // ==========================================
    // GETTERS / SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SecurityEvent getSecurityEvent() {
        return securityEvent;
    }

    public void setSecurityEvent(SecurityEvent securityEvent) {
        this.securityEvent = securityEvent;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ==========================================
    // ALERT TYPE
    // ==========================================

    public enum AlertType {

        NEW_DEVICE,

        REPEATED_LOGIN_FAILURES,

        ANOMALOUS_LOGIN,

        HIGH_RISK_LOGIN,

        CRITICAL_RISK
    }

    // ==========================================
    // RISK LEVEL
    // ==========================================

    public enum RiskLevel {

        LOW,

        MEDIUM,

        HIGH,

        CRITICAL
    }
}