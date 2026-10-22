package com.securevault.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ReportResponse {

    private String reportType;
    private LocalDateTime generatedAt;

    private int totalCredentials;
    private int favoriteCredentials;

    private int strongPasswords;
    private int mediumPasswords;
    private int weakPasswords;

    private int totalNotifications;
    private int unreadNotifications;
    private int emailNotifications;
    private int pushNotifications;

    private List<Map<String, Object>> details;

    public ReportResponse() {
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public int getTotalCredentials() {
        return totalCredentials;
    }

    public void setTotalCredentials(int totalCredentials) {
        this.totalCredentials = totalCredentials;
    }

    public int getFavoriteCredentials() {
        return favoriteCredentials;
    }

    public void setFavoriteCredentials(int favoriteCredentials) {
        this.favoriteCredentials = favoriteCredentials;
    }

    public int getStrongPasswords() {
        return strongPasswords;
    }

    public void setStrongPasswords(int strongPasswords) {
        this.strongPasswords = strongPasswords;
    }

    public int getMediumPasswords() {
        return mediumPasswords;
    }

    public void setMediumPasswords(int mediumPasswords) {
        this.mediumPasswords = mediumPasswords;
    }

    public int getWeakPasswords() {
        return weakPasswords;
    }

    public void setWeakPasswords(int weakPasswords) {
        this.weakPasswords = weakPasswords;
    }

    public int getTotalNotifications() {
        return totalNotifications;
    }

    public void setTotalNotifications(int totalNotifications) {
        this.totalNotifications = totalNotifications;
    }

    public int getUnreadNotifications() {
        return unreadNotifications;
    }

    public void setUnreadNotifications(int unreadNotifications) {
        this.unreadNotifications = unreadNotifications;
    }

    public int getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(int emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public int getPushNotifications() {
        return pushNotifications;
    }

    public void setPushNotifications(int pushNotifications) {
        this.pushNotifications = pushNotifications;
    }

    public List<Map<String, Object>> getDetails() {
        return details;
    }

    public void setDetails(List<Map<String, Object>> details) {
        this.details = details;
    }
}