package com.securevault.backend.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.securevault.backend.dto.CredentialRequest;
import com.securevault.backend.dto.CredentialResponse;
import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.CredentialRepository;
import com.securevault.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private CredentialService credentialService;

    private User user;
    private Credential credential;
    private CredentialRequest request;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        request = new CredentialRequest();
        request.setTitle("GitHub");
        request.setUsername("testuser");
        request.setPassword("password123");

        credential = new Credential();
        credential.setId(1L);
        credential.setTitle("GitHub");
        credential.setUsername("testuser");
        credential.setPassword("encryptedPassword");
        credential.setUser(user);
    }

    // ==========================================
    // TEST 1 - ADD CREDENTIAL
    // ==========================================

    @Test
    void addCredential_success() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(encryptionService.encrypt("password123"))
                .thenReturn("encryptedPassword");

        when(credentialRepository.save(any(Credential.class)))
                .thenReturn(credential);

        when(encryptionService.decrypt("encryptedPassword"))
                .thenReturn("password123");

        CredentialResponse response =
                credentialService.addCredential(
                        request,
                        "test@example.com");

        assertNotNull(response);

        verify(userRepository)
                .findByEmail("test@example.com");

        verify(encryptionService)
                .encrypt("password123");

        verify(credentialRepository)
                .save(any(Credential.class));
    }

    // ==========================================
    // TEST 2 - USER NOT FOUND
    // ==========================================

    @Test
    void addCredential_userNotFound_shouldFail() {

        when(userRepository.findByEmail("wrong@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> credentialService.addCredential(
                        request,
                        "wrong@example.com"));

        verify(credentialRepository, never())
                .save(any(Credential.class));
    }

    // ==========================================
    // TEST 3 - GET CREDENTIALS
    // ==========================================

    @Test
    void getCredentials_success() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository.findByUser(user))
                .thenReturn(List.of(credential));

        when(encryptionService.decrypt("encryptedPassword"))
                .thenReturn("password123");

        List<CredentialResponse> result =
                credentialService.getCredentials(
                        "test@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(credentialRepository)
                .findByUser(user);
    }

    // ==========================================
    // TEST 4 - GET EMPTY CREDENTIALS
    // ==========================================

    @Test
    void getCredentials_emptyList() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository.findByUser(user))
                .thenReturn(List.of());

        List<CredentialResponse> result =
                credentialService.getCredentials(
                        "test@example.com");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==========================================
    // TEST 5 - GET USER NOT FOUND
    // ==========================================

    @Test
    void getCredentials_userNotFound_shouldFail() {

        when(userRepository.findByEmail("wrong@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> credentialService.getCredentials(
                        "wrong@example.com"));

        verify(credentialRepository, never())
                .findByUser(any());
    }

    // ==========================================
    // TEST 6 - UPDATE CREDENTIAL
    // ==========================================

    @Test
    void updateCredential_success() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository
                .findByIdAndUser(1L, user))
                .thenReturn(Optional.of(credential));

        when(encryptionService.encrypt("password123"))
                .thenReturn("encryptedPassword");

        when(credentialRepository.save(credential))
                .thenReturn(credential);

        when(encryptionService.decrypt("encryptedPassword"))
                .thenReturn("password123");

        CredentialResponse response =
                credentialService.updateCredential(
                        1L,
                        request,
                        "test@example.com");

        assertNotNull(response);

        assertEquals("GitHub", credential.getTitle());
        assertEquals("testuser", credential.getUsername());

        verify(credentialRepository)
                .findByIdAndUser(1L, user);

        verify(credentialRepository)
                .save(credential);
    }

    // ==========================================
    // TEST 7 - UPDATE CREDENTIAL NOT FOUND
    // ==========================================

    @Test
    void updateCredential_notFound_shouldFail() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository
                .findByIdAndUser(99L, user))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> credentialService.updateCredential(
                        99L,
                        request,
                        "test@example.com"));

        verify(credentialRepository, never())
                .save(any(Credential.class));
    }

    // ==========================================
    // TEST 8 - UPDATE UNAUTHORIZED USER
    // ==========================================

    @Test
    void updateCredential_unauthorized_shouldFail() {

        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("other@example.com");

        when(userRepository.findByEmail("other@example.com"))
                .thenReturn(Optional.of(anotherUser));

        when(credentialRepository
                .findByIdAndUser(1L, anotherUser))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> credentialService.updateCredential(
                        1L,
                        request,
                        "other@example.com"));

        verify(credentialRepository, never())
                .save(any(Credential.class));
    }

    // ==========================================
    // TEST 9 - DELETE CREDENTIAL
    // ==========================================

    @Test
    void deleteCredential_success() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository
                .findByIdAndUser(1L, user))
                .thenReturn(Optional.of(credential));

        credentialService.deleteCredential(
                1L,
                "test@example.com");

        verify(credentialRepository)
                .delete(credential);
    }

    // ==========================================
    // TEST 10 - DELETE NOT FOUND
    // ==========================================

    @Test
    void deleteCredential_notFound_shouldFail() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository
                .findByIdAndUser(99L, user))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> credentialService.deleteCredential(
                        99L,
                        "test@example.com"));

        verify(credentialRepository, never())
                .delete(any(Credential.class));
    }

    // ==========================================
    // TEST 11 - DELETE UNAUTHORIZED USER
    // ==========================================

    @Test
    void deleteCredential_unauthorized_shouldFail() {

        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("other@example.com");

        when(userRepository.findByEmail("other@example.com"))
                .thenReturn(Optional.of(anotherUser));

        when(credentialRepository
                .findByIdAndUser(1L, anotherUser))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> credentialService.deleteCredential(
                        1L,
                        "other@example.com"));

        verify(credentialRepository, never())
                .delete(any(Credential.class));
    }

    // ==========================================
    // TEST 12 - ENCRYPTION USED
    // ==========================================

    @Test
    void addCredential_shouldEncryptPassword() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(encryptionService.encrypt("password123"))
                .thenReturn("encryptedPassword");

        when(credentialRepository.save(any(Credential.class)))
                .thenReturn(credential);

        when(encryptionService.decrypt("encryptedPassword"))
                .thenReturn("password123");

        credentialService.addCredential(
                request,
                "test@example.com");

        verify(encryptionService)
                .encrypt("password123");
    }

    // ==========================================
    // TEST 13 - PASSWORD DECRYPTION
    // ==========================================

    @Test
    void getCredentials_shouldDecryptPassword() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository.findByUser(user))
                .thenReturn(List.of(credential));

        when(encryptionService.decrypt("encryptedPassword"))
                .thenReturn("password123");

        credentialService.getCredentials(
                "test@example.com");

        verify(encryptionService)
                .decrypt("encryptedPassword");
    }

    // ==========================================
    // TEST 14 - DELETE USER NOT FOUND
    // ==========================================

    @Test
    void deleteCredential_userNotFound_shouldFail() {

        when(userRepository.findByEmail("wrong@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> credentialService.deleteCredential(
                        1L,
                        "wrong@example.com"));

        verify(credentialRepository, never())
                .delete(any(Credential.class));
    }

    // ==========================================
    // TEST 15 - UPDATE ENCRYPTION
    // ==========================================

    @Test
    void updateCredential_shouldEncryptNewPassword() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository
                .findByIdAndUser(1L, user))
                .thenReturn(Optional.of(credential));

        when(encryptionService.encrypt("password123"))
                .thenReturn("encryptedPassword");

        when(credentialRepository.save(credential))
                .thenReturn(credential);

        when(encryptionService.decrypt("encryptedPassword"))
                .thenReturn("password123");

        credentialService.updateCredential(
                1L,
                request,
                "test@example.com");

        verify(encryptionService)
                .encrypt("password123");

        verify(credentialRepository)
                .save(credential);
    }
}