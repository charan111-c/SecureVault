package com.securevault.backend.dto;

import java.time.LocalDateTime;

public class UserDeviceResponse {

    private Long id;
    private String deviceFingerprint;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime firstSeen;
    private LocalDateTime lastSeen;
    private Long loginCount;

    public UserDeviceResponse(
            Long id,
            String deviceFingerprint,
            String ipAddress,
            String userAgent,
            LocalDateTime firstSeen,
            LocalDateTime lastSeen,
            Long loginCount
    ) {
        this.id = id;
        this.deviceFingerprint = deviceFingerprint;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
        this.loginCount = loginCount;
    }

    public Long getId() {
        return id;
    }

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public LocalDateTime getFirstSeen() {
        return firstSeen;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public Long getLoginCount() {
        return loginCount;
    }
}