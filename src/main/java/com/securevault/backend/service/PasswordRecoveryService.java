package com.securevault.backend.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securevault.backend.entity.PasswordResetChallenge;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.PasswordResetChallengeRepository;
import com.securevault.backend.repository.UserRepository;

@Service
public class PasswordRecoveryService {

    private static final int CODE_EXPIRATION_MINUTES = 10;

    private final UserRepository userRepository;
    private final PasswordResetChallengeRepository challengeRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public PasswordRecoveryService(
            UserRepository userRepository,
            PasswordResetChallengeRepository challengeRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================================
    // REQUEST PASSWORD RECOVERY
    // =====================================================

    @Transactional
    public String createRecoveryChallenge(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No account found with this email"
                        )
                );

        // Remove previous active recovery codes.
        challengeRepository.deleteByUserId(
                user.getId()
        );

        // Generate six-digit recovery code.
        String code = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        PasswordResetChallenge challenge =
                new PasswordResetChallenge();

        challenge.setUser(user);
        challenge.setCode(code);

        challenge.setExpirationTime(
                LocalDateTime.now()
                        .plusMinutes(
                                CODE_EXPIRATION_MINUTES
                        )
        );

        challenge.setUsed(false);

        challengeRepository.save(challenge);

        /*
         * DEVELOPMENT MODE
         *
         * Later replace this with Email delivery.
         */
        System.out.println(
                "======================================"
        );

        System.out.println(
                "PASSWORD RECOVERY CODE"
        );

        System.out.println(
                "Email: " + user.getEmail()
        );

        System.out.println(
                "Code: " + code
        );

        System.out.println(
                "Expires in "
                        + CODE_EXPIRATION_MINUTES
                        + " minutes"
        );

        System.out.println(
                "======================================"
        );

        return code;
    }

    // =====================================================
    // RESET PASSWORD
    // =====================================================

    @Transactional
    public void resetPassword(
            String email,
            String code,
            String newPassword
    ) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid recovery request"
                        )
                );

        PasswordResetChallenge challenge =
                challengeRepository
                        .findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid or expired recovery code"
                                )
                        );

        // Check expiration.
        if (challenge
                .getExpirationTime()
                .isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Recovery code has expired"
            );
        }

        // Check recovery code.
        if (!challenge.getCode().equals(code)) {

            throw new IllegalArgumentException(
                    "Invalid recovery code"
            );
        }

        // Securely hash the new password.
        String encodedPassword =
                passwordEncoder.encode(
                        newPassword
                );

        user.setPasswordHash(
                encodedPassword
        );

        userRepository.save(user);

        // Make recovery code single-use.
        challenge.setUsed(true);

        challengeRepository.save(challenge);
    }
}