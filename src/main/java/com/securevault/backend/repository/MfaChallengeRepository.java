package com.securevault.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securevault.backend.entity.MfaChallenge;

public interface MfaChallengeRepository
        extends JpaRepository<MfaChallenge, Long> {

    Optional<MfaChallenge>
    findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(
            Long userId
    );

    void deleteByUserId(Long userId);
}