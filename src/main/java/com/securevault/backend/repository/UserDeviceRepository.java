package com.securevault.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securevault.backend.entity.UserDevice;

public interface UserDeviceRepository
        extends JpaRepository<UserDevice, Long> {

    Optional<UserDevice> findByUserIdAndDeviceFingerprint(
            Long userId,
            String deviceFingerprint
    );

    List<UserDevice> findByUserIdOrderByLastSeenDesc(
            Long userId
    );
}