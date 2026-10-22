package com.securevault.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securevault.backend.dto.UserDeviceResponse;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.service.DeviceTrackingService;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceTrackingService deviceTrackingService;
    private final UserRepository userRepository;

    public DeviceController(
            DeviceTrackingService deviceTrackingService,
            UserRepository userRepository
    ) {
        this.deviceTrackingService = deviceTrackingService;
        this.userRepository = userRepository;
    }

    /*
     * Get devices belonging to the currently
     * authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<UserDeviceResponse>> getMyDevices(
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        )
                );

        List<UserDeviceResponse> devices =
                deviceTrackingService.getUserDevices(
                        user.getId()
                );

        return ResponseEntity.ok(devices);
    }
}