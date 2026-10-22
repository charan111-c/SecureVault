package com.securevault.backend.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.securevault.backend.entity.PasswordResetToken;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.PasswordResetTokenRepository;
import com.securevault.backend.repository.UserRepository;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordHistoryService passwordHistoryService;

    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            PasswordResetTokenRepository passwordResetTokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PasswordHistoryService passwordHistoryService
    ) {
        this.passwordResetTokenRepository =
                passwordResetTokenRepository;

        this.userRepository =
                userRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.passwordHistoryService =
                passwordHistoryService;
    }

    // ==========================================
    // CREATE PASSWORD RESET TOKEN
    // ==========================================

    /**
     * Creates a password reset token for the supplied email.
     *
     * Development version:
     * The token is returned to the frontend so the reset
     * workflow can be tested without an email service.
     */
    @Transactional
    public String createResetToken(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No account found with this email address."
                        )
                );

        // Remove older reset tokens for this user.
        passwordResetTokenRepository
                .deleteByUserId(user.getId());

        // Generate secure random token
        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setToken(token);
        resetToken.setUser(user);

        // Token valid for 15 minutes
        resetToken.setExpirationDate(
                LocalDateTime.now().plusMinutes(15)
        );

        resetToken.setUsed(false);

        passwordResetTokenRepository.save(resetToken);

        return token;
    }

    // ==========================================
    // RESET PASSWORD
    // ==========================================

    /**
     * Resets the user's password using a valid reset token.
     *
     * Password history is checked before changing
     * the password.
     */
    @Transactional
    public void resetPassword(
            String token,
            String newPassword
    ) {

        // ==========================================
        // VALIDATE TOKEN INPUT
        // ==========================================

        if (token == null || token.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reset token is required."
            );
        }

        // ==========================================
        // VALIDATE PASSWORD INPUT
        // ==========================================

        if (newPassword == null || newPassword.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password is required."
            );
        }

        if (newPassword.length() < 8) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must contain at least 8 characters."
            );
        }

        // ==========================================
        // FIND RESET TOKEN
        // ==========================================

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByToken(token)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Invalid reset token."
                                )
                        );

        // ==========================================
        // CHECK TOKEN USED
        // ==========================================

        if (resetToken.isUsed()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This reset token has already been used."
            );
        }

        // ==========================================
        // CHECK TOKEN EXPIRATION
        // ==========================================

        if (resetToken
                .getExpirationDate()
                .isBefore(LocalDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This reset token has expired."
            );
        }

        // ==========================================
        // GET USER
        // ==========================================

        User user = resetToken.getUser();

        if (user == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User associated with reset token was not found."
            );
        }

        // ==========================================
        // CHECK CURRENT PASSWORD
        // ==========================================

        /*
         * Prevent the user from choosing the exact
         * same password that is currently active.
         */
        if (user.getPasswordHash() != null
                && passwordEncoder.matches(
                        newPassword,
                        user.getPasswordHash()
                )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password must be different from the current password."
            );
        }

        // ==========================================
        // CHECK PASSWORD HISTORY
        // ==========================================

        try {

            passwordHistoryService.validateNewPassword(
                    user,
                    newPassword
            );

        } catch (IllegalArgumentException exception) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage()
            );
        }

        // ==========================================
        // SAVE CURRENT PASSWORD TO HISTORY
        // ==========================================

        /*
         * Store the currently active password hash
         * before replacing it.
         */
        if (user.getPasswordHash() != null
                && !user.getPasswordHash().isBlank()) {

            passwordHistoryService.savePasswordHistory(
                    user,
                    user.getPasswordHash()
            );
        }

        // ==========================================
        // HASH NEW PASSWORD
        // ==========================================

        /*
         * User login passwords are stored using BCrypt.
         * They are NOT stored using AES encryption.
         */
        String hashedPassword =
                passwordEncoder.encode(newPassword);

        // ==========================================
        // UPDATE USER PASSWORD
        // ==========================================

        user.setPasswordHash(hashedPassword);

        userRepository.save(user);

        // ==========================================
        // MARK RESET TOKEN AS USED
        // ==========================================

        resetToken.setUsed(true);

        passwordResetTokenRepository.save(resetToken);
    }
}