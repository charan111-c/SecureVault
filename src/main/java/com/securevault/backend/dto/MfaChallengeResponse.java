package com.securevault.backend.dto;

public class MfaChallengeResponse {

    private String message;
    private boolean mfaRequired;

    public MfaChallengeResponse(
            String message,
            boolean mfaRequired
    ) {
        this.message = message;
        this.mfaRequired = mfaRequired;
    }

    public String getMessage() {
        return message;
    }

    public boolean isMfaRequired() {
        return mfaRequired;
    }
}