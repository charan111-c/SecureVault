package com.securevault.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securevault.backend.dto.UserDeviceResponse;
import com.securevault.backend.entity.User;
import com.securevault.backend.entity.UserDevice;
import com.securevault.backend.repository.UserDeviceRepository;

@Service
public class DeviceTrackingService {

    private final UserDeviceRepository userDeviceRepository;

    public DeviceTrackingService(
            UserDeviceRepository userDeviceRepository
    ) {
        this.userDeviceRepository = userDeviceRepository;
    }

    @Transactional
    public UserDevice recordDevice(
            User user,
            String ipAddress,
            String userAgent
    ) {

        String fingerprint =
                generateFingerprint(ipAddress, userAgent);

        LocalDateTime now = LocalDateTime.now();

        UserDevice device =
                userDeviceRepository
                        .findByUserIdAndDeviceFingerprint(
                                user.getId(),
                                fingerprint
                        )
                        .orElse(null);

        if (device == null) {

            device = new UserDevice();

            device.setUser(user);
            device.setDeviceFingerprint(fingerprint);
            device.setIpAddress(ipAddress);
            device.setUserAgent(userAgent);
            device.setFirstSeen(now);
            device.setLastSeen(now);
            device.setLoginCount(1L);

        } else {

            device.setIpAddress(ipAddress);
            device.setUserAgent(userAgent);
            device.setLastSeen(now);

            device.setLoginCount(
                    device.getLoginCount() + 1
            );
        }

        return userDeviceRepository.save(device);
    }

    @Transactional(readOnly = true)
    public List<UserDeviceResponse> getUserDevices(
            Long userId
    ) {

        return userDeviceRepository
                .findByUserIdOrderByLastSeenDesc(userId)
                .stream()
                .map(device ->
                        new UserDeviceResponse(
                                device.getId(),
                                device.getDeviceFingerprint(),
                                device.getIpAddress(),
                                device.getUserAgent(),
                                device.getFirstSeen(),
                                device.getLastSeen(),
                                device.getLoginCount()
                        )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isKnownDevice(
            Long userId,
            String ipAddress,
            String userAgent
    ) {

        String fingerprint =
                generateFingerprint(ipAddress, userAgent);

        return userDeviceRepository
                .findByUserIdAndDeviceFingerprint(
                        userId,
                        fingerprint
                )
                .isPresent();
    }

    private String generateFingerprint(
            String ipAddress,
            String userAgent
    ) {

        String raw =
                String.valueOf(ipAddress)
                        + "|"
                        + String.valueOf(userAgent);

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            raw.getBytes(StandardCharsets.UTF_8)
                    );

            StringBuilder result =
                    new StringBuilder();

            for (byte b : hash) {
                result.append(
                        String.format("%02x", b)
                );
            }

            return result.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "Unable to generate device fingerprint",
                    e
            );
        }
    }
}
