package com.securevault.backend.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.securevault.backend.dto.LoginRequest;
import com.securevault.backend.dto.LoginResponse;
import com.securevault.backend.dto.RegisterRequest;
import com.securevault.backend.dto.RegisterResponse;
import com.securevault.backend.entity.Role;
import com.securevault.backend.entity.SecurityEvent.LoginStatus;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.security.JwtService;
import com.securevault.backend.security.RiskLevel;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final SecurityEventService securityEventService;

    private final MfaService mfaService;

    private final RiskAnalysisService riskAnalysisService;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            SecurityEventService securityEventService,
            MfaService mfaService,
            RiskAnalysisService riskAnalysisService
    ) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.securityEventService = securityEventService;
        this.mfaService = mfaService;
        this.riskAnalysisService = riskAnalysisService;
    }

    // =========================================================
    // REGISTER
    // =========================================================

    public RegisterResponse register(
            RegisterRequest request
    ) {

        // Check username
        if (userRepository.existsByUsername(
                request.getUsername()
        )) {

            throw new IllegalArgumentException(
                    "Username already exists"
            );
        }

        // Check email
        if (userRepository.existsByEmail(
                request.getEmail()
        )) {

            throw new IllegalArgumentException(
                    "Email already exists"
            );
        }

        User user = new User();

        user.setUsername(
                request.getUsername()
        );

        user.setEmail(
                request.getEmail()
        );

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        // =====================================================
        // IMPORTANT
        // EVERY NEW REGISTRATION IS USER
        // =====================================================

        user.setRole(Role.USER);

        user.setMfaEnabled(false);

        User savedUser =
                userRepository.save(user);

        return new RegisterResponse(
                "User registered successfully",
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }

    // =========================================================
    // LOGIN
    // =========================================================

    public LoginResponse login(
            LoginRequest request,
            String ipAddress,
            String userAgent
    ) {

        User user =
                userRepository
                        .findByEmail(
                                request.getEmail()
                        )
                        .orElse(null);

        try {

            // =================================================
            // AUTHENTICATE EMAIL + PASSWORD
            // =================================================

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // =================================================
            // RISK ANALYSIS
            // =================================================

            RiskLevel riskLevel =
                    riskAnalysisService.analyzeLoginRisk(
                            user,
                            ipAddress,
                            userAgent
                    );

            System.out.println(
                    "Login risk for "
                            + request.getEmail()
                            + ": "
                            + riskLevel
            );

            // =================================================
            // RECORD SUCCESSFUL LOGIN
            // =================================================

            securityEventService.recordLoginEvent(
                    user,
                    LoginStatus.SUCCESS,
                    ipAddress,
                    userAgent
            );

            // =================================================
            // ANOMALOUS LOGIN
            // =================================================

            if (riskAnalysisService.isAnomalous(
                    riskLevel
            )) {

                securityEventService
                        .recordAnomalousLoginEvent(
                                user,
                                ipAddress,
                                userAgent
                        );

                System.out.println(
                        "WARNING: Anomalous login detected for "
                                + request.getEmail()
                                + " | Risk: "
                                + riskLevel
                );
            }

            // =================================================
            // MFA
            // =================================================

            if (user != null
                    && user.isMfaEnabled()) {

                String code =
                        mfaService.createChallenge(
                                user
                        );

                System.out.println(
                        "================================="
                );

                System.out.println(
                        "MFA CODE FOR "
                                + user.getEmail()
                                + ": "
                                + code
                );

                System.out.println(
                        "RISK LEVEL: "
                                + riskLevel
                );

                System.out.println(
                        "================================="
                );

                return new LoginResponse(
                        "MFA verification required",
                        true
                );
            }

            // =================================================
            // GENERATE JWT
            // =================================================

            String token =
                    jwtService.generateToken(
                            user.getEmail()
                    );

            // =================================================
            // RETURN ROLE TO FRONTEND
            // =================================================

            return new LoginResponse(
                    "Login successful",
                    token,
                    "Bearer",
                    user.getEmail(),
                    user.getUsername(),
                    user.getRole().name()
            );

        } catch (RuntimeException exception) {

            // =================================================
            // RECORD FAILED LOGIN
            // =================================================

            securityEventService.recordLoginEvent(
                    user,
                    LoginStatus.FAILURE,
                    ipAddress,
                    userAgent
            );

            throw exception;
        }
    }

    // =========================================================
    // VERIFY MFA
    // =========================================================

    public LoginResponse verifyMfa(
            String email,
            String code
    ) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found"
                                )
                        );

        // =====================================================
        // CHECK MFA
        // =====================================================

        if (!user.isMfaEnabled()) {

            throw new IllegalArgumentException(
                    "MFA is not enabled for this account"
            );
        }

        // =====================================================
        // VERIFY CODE
        // =====================================================

        boolean valid =
                mfaService.verifyCode(
                        user,
                        code
                );

        if (!valid) {

            throw new IllegalArgumentException(
                    "Invalid or expired MFA code"
            );
        }

        // =====================================================
        // GENERATE JWT
        // =====================================================

        String token =
                jwtService.generateToken(
                        user.getEmail()
                );

        // =====================================================
        // RETURN ROLE
        // =====================================================

        return new LoginResponse(
                "MFA verification successful",
                token,
                "Bearer",
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );
    }
}