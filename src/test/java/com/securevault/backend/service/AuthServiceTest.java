package com.securevault.backend.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.securevault.backend.dto.LoginRequest;
import com.securevault.backend.dto.LoginResponse;
import com.securevault.backend.dto.RegisterRequest;
import com.securevault.backend.dto.RegisterResponse;
import com.securevault.backend.entity.SecurityEvent.LoginStatus;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private SecurityEventService securityEventService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    // =========================================
    // SETUP
    // =========================================

    @BeforeEach
    void setUp() {
        // Mockito creates and injects all mocks automatically.
    }

    // =========================================
    // REGISTRATION TESTS
    // =========================================

    @Test
    void register_shouldRegisterUserSuccessfully() {

        RegisterRequest request = new RegisterRequest();

        request.setUsername("charan");
        request.setEmail("charan@gmail.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("charan"))
                .thenReturn(false);

        when(userRepository.existsByEmail("charan@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        User savedUser = new User();

        savedUser.setId(1L);
        savedUser.setUsername("charan");
        savedUser.setEmail("charan@gmail.com");
        savedUser.setPasswordHash("encodedPassword");
        savedUser.setMfaEnabled(false);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        RegisterResponse response =
                authService.register(request);

        assertNotNull(response);

        assertEquals(
                "User registered successfully",
                response.getMessage());

        assertEquals(
                1L,
                response.getUserId());

        assertEquals(
                "charan",
                response.getUsername());

        assertEquals(
                "charan@gmail.com",
                response.getEmail());

        verify(userRepository)
                .existsByUsername("charan");

        verify(userRepository)
                .existsByEmail("charan@gmail.com");

        verify(passwordEncoder)
                .encode("password123");

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void register_shouldThrowExceptionWhenUsernameAlreadyExists() {

        RegisterRequest request = new RegisterRequest();

        request.setUsername("charan");
        request.setEmail("charan@gmail.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("charan"))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.register(request));

        assertEquals(
                "Username already exists",
                exception.getMessage());

        verify(userRepository)
                .existsByUsername("charan");

        verify(userRepository, never())
                .existsByEmail(anyString());

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void register_shouldThrowExceptionWhenEmailAlreadyExists() {

        RegisterRequest request = new RegisterRequest();

        request.setUsername("charan");
        request.setEmail("charan@gmail.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("charan"))
                .thenReturn(false);

        when(userRepository.existsByEmail("charan@gmail.com"))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.register(request));

        assertEquals(
                "Email already exists",
                exception.getMessage());

        verify(userRepository)
                .existsByUsername("charan");

        verify(userRepository)
                .existsByEmail("charan@gmail.com");

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void register_shouldEncodePasswordBeforeSaving() {

        RegisterRequest request = new RegisterRequest();

        request.setUsername("testuser");
        request.setEmail("test@gmail.com");
        request.setPassword("mypassword");

        when(userRepository.existsByUsername(anyString()))
                .thenReturn(false);

        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);

        when(passwordEncoder.encode("mypassword"))
                .thenReturn("hashedPassword");

        User savedUser = new User();

        savedUser.setId(2L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@gmail.com");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {

                    User user =
                            invocation.getArgument(0);

                    assertEquals(
                            "hashedPassword",
                            user.getPasswordHash());

                    assertNotEquals(
                            "mypassword",
                            user.getPasswordHash());

                    return savedUser;
                });

        authService.register(request);

        verify(passwordEncoder)
                .encode("mypassword");

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void register_shouldDisableMfaByDefault() {

        RegisterRequest request = new RegisterRequest();

        request.setUsername("mfauser");
        request.setEmail("mfa@gmail.com");
        request.setPassword("password");

        when(userRepository.existsByUsername(anyString()))
                .thenReturn(false);

        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded");

        User savedUser = new User();

        savedUser.setId(3L);
        savedUser.setUsername("mfauser");
        savedUser.setEmail("mfa@gmail.com");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {

                    User user =
                            invocation.getArgument(0);

                    assertFalse(
                            user.isMfaEnabled());

                    return savedUser;
                });

        authService.register(request);

        verify(userRepository)
                .save(any(User.class));
    }

    // =========================================
    // LOGIN SUCCESS TESTS
    // =========================================

    @Test
    void login_shouldLoginSuccessfully() {

        LoginRequest request = new LoginRequest();

        request.setEmail("charan@gmail.com");
        request.setPassword("password123");

        User user = new User();

        user.setId(1L);
        user.setUsername("charan");
        user.setEmail("charan@gmail.com");

        when(userRepository.findByEmail(
                "charan@gmail.com"))
                .thenReturn(Optional.of(user));

        /*
         * authenticate() returns Authentication.
         * Therefore we use when(...).thenReturn(...)
         * instead of doNothing().
         */
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtService.generateToken(
                "charan@gmail.com"))
                .thenReturn("jwt-token");

        LoginResponse response =
                authService.login(
                        request,
                        "127.0.0.1",
                        "Chrome");

        assertNotNull(response);

        assertEquals(
                "Login successful",
                response.getMessage());

        assertEquals(
                "jwt-token",
                response.getToken());

        assertEquals(
                "Bearer",
                response.getTokenType());

        verify(authenticationManager)
                .authenticate(any(
                        UsernamePasswordAuthenticationToken.class));

        verify(jwtService)
                .generateToken("charan@gmail.com");

        verify(securityEventService)
                .recordLoginEvent(
                        user,
                        LoginStatus.SUCCESS,
                        "127.0.0.1",
                        "Chrome");
    }

    @Test
    void login_shouldFindUserByEmail() {

        LoginRequest request = new LoginRequest();

        request.setEmail("user@gmail.com");
        request.setPassword("password");

        User user = new User();

        user.setEmail("user@gmail.com");

        when(userRepository.findByEmail(
                "user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(anyString()))
                .thenReturn("token");

        authService.login(
                request,
                "127.0.0.1",
                "Chrome");

        verify(userRepository)
                .findByEmail("user@gmail.com");
    }

    @Test
    void login_shouldGenerateJwtAfterAuthentication() {

        LoginRequest request = new LoginRequest();

        request.setEmail("user@gmail.com");
        request.setPassword("password");

        User user = new User();

        user.setEmail("user@gmail.com");

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(
                "user@gmail.com"))
                .thenReturn("generated-token");

        LoginResponse response =
                authService.login(
                        request,
                        "127.0.0.1",
                        "Chrome");

        assertEquals(
                "generated-token",
                response.getToken());

        verify(jwtService)
                .generateToken("user@gmail.com");
    }

    @Test
    void login_shouldRecordSuccessfulLoginEvent() {

        LoginRequest request = new LoginRequest();

        request.setEmail("success@gmail.com");
        request.setPassword("password");

        User user = new User();

        user.setEmail("success@gmail.com");

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(anyString()))
                .thenReturn("token");

        authService.login(
                request,
                "192.168.1.10",
                "Mozilla");

        verify(securityEventService)
                .recordLoginEvent(
                        user,
                        LoginStatus.SUCCESS,
                        "192.168.1.10",
                        "Mozilla");
    }

    // =========================================
    // LOGIN FAILURE TESTS
    // =========================================

    @Test
    void login_shouldRecordFailureWhenAuthenticationFails() {

        LoginRequest request = new LoginRequest();

        request.setEmail("charan@gmail.com");
        request.setPassword("wrongpassword");

        User user = new User();

        user.setEmail("charan@gmail.com");

        when(userRepository.findByEmail(
                "charan@gmail.com"))
                .thenReturn(Optional.of(user));

        RuntimeException authenticationException =
                new RuntimeException("Invalid credentials");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(authenticationException);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> authService.login(
                                request,
                                "127.0.0.1",
                                "Chrome"));

        assertEquals(
                "Invalid credentials",
                exception.getMessage());

        verify(securityEventService)
                .recordLoginEvent(
                        user,
                        LoginStatus.FAILURE,
                        "127.0.0.1",
                        "Chrome");

        verify(jwtService, never())
                .generateToken(anyString());
    }

    @Test
    void login_shouldPropagateAuthenticationException() {

        LoginRequest request = new LoginRequest();

        request.setEmail("wrong@gmail.com");
        request.setPassword("wrong");

        when(userRepository.findByEmail(
                "wrong@gmail.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                new RuntimeException("Authentication failed");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(exception);

        RuntimeException result =
                assertThrows(
                        RuntimeException.class,
                        () -> authService.login(
                                request,
                                "127.0.0.1",
                                "Chrome"));

        assertSame(
                exception,
                result);

        verify(securityEventService)
                .recordLoginEvent(
                        null,
                        LoginStatus.FAILURE,
                        "127.0.0.1",
                        "Chrome");
    }

    @Test
    void login_shouldNotGenerateTokenWhenAuthenticationFails() {

        LoginRequest request = new LoginRequest();

        request.setEmail("fail@gmail.com");
        request.setPassword("wrong");

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(
                        new RuntimeException("Invalid login"));

        assertThrows(
                RuntimeException.class,
                () -> authService.login(
                        request,
                        "127.0.0.1",
                        "Chrome"));

        verify(jwtService, never())
                .generateToken(anyString());
    }

    @Test
    void login_shouldPassCorrectCredentialsToAuthenticationManager() {

        LoginRequest request = new LoginRequest();

        request.setEmail("test@gmail.com");
        request.setPassword("secret123");

        User user = new User();

        user.setEmail("test@gmail.com");

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(anyString()))
                .thenReturn("token");

        authService.login(
                request,
                "127.0.0.1",
                "Chrome");

        verify(authenticationManager)
                .authenticate(
                        argThat(authenticationObject -> {

                            UsernamePasswordAuthenticationToken token =
                                    (UsernamePasswordAuthenticationToken)
                                            authenticationObject;

                            return token.getPrincipal()
                                    .equals("test@gmail.com")
                                    &&
                                    token.getCredentials()
                                            .equals("secret123");
                        }));
    }

    // =========================================
    // SECURITY EVENT TESTS
    // =========================================

    @Test
    void login_shouldRecordCorrectIpAddress() {

        LoginRequest request = new LoginRequest();

        request.setEmail("ip@gmail.com");
        request.setPassword("password");

        User user = new User();

        user.setEmail("ip@gmail.com");

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(anyString()))
                .thenReturn("token");

        authService.login(
                request,
                "10.10.10.10",
                "Firefox");

        verify(securityEventService)
                .recordLoginEvent(
                        eq(user),
                        eq(LoginStatus.SUCCESS),
                        eq("10.10.10.10"),
                        eq("Firefox"));
    }

    @Test
    void login_shouldRecordCorrectUserAgent() {

        LoginRequest request = new LoginRequest();

        request.setEmail("agent@gmail.com");
        request.setPassword("password");

        User user = new User();

        user.setEmail("agent@gmail.com");

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(anyString()))
                .thenReturn("token");

        authService.login(
                request,
                "127.0.0.1",
                "Mozilla/5.0");

        verify(securityEventService)
                .recordLoginEvent(
                        eq(user),
                        eq(LoginStatus.SUCCESS),
                        eq("127.0.0.1"),
                        eq("Mozilla/5.0"));
    }
}