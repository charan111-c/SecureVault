package com.securevault.backend.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomUserDetailsService userDetailsService;

    private final AuthenticationEntryPointImpl authenticationEntryPoint;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService userDetailsService,
            AuthenticationEntryPointImpl authenticationEntryPoint
    ) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;

        this.userDetailsService =
                userDetailsService;

        this.authenticationEntryPoint =
                authenticationEntryPoint;
    }

    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    // =========================================================
    // AUTHENTICATION PROVIDER
    // =========================================================

    @Bean
    public AuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder
    ) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder
        );

        return provider;
    }

    // =========================================================
    // AUTHENTICATION MANAGER
    // =========================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration
                .getAuthenticationManager();
    }

    // =========================================================
    // CORS
    // =========================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173",
                        "http://localhost:5174"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider
    ) throws Exception {

        http

                // =================================================
                // CSRF
                // =================================================

                .csrf(csrf ->
                        csrf.disable()
                )

                // =================================================
                // CORS
                // =================================================

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                // =================================================
                // SESSION
                // =================================================

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // =================================================
                // AUTHENTICATION PROVIDER
                // =================================================

                .authenticationProvider(
                        authenticationProvider
                )

                // =================================================
                // AUTHORIZATION
                // =================================================

                .authorizeHttpRequests(auth -> auth

                        // -------------------------------------------------
                        // PUBLIC AUTHENTICATION
                        // -------------------------------------------------

                        .requestMatchers(
                                HttpMethod.POST,

                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/verify-mfa",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password"
                        ).permitAll()

                        // -------------------------------------------------
                        // CORS PREFLIGHT
                        // -------------------------------------------------

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // -------------------------------------------------
                        // ERROR
                        // -------------------------------------------------

                        .requestMatchers(
                                "/error"
                        ).permitAll()

                        // -------------------------------------------------
                        // GOOGLE OAUTH2
                        // -------------------------------------------------

                        .requestMatchers(
                                "/oauth2/**",
                                "/login/**"
                        ).permitAll()

                        // -------------------------------------------------
                        // TEST ENDPOINT
                        // -------------------------------------------------

                        .requestMatchers(
                                "/api/test/protected"
                        ).authenticated()

                        // =================================================
                        // ADMINISTRATOR ONLY
                        // =================================================

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMINISTRATOR")

                        // =================================================
                        // TEAM MEMBER + ADMINISTRATOR
                        // =================================================

                        .requestMatchers(
                                "/api/team-admin/**"
                        ).hasAnyRole(
                                "TEAM_MEMBER",
                                "ADMINISTRATOR"
                        )

                        // =================================================
                        // TEAM ACCESS
                        // =================================================

                        .requestMatchers(
                                "/api/teams/**",
                                "/api/team-access/**"
                        ).hasAnyRole(
                                "USER",
                                "TEAM_MEMBER",
                                "ADMINISTRATOR"
                        )

                        // =================================================
                        // PERSONAL VAULT
                        // =================================================

                        .requestMatchers(
                                "/api/vault/**"
                        ).hasAnyRole(
                                "USER",
                                "TEAM_MEMBER",
                                "ADMINISTRATOR"
                        )

                        // =================================================
                        // SECURITY / ANALYTICS
                        // =================================================

                        .requestMatchers(
                                "/api/threat/**",
                                "/api/security/**",
                                "/api/analytics/**"
                        ).hasAnyRole(
                                "USER",
                                "TEAM_MEMBER",
                                "ADMINISTRATOR"
                        )

                        // =================================================
                        // EVERYTHING ELSE
                        // =================================================

                        .anyRequest().authenticated()
                )

                // =========================================================
                // UNAUTHORIZED HANDLER
                // =========================================================

                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                authenticationEntryPoint
                        )
                )

                // =========================================================
                // JWT FILTER
                // =========================================================

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}