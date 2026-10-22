package com.securevault.backend.dto;

public class LoginResponse {

    // =========================================================
    // RESPONSE MESSAGE
    // =========================================================

    private String message;

    // =========================================================
    // JWT TOKEN
    // =========================================================

    private String token;

    // =========================================================
    // TOKEN TYPE
    // =========================================================

    private String tokenType;

    // =========================================================
    // MFA
    // =========================================================

    private boolean mfaRequired;

    // =========================================================
    // USER INFORMATION
    // =========================================================

    private String email;

    private String username;

    private String role;

    // =========================================================
    // DEFAULT CONSTRUCTOR
    // =========================================================

    public LoginResponse() {
    }

    // =========================================================
    // NORMAL LOGIN RESPONSE
    // =========================================================

    public LoginResponse(
            String message,
            String token,
            String tokenType
    ) {

        this.message = message;
        this.token = token;
        this.tokenType = tokenType;
        this.mfaRequired = false;
    }

    // =========================================================
    // NORMAL LOGIN RESPONSE WITH USER DETAILS
    // =========================================================

    public LoginResponse(
            String message,
            String token,
            String tokenType,
            String email,
            String username,
            String role
    ) {

        this.message = message;
        this.token = token;
        this.tokenType = tokenType;
        this.mfaRequired = false;

        this.email = email;
        this.username = username;
        this.role = role;
    }

    // =========================================================
    // MFA RESPONSE
    // =========================================================

    public LoginResponse(
            String message,
            boolean mfaRequired
    ) {

        this.message = message;
        this.mfaRequired = mfaRequired;

        this.token = null;
        this.tokenType = null;

        this.email = null;
        this.username = null;
        this.role = null;
    }

    // =========================================================
    // GET MESSAGE
    // =========================================================

    public String getMessage() {
        return message;
    }

    // =========================================================
    // SET MESSAGE
    // =========================================================

    public void setMessage(String message) {
        this.message = message;
    }

    // =========================================================
    // GET TOKEN
    // =========================================================

    public String getToken() {
        return token;
    }

    // =========================================================
    // SET TOKEN
    // =========================================================

    public void setToken(String token) {
        this.token = token;
    }

    // =========================================================
    // GET TOKEN TYPE
    // =========================================================

    public String getTokenType() {
        return tokenType;
    }

    // =========================================================
    // SET TOKEN TYPE
    // =========================================================

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    // =========================================================
    // GET MFA REQUIRED
    // =========================================================

    public boolean isMfaRequired() {
        return mfaRequired;
    }

    // =========================================================
    // SET MFA REQUIRED
    // =========================================================

    public void setMfaRequired(boolean mfaRequired) {
        this.mfaRequired = mfaRequired;
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
    // GET ROLE
    // =========================================================

    public String getRole() {
        return role;
    }

    // =========================================================
    // SET ROLE
    // =========================================================

    public void setRole(String role) {
        this.role = role;
    }
}