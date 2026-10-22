package com.securevault.backend.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.securevault.backend.entity.PasswordHistory;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.PasswordHistoryRepository;

@ExtendWith(MockitoExtension.class)
class PasswordHistoryServiceTest {

    @Mock
    private PasswordHistoryRepository passwordHistoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordHistoryService passwordHistoryService;

    private User user;
    private PasswordHistory history;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        history = new PasswordHistory();
        history.setId(1L);
        history.setUser(user);
        history.setPasswordHash("encodedPassword");
    }

    // --------------------------------------------------
    // TEST 1
    // --------------------------------------------------

    @Test
    void savePasswordHistory_success() {

        when(passwordHistoryRepository.save(any(PasswordHistory.class)))
                .thenReturn(history);

        passwordHistoryService.savePasswordHistory(
                user,
                "encodedPassword");

        verify(passwordHistoryRepository, times(1))
                .save(any(PasswordHistory.class));
    }

    // --------------------------------------------------
    // TEST 2
    // --------------------------------------------------

    @Test
    void savePasswordHistory_nullUser_shouldFail() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> passwordHistoryService
                                .savePasswordHistory(
                                        null,
                                        "encodedPassword"));

        assertEquals(
                "User cannot be null",
                exception.getMessage());

        verify(passwordHistoryRepository, never())
                .save(any());
    }

    // --------------------------------------------------
    // TEST 3
    // --------------------------------------------------

    @Test
    void savePasswordHistory_emptyPassword_shouldFail() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> passwordHistoryService
                                .savePasswordHistory(
                                        user,
                                        ""));

        assertEquals(
                "Password hash cannot be empty",
                exception.getMessage());

        verify(passwordHistoryRepository, never())
                .save(any());
    }

    // --------------------------------------------------
    // TEST 4
    // --------------------------------------------------

    @Test
    void savePasswordHistory_nullPassword_shouldFail() {

        assertThrows(
                IllegalArgumentException.class,
                () -> passwordHistoryService
                        .savePasswordHistory(
                                user,
                                null));

        verify(passwordHistoryRepository, never())
                .save(any());
    }

    // --------------------------------------------------
    // TEST 5
    // --------------------------------------------------

    @Test
    void passwordPreviouslyUsed_shouldReturnTrue() {

        when(passwordHistoryRepository
                .findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(history));

        when(passwordEncoder.matches(
                "oldPassword",
                "encodedPassword"))
                .thenReturn(true);

        boolean result =
                passwordHistoryService
                        .isPasswordPreviouslyUsed(
                                user,
                                "oldPassword");

        assertTrue(result);

        verify(passwordEncoder)
                .matches(
                        "oldPassword",
                        "encodedPassword");
    }

    // --------------------------------------------------
    // TEST 6
    // --------------------------------------------------

    @Test
    void passwordPreviouslyUsed_shouldReturnFalse() {

        when(passwordHistoryRepository
                .findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(history));

        when(passwordEncoder.matches(
                "newPassword",
                "encodedPassword"))
                .thenReturn(false);

        boolean result =
                passwordHistoryService
                        .isPasswordPreviouslyUsed(
                                user,
                                "newPassword");

        assertFalse(result);
    }

    // --------------------------------------------------
    // TEST 7
    // --------------------------------------------------

    @Test
    void passwordPreviouslyUsed_emptyHistory_shouldReturnFalse() {

        when(passwordHistoryRepository
                .findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of());

        boolean result =
                passwordHistoryService
                        .isPasswordPreviouslyUsed(
                                user,
                                "newPassword");

        assertFalse(result);

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
    }

    // --------------------------------------------------
    // TEST 8
    // --------------------------------------------------

    @Test
    void passwordPreviouslyUsed_nullUser_shouldFail() {

        assertThrows(
                IllegalArgumentException.class,
                () -> passwordHistoryService
                        .isPasswordPreviouslyUsed(
                                null,
                                "password"));

        verify(passwordHistoryRepository, never())
                .findByUserOrderByCreatedAtDesc(any());
    }

    // --------------------------------------------------
    // TEST 9
    // --------------------------------------------------

    @Test
    void passwordPreviouslyUsed_emptyPassword_shouldFail() {

        assertThrows(
                IllegalArgumentException.class,
                () -> passwordHistoryService
                        .isPasswordPreviouslyUsed(
                                user,
                                ""));

        verify(passwordHistoryRepository, never())
                .findByUserOrderByCreatedAtDesc(any());
    }

    // --------------------------------------------------
    // TEST 10
    // --------------------------------------------------

    @Test
    void validateNewPassword_newPassword_shouldPass() {

        when(passwordHistoryRepository
                .findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(history));

        when(passwordEncoder.matches(
                "newPassword",
                "encodedPassword"))
                .thenReturn(false);

        assertDoesNotThrow(
                () -> passwordHistoryService
                        .validateNewPassword(
                                user,
                                "newPassword"));
    }

    // --------------------------------------------------
    // TEST 11
    // --------------------------------------------------

    @Test
    void validateNewPassword_oldPassword_shouldFail() {

        when(passwordHistoryRepository
                .findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(history));

        when(passwordEncoder.matches(
                "oldPassword",
                "encodedPassword"))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> passwordHistoryService
                                .validateNewPassword(
                                        user,
                                        "oldPassword"));

        assertEquals(
                "Password was previously used. " +
                "Please choose a different password.",
                exception.getMessage());
    }

    // --------------------------------------------------
    // TEST 12
    // --------------------------------------------------

    @Test
    void getPasswordHistory_shouldReturnHistory() {

        List<PasswordHistory> histories =
                List.of(history);

        when(passwordHistoryRepository
                .findByUserOrderByCreatedAtDesc(user))
                .thenReturn(histories);

        List<PasswordHistory> result =
                passwordHistoryService
                        .getPasswordHistory(user);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(history, result.get(0));
    }

    // --------------------------------------------------
    // TEST 13
    // --------------------------------------------------

    @Test
    void getPasswordHistory_emptyHistory_shouldReturnEmptyList() {

        when(passwordHistoryRepository
                .findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of());

        List<PasswordHistory> result =
                passwordHistoryService
                        .getPasswordHistory(user);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // --------------------------------------------------
    // TEST 14
    // --------------------------------------------------

    @Test
    void getPasswordHistory_nullUser_shouldFail() {

        assertThrows(
                IllegalArgumentException.class,
                () -> passwordHistoryService
                        .getPasswordHistory(null));

        verify(passwordHistoryRepository, never())
                .findByUserOrderByCreatedAtDesc(any());
    }

    // --------------------------------------------------
    // TEST 15
    // --------------------------------------------------

    @Test
    void passwordPreviouslyUsed_shouldCheckMultiplePasswords() {

        PasswordHistory history2 =
                new PasswordHistory();

        history2.setId(2L);
        history2.setUser(user);
        history2.setPasswordHash("encodedPassword2");

        when(passwordHistoryRepository
                .findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(history, history2));

        when(passwordEncoder.matches(
                "password",
                "encodedPassword"))
                .thenReturn(false);

        when(passwordEncoder.matches(
                "password",
                "encodedPassword2"))
                .thenReturn(true);

        boolean result =
                passwordHistoryService
                        .isPasswordPreviouslyUsed(
                                user,
                                "password");

        assertTrue(result);
    }
}