package com.securevault.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.SharedCredential;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.CredentialRepository;
import com.securevault.backend.repository.SharedCredentialRepository;
import com.securevault.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SharingServiceTest {

    @Mock
    private SharedCredentialRepository sharedCredentialRepository;

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SharingService sharingService;

    private User owner;
    private User sharedUser;
    private User manager;
    private Credential credential;
    private SharedCredential sharedCredential;

    @BeforeEach
    void setUp() {

        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");
        owner.setEmail("owner@example.com");

        sharedUser = new User();
        sharedUser.setId(2L);
        sharedUser.setUsername("shareduser");
        sharedUser.setEmail("shared@example.com");

        manager = new User();
        manager.setId(3L);
        manager.setUsername("manager");
        manager.setEmail("manager@example.com");

        credential = new Credential();
        credential.setId(10L);
        credential.setTitle("GitHub");
        credential.setUsername("githubuser");
        credential.setPassword("encryptedPassword");
        credential.setUser(owner);

        sharedCredential = new SharedCredential();
        sharedCredential.setCredential(credential);
        sharedCredential.setOwner(owner);
        sharedCredential.setSharedUser(sharedUser);
        sharedCredential.setPermissionLevel(
                SharedCredential.PermissionLevel.VIEW_ONLY);
        sharedCredential.setSharingStatus(
                SharedCredential.SharingStatus.ACTIVE);
        sharedCredential.setExpirationDate(
                LocalDateTime.now().plusDays(5));
    }

    // ==========================================
    // 1. OWNER CAN SHARE
    // ==========================================

    @Test
    void shareCredential_owner_success() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(userRepository.findByEmail("shared@example.com"))
                .thenReturn(Optional.of(sharedUser));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 2L))
                .thenReturn(Optional.empty());

        when(sharedCredentialRepository
                .save(any(SharedCredential.class)))
                .thenReturn(sharedCredential);

        SharedCredential result =
                sharingService.shareCredential(
                        10L,
                        1L,
                        "shared@example.com",
                        SharedCredential.PermissionLevel.VIEW_ONLY,
                        LocalDateTime.now().plusDays(5));

        assertNotNull(result);

        verify(sharedCredentialRepository)
                .save(any(SharedCredential.class));
    }

    // ==========================================
    // 2. NON-OWNER CANNOT SHARE
    // ==========================================

    @Test
    void shareCredential_nonOwner_shouldFail() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        assertThrows(
                ResponseStatusException.class,
                () -> sharingService.shareCredential(
                        10L,
                        99L,
                        "shared@example.com",
                        SharedCredential.PermissionLevel.VIEW_ONLY,
                        LocalDateTime.now().plusDays(5)));

        verify(sharedCredentialRepository, never())
                .save(any(SharedCredential.class));
    }

    // ==========================================
    // 3. CREDENTIAL NOT FOUND
    // ==========================================

    @Test
    void shareCredential_credentialNotFound_shouldFail() {

        when(credentialRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> sharingService.shareCredential(
                        999L,
                        1L,
                        "shared@example.com",
                        SharedCredential.PermissionLevel.VIEW_ONLY,
                        null));
    }

    // ==========================================
    // 4. EMPTY EMAIL
    // ==========================================

    @Test
    void shareCredential_emptyEmail_shouldFail() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        assertThrows(
                ResponseStatusException.class,
                () -> sharingService.shareCredential(
                        10L,
                        1L,
                        "",
                        SharedCredential.PermissionLevel.VIEW_ONLY,
                        null));

        verify(userRepository, never())
                .findByEmail(anyString());
    }

    // ==========================================
    // 5. NULL PERMISSION
    // ==========================================

    @Test
    void shareCredential_nullPermission_shouldFail() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        assertThrows(
                ResponseStatusException.class,
                () -> sharingService.shareCredential(
                        10L,
                        1L,
                        "shared@example.com",
                        null,
                        null));
    }

    // ==========================================
    // 6. EXPIRED DATE
    // ==========================================

    @Test
    void shareCredential_expiredDate_shouldFail() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        LocalDateTime past =
                LocalDateTime.now().minusDays(1);

        assertThrows(
                ResponseStatusException.class,
                () -> sharingService.shareCredential(
                        10L,
                        1L,
                        "shared@example.com",
                        SharedCredential.PermissionLevel.VIEW_ONLY,
                        past));
    }

    // ==========================================
    // 7. SHARED USER NOT FOUND
    // ==========================================

    @Test
    void shareCredential_sharedUserNotFound_shouldFail() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> sharingService.shareCredential(
                        10L,
                        1L,
                        "unknown@example.com",
                        SharedCredential.PermissionLevel.VIEW_ONLY,
                        null));
    }

    // ==========================================
    // 8. CANNOT SHARE WITH SELF
    // ==========================================

    @Test
    void shareCredential_selfShare_shouldFail() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));

        assertThrows(
                ResponseStatusException.class,
                () -> sharingService.shareCredential(
                        10L,
                        1L,
                        "owner@example.com",
                        SharedCredential.PermissionLevel.VIEW_ONLY,
                        null));
    }

    // ==========================================
    // 9. EXISTING SHARE IS UPDATED
    // ==========================================

    @Test
    void shareCredential_existingShare_shouldUpdate() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(userRepository.findByEmail("shared@example.com"))
                .thenReturn(Optional.of(sharedUser));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 2L))
                .thenReturn(Optional.of(sharedCredential));

        when(sharedCredentialRepository
                .save(sharedCredential))
                .thenReturn(sharedCredential);

        SharedCredential result =
                sharingService.shareCredential(
                        10L,
                        1L,
                        "shared@example.com",
                        SharedCredential.PermissionLevel.EDIT_ACCESS,
                        LocalDateTime.now().plusDays(10));

        assertNotNull(result);

        assertEquals(
                SharedCredential.PermissionLevel.EDIT_ACCESS,
                sharedCredential.getPermissionLevel());

        verify(sharedCredentialRepository)
                .save(sharedCredential);
    }

    // ==========================================
    // 10. GET SHARED WITH ME
    // ==========================================

    @Test
    void getSharedWithMe_success() {

        when(sharedCredentialRepository
                .findBySharedUserId(2L))
                .thenReturn(List.of(sharedCredential));

        List<SharedCredential> result =
                sharingService.getSharedWithMe(2L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(sharedCredential, result.get(0));
    }

    // ==========================================
    // 11. GET CREDENTIAL SHARES OWNER
    // ==========================================

    @Test
    void getCredentialShares_owner_success() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialId(10L))
                .thenReturn(List.of(sharedCredential));

        List<SharedCredential> result =
                sharingService.getCredentialShares(10L, 1L);

        assertEquals(1, result.size());

        verify(sharedCredentialRepository)
                .findByCredentialId(10L);
    }

    // ==========================================
    // 12. GET CREDENTIAL SHARES WITHOUT ACCESS
    // ==========================================

    @Test
    void getCredentialShares_noAccess_shouldFail() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> sharingService.getCredentialShares(
                        10L,
                        99L));
    }

    // ==========================================
    // 13. OWNER CAN VIEW CREDENTIAL
    // ==========================================

    @Test
    void getSharedCredential_owner_success() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        SharedCredential result =
                sharingService.getSharedCredential(10L, 1L);

        assertNotNull(result);

        assertEquals(
                SharedCredential.PermissionLevel.FULL_MANAGEMENT,
                result.getPermissionLevel());

        assertEquals(
                SharedCredential.SharingStatus.ACTIVE,
                result.getSharingStatus());
    }

    // ==========================================
    // 14. SHARED USER CAN VIEW
    // ==========================================

    @Test
    void getSharedCredential_sharedUser_success() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 2L))
                .thenReturn(Optional.of(sharedCredential));

        SharedCredential result =
                sharingService.getSharedCredential(10L, 2L);

        assertNotNull(result);
        assertSame(sharedCredential, result);
    }

    // ==========================================
    // 15. UNAUTHORIZED USER CANNOT VIEW
    // ==========================================

    @Test
    void getSharedCredential_unauthorized_shouldFail() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> sharingService.getSharedCredential(
                        10L,
                        99L));
    }

    // ==========================================
    // 16. OWNER CAN VIEW CREDENTIAL DATA
    // ==========================================

    @Test
    void getCredentialForViewing_owner_success() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        Credential result =
                sharingService.getCredentialForViewing(
                        10L,
                        1L);

        assertNotNull(result);
        assertSame(credential, result);
    }

    // ==========================================
    // 17. SHARED USER CAN VIEW CREDENTIAL DATA
    // ==========================================

    @Test
    void getCredentialForViewing_sharedUser_success() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 2L))
                .thenReturn(Optional.of(sharedCredential));

        Credential result =
                sharingService.getCredentialForViewing(
                        10L,
                        2L);

        assertSame(credential, result);
    }

    // ==========================================
    // 18. VIEW WITHOUT ACCESS SHOULD FAIL
    // ==========================================

    @Test
    void getCredentialForViewing_noAccess_shouldFail() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> sharingService.getCredentialForViewing(
                        10L,
                        99L));
    }

    // ==========================================
    // 19. OWNER CAN UPDATE
    // ==========================================

    @Test
    void updateSharedCredential_owner_success() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(credentialRepository.save(credential))
                .thenReturn(credential);

        Credential result =
                sharingService.updateSharedCredential(
                        10L,
                        1L,
                        "Updated GitHub",
                        "newuser",
                        "newpassword");

        assertSame(credential, result);

        assertEquals(
                "Updated GitHub",
                credential.getTitle());

        assertEquals(
                "newuser",
                credential.getUsername());

        assertEquals(
                "newpassword",
                credential.getPassword());

        verify(credentialRepository)
                .save(credential);
    }

    // ==========================================
    // 20. VIEW ONLY CANNOT UPDATE
    // ==========================================

    @Test
    void updateSharedCredential_viewOnly_shouldFail() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 2L))
                .thenReturn(Optional.of(sharedCredential));

        assertThrows(
                ResponseStatusException.class,
                () -> sharingService.updateSharedCredential(
                        10L,
                        2L,
                        "Updated",
                        "user",
                        "password"));

        verify(credentialRepository, never())
                .save(any(Credential.class));
    }

    // ==========================================
    // 21. EDIT ACCESS CAN UPDATE
    // ==========================================

    @Test
    void updateSharedCredential_editAccess_success() {

        sharedCredential.setPermissionLevel(
                SharedCredential.PermissionLevel.EDIT_ACCESS);

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 2L))
                .thenReturn(Optional.of(sharedCredential));

        when(credentialRepository.save(credential))
                .thenReturn(credential);

        Credential result =
                sharingService.updateSharedCredential(
                        10L,
                        2L,
                        "Updated",
                        "newuser",
                        "newpassword");

        assertSame(credential, result);

        verify(credentialRepository)
                .save(credential);
    }

    // ==========================================
    // 22. OWNER REMOVE ACCESS
    // ==========================================

    @Test
    void removeSharedAccess_owner_success() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 2L))
                .thenReturn(Optional.of(sharedCredential));

        sharingService.removeSharedAccess(
                10L,
                1L,
                2L);

        verify(sharedCredentialRepository)
                .delete(sharedCredential);
    }

    // ==========================================
    // 23. NON-OWNER CANNOT REMOVE
    // ==========================================

    @Test
    void removeSharedAccess_nonOwner_shouldFail() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        assertThrows(
                ResponseStatusException.class,
                () -> sharingService.removeSharedAccess(
                        10L,
                        99L,
                        2L));

        verify(sharedCredentialRepository, never())
                .delete(any(SharedCredential.class));
    }

    // ==========================================
    // 24. REMOVE BY SHARE ID
    // ==========================================

    @Test
    void removeSharedAccessByShareId_success() {

        when(sharedCredentialRepository.findById(100L))
                .thenReturn(Optional.of(sharedCredential));

        sharingService.removeSharedAccessByShareId(
                100L,
                1L);

        verify(sharedCredentialRepository)
                .delete(sharedCredential);
    }

    // ==========================================
    // 25. REMOVE BY SHARE ID UNAUTHORIZED
    // ==========================================

    @Test
    void removeSharedAccessByShareId_nonOwner_shouldFail() {

        when(sharedCredentialRepository.findById(100L))
                .thenReturn(Optional.of(sharedCredential));

        assertThrows(
                ResponseStatusException.class,
                () -> sharingService.removeSharedAccessByShareId(
                        100L,
                        99L));

        verify(sharedCredentialRepository, never())
                .delete(any(SharedCredential.class));
    }

    // ==========================================
    // 26. REMOVE EXPIRED ACCESS
    // ==========================================

    @Test
    void removeExpiredAccess_shouldRevokeExpiredShares() {

        SharedCredential expiredShare =
                new SharedCredential();

        expiredShare.setCredential(credential);
        expiredShare.setOwner(owner);
        expiredShare.setSharedUser(sharedUser);

        expiredShare.setPermissionLevel(
                SharedCredential.PermissionLevel.VIEW_ONLY);

        expiredShare.setSharingStatus(
                SharedCredential.SharingStatus.ACTIVE);

        expiredShare.setExpirationDate(
                LocalDateTime.now().minusDays(1));

        when(sharedCredentialRepository.findAll())
                .thenReturn(List.of(expiredShare));

        int result =
                sharingService.removeExpiredAccess();

        assertEquals(1, result);

        assertEquals(
                SharedCredential.SharingStatus.REVOKED,
                expiredShare.getSharingStatus());

        verify(sharedCredentialRepository)
                .save(expiredShare);
    }

    // ==========================================
    // 27. NO EXPIRED ACCESS
    // ==========================================

    @Test
    void removeExpiredAccess_noExpiredShares() {

        sharedCredential.setExpirationDate(
                LocalDateTime.now().plusDays(10));

        when(sharedCredentialRepository.findAll())
                .thenReturn(List.of(sharedCredential));

        int result =
                sharingService.removeExpiredAccess();

        assertEquals(0, result);

        verify(sharedCredentialRepository, never())
                .save(any(SharedCredential.class));
    }
}