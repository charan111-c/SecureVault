package com.securevault.backend.service;

import org.springframework.stereotype.Service;

import com.securevault.backend.entity.User;

@Service
public class PushNotificationService {

    public void send(
            User user,
            String title,
            String message) {

        // Firebase push notifications are disabled.
    }
}