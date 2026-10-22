package com.securevault.backend.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.securevault.backend.entity.PasswordHistory;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.PasswordHistoryRepository;

@Service
public class PasswordHistoryService {

    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordHistoryService(
            PasswordHistoryRepository passwordHistoryRepository,
            PasswordEncoder passwordEncoder) {

        this.passwordHistoryRepository = passwordHistoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void savePasswordHistory(User user, String passwordHash) {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Password hash cannot be empty");
        }

        PasswordHistory history = new PasswordHistory();

        history.setUser(user);
        history.setPasswordHash(passwordHash);

        passwordHistoryRepository.save(history);
    }

    public boolean isPasswordPreviouslyUsed(
            User user,
            String rawPassword) {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be empty");
        }

        List<PasswordHistory> historyList =
                passwordHistoryRepository
                        .findByUserOrderByCreatedAtDesc(user);

        for (PasswordHistory history : historyList) {

            if (passwordEncoder.matches(
                    rawPassword,
                    history.getPasswordHash())) {

                return true;
            }
        }

        return false;
    }

    public void validateNewPassword(
            User user,
            String rawPassword) {

        if (isPasswordPreviouslyUsed(user, rawPassword)) {

            throw new IllegalArgumentException(
                    "Password was previously used. " +
                    "Please choose a different password.");
        }
    }

    public List<PasswordHistory> getPasswordHistory(User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null");
        }

        return passwordHistoryRepository
                .findByUserOrderByCreatedAtDesc(user);
    }
}