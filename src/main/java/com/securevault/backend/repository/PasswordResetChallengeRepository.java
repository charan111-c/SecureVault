package com.securevault.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securevault.backend.entity.PasswordResetChallenge;

public interface PasswordResetChallengeRepository
        extends JpaRepository<PasswordResetChallenge, Long> {

    Optional<PasswordResetChallenge>
    findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(
            Long userId
    );

    void deleteByUserId(Long userId);
}
