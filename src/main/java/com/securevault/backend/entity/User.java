package com.securevault.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    // =========================================================
    // ID
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // USERNAME
    // =========================================================

    @Column(
        nullable = false,
        unique = true,
        length = 50
    )
    private String username;

    // =========================================================
    // EMAIL
    // =========================================================

    @Column(
        nullable = false,
        unique = true,
        length = 150
    )
    private String email;

    // =========================================================
    // PASSWORD
    // =========================================================

    @Column(
        name = "password_hash",
        nullable = false
    )
    private String passwordHash;

    // =========================================================
    // ROLE
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 30
    )
    private Role role = Role.USER;

    // =========================================================
    // MFA
    // =========================================================

    @Column(
        name = "mfa_enabled",
        nullable = false
    )
    private boolean mfaEnabled = false;

    // =========================================================
    // FCM TOKEN
    // =========================================================

    @Column(
        name = "fcm_token",
        length = 500
    )
    private String fcmToken;

    // =========================================================
    // CREATED AT
    // =========================================================

    @Column(
        name = "created_at",
        nullable = false
    )
    private LocalDateTime createdAt;

    // =========================================================
    // UPDATED AT
    // =========================================================

    @Column(
        name = "updated_at",
        nullable = false
    )
    private LocalDateTime updatedAt;

    // =========================================================
    // PRE PERSIST
    // =========================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (role == null) {
            role = Role.USER;
        }
    }

    // =========================================================
    // PRE UPDATE
    // =========================================================

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }

    // =========================================================
    // GET ID
    // =========================================================

    public Long getId() {
        return id;
    }

    // =========================================================
    // SET ID
    // =========================================================

    public void setId(Long id) {
        this.id = id;
    }

    // =========================================================
    // GET USERNAME
    // =========================================================

    public String getUsername() {
        return username;
    }

    // =========================================================
    // SET USERNAME
    // =========================================================

    public void setUsername(String username) {
        this.username = username;
    }

    // =========================================================
    // GET EMAIL
    // =========================================================

    public String getEmail() {
        return email;
    }

    // =========================================================
    // SET EMAIL
    // =========================================================

    public void setEmail(String email) {
        this.email = email;
    }

    // =========================================================
    // GET PASSWORD HASH
    // =========================================================

    public String getPasswordHash() {
        return passwordHash;
    }

    // =========================================================
    // SET PASSWORD HASH
    // =========================================================

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // =========================================================
    // GET ROLE
    // =========================================================

    public Role getRole() {
        return role;
    }

    // =========================================================
    // SET ROLE
    // =========================================================

    public void setRole(Role role) {
        this.role = role;
    }

    // =========================================================
    // GET MFA ENABLED
    // =========================================================

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    // =========================================================
    // SET MFA ENABLED
    // =========================================================

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    // =========================================================
    // GET FCM TOKEN
    // =========================================================

    public String getFcmToken() {
        return fcmToken;
    }

    // =========================================================
    // SET FCM TOKEN
    // =========================================================

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    // =========================================================
    // GET CREATED AT
    // =========================================================

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // =========================================================
    // GET UPDATED AT
    // =========================================================

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}