package com.securevault.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securevault.backend.dto.ForgotPasswordRequest;
import com.securevault.backend.dto.LoginRequest;
import com.securevault.backend.dto.LoginResponse;
import com.securevault.backend.dto.MfaVerifyRequest;
import com.securevault.backend.dto.RegisterRequest;
import com.securevault.backend.dto.RegisterResponse;
import com.securevault.backend.dto.ResetPasswordRequest;
import com.securevault.backend.service.AuthService;
import com.securevault.backend.service.PasswordRecoveryService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordRecoveryService passwordRecoveryService;

    public AuthController(
            AuthService authService,
            PasswordRecoveryService passwordRecoveryService
    ) {
        this.authService = authService;
        this.passwordRecoveryService = passwordRecoveryService;
    }

    // =====================================================
    // REGISTER
    // =====================================================

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        RegisterResponse response =
                authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =====================================================
    // LOGIN
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {

        String ipAddress =
                httpRequest.getRemoteAddr();

        String userAgent =
                httpRequest.getHeader("User-Agent");

        LoginResponse response =
                authService.login(
                        request,
                        ipAddress,
                        userAgent
                );

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // VERIFY MFA
    // =====================================================

    @PostMapping("/verify-mfa")
    public ResponseEntity<LoginResponse> verifyMfa(
            @Valid @RequestBody MfaVerifyRequest request
    ) {

        LoginResponse response =
                authService.verifyMfa(
                        request.getEmail(),
                        request.getCode()
                );

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // FORGOT PASSWORD
    // =====================================================

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {

        passwordRecoveryService.createRecoveryChallenge(
                request.getEmail()
        );

        return ResponseEntity.ok(
                "Password recovery code generated"
        );
    }

    // =====================================================
    // RESET PASSWORD
    // =====================================================

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {

        passwordRecoveryService.resetPassword(
                request.getEmail(),
                request.getCode(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(
                "Password reset successful"
        );
    }
}