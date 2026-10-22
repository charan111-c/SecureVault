package com.securevault.backend.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securevault.backend.entity.MfaChallenge;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.MfaChallengeRepository;

@Service
public class MfaService {

    private static final int CODE_EXPIRATION_MINUTES = 5;

    private final MfaChallengeRepository mfaChallengeRepository;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public MfaService(
            MfaChallengeRepository mfaChallengeRepository
    ) {
        this.mfaChallengeRepository =
                mfaChallengeRepository;
    }

    @Transactional
    public String createChallenge(User user) {

        // Remove previous unused challenges.
        mfaChallengeRepository.deleteByUserId(
                user.getId()
        );

        // Generate a six-digit code.
        String code = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        MfaChallenge challenge =
                new MfaChallenge();

        challenge.setUser(user);
        challenge.setCode(code);

        challenge.setExpirationTime(
                LocalDateTime.now()
                        .plusMinutes(
                                CODE_EXPIRATION_MINUTES
                        )
        );

        challenge.setUsed(false);

        mfaChallengeRepository.save(challenge);

        return code;
    }

    @Transactional
    public boolean verifyCode(
            User user,
            String code
    ) {

        if (user == null ||
                code == null ||
                code.isBlank()) {

            return false;
        }

        MfaChallenge challenge =
                mfaChallengeRepository
                        .findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(
                                user.getId()
                        )
                        .orElse(null);

        if (challenge == null) {
            return false;
        }

        if (
                challenge
                        .getExpirationTime()
                        .isBefore(LocalDateTime.now())
        ) {
            return false;
        }

        if (!challenge.getCode().equals(code)) {
            return false;
        }

        challenge.setUsed(true);

        mfaChallengeRepository.save(challenge);

        return true;
    }
}