package com.securevault.backend.dto;

public class CredentialResponse {

    private Long id;
    private String title;
    private String username;
    private String password;
    private String category;
    private String credentialType;
    private boolean favorite;
    private String secureNote;

    public CredentialResponse(
            Long id,
            String title,
            String username,
            String password,
            String category,
            String credentialType,
            boolean favorite,
            String secureNote) {

        this.id = id;
        this.title = title;
        this.username = username;
        this.password = password;
        this.category = category;
        this.credentialType = credentialType;
        this.favorite = favorite;
        this.secureNote = secureNote;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCategory() {
        return category;
    }

    public String getCredentialType() {
        return credentialType;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public String getSecureNote() {
        return secureNote;
    }
}