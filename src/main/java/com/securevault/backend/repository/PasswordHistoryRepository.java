package com.securevault.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securevault.backend.entity.PasswordHistory;
import com.securevault.backend.entity.User;

public interface PasswordHistoryRepository
        extends JpaRepository<PasswordHistory, Long> {

    List<PasswordHistory> findByUserOrderByCreatedAtDesc(User user);
}