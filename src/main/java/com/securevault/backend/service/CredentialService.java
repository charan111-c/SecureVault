package com.securevault.backend.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.securevault.backend.dto.CredentialRequest;
import com.securevault.backend.dto.CredentialResponse;
import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.CredentialRepository;
import com.securevault.backend.repository.UserRepository;

@Service
public class CredentialService {

    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    public CredentialService(
            CredentialRepository credentialRepository,
            UserRepository userRepository,
            EncryptionService encryptionService) {

        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    // =========================================================
    // 1. ADD CREDENTIAL
    // =========================================================

    public CredentialResponse addCredential(
            CredentialRequest request,
            String email) {

        User user = getUserByEmail(email);

        validateRequest(request);

        Credential credential = new Credential();

        credential.setTitle(request.getTitle());
        credential.setUser(user);

        credential.setCategory(
                isBlank(request.getCategory())
                        ? "OTHER"
                        : request.getCategory().trim().toUpperCase()
        );

        credential.setCredentialType(
                isBlank(request.getCredentialType())
                        ? "LOGIN"
                        : request.getCredentialType().trim().toUpperCase()
        );

        credential.setFavorite(request.isFavorite());

        /*
         * SECURE NOTE
         */
        if ("SECURE_NOTE".equalsIgnoreCase(
                credential.getCredentialType())) {

            credential.setUsername(null);
            credential.setPassword(null);

            if (!isBlank(request.getSecureNote())) {

                credential.setSecureNote(
                        encryptionService.encrypt(
                                request.getSecureNote()
                        )
                );
            }

        } else {

            credential.setUsername(request.getUsername());

            if (!isBlank(request.getPassword())) {

                credential.setPassword(
                        encryptionService.encrypt(
                                request.getPassword()
                        )
                );
            }

            credential.setSecureNote(null);
        }

        Credential savedCredential =
                credentialRepository.save(credential);

        return toResponse(savedCredential);
    }

    // =========================================================
    // 2. GET ALL CREDENTIALS
    // =========================================================

    public List<CredentialResponse> getCredentials(
            String email) {

        User user = getUserByEmail(email);

        return credentialRepository
                .findByUser(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // 3. SEARCH CREDENTIALS
    // =========================================================

    public List<CredentialResponse> searchCredentials(
            String query,
            String email) {

        User user = getUserByEmail(email);

        String searchQuery =
                query == null
                        ? ""
                        : query.trim();

        if (searchQuery.isBlank()) {
            return getCredentials(email);
        }

        return credentialRepository
                .findByUserAndTitleContainingIgnoreCaseOrUserAndUsernameContainingIgnoreCase(
                        user,
                        searchQuery,
                        user,
                        searchQuery
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // 4. SEARCH + FILTER
    // =========================================================

    public List<CredentialResponse> filterCredentials(
            String query,
            String category,
            String credentialType,
            Boolean favorite,
            String email) {

        User user = getUserByEmail(email);

        String cleanQuery =
                isBlank(query)
                        ? null
                        : query.trim();

        String cleanCategory =
                isBlank(category)
                        ? null
                        : category.trim().toUpperCase();

        String cleanCredentialType =
                isBlank(credentialType)
                        ? null
                        : credentialType.trim().toUpperCase();

        /*
         * IMPORTANT:
         *
         * Search/filtering happens inside the database
         * through CredentialRepository.searchAndFilter().
         *
         * We do NOT load the complete vault and filter
         * using Java streams.
         */

        List<Credential> credentials =
                credentialRepository.searchAndFilter(
                        user,
                        cleanQuery,
                        cleanCategory,
                        cleanCredentialType,
                        favorite
                );

        return credentials
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // 5. MARK / UNMARK FAVORITE
    // =========================================================

    public CredentialResponse setFavorite(
            Long id,
            boolean favorite,
            String email) {

        User user = getUserByEmail(email);

        Credential credential =
                credentialRepository
                        .findByIdAndUser(id, user)
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "You do not have permission to access this credential"
                                )
                        );

        credential.setFavorite(favorite);

        Credential updated =
                credentialRepository.save(credential);

        return toResponse(updated);
    }

    // =========================================================
    // 6. UPDATE CREDENTIAL
    // =========================================================

    public CredentialResponse updateCredential(
            Long id,
            CredentialRequest request,
            String email) {

        User user = getUserByEmail(email);

        Credential credential =
                credentialRepository
                        .findByIdAndUser(id, user)
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "You do not have permission to access this credential"
                                )
                        );

        validateRequest(request);

        credential.setTitle(request.getTitle());

        String newType =
                isBlank(request.getCredentialType())
                        ? credential.getCredentialType()
                        : request.getCredentialType()
                                .trim()
                                .toUpperCase();

        credential.setCredentialType(newType);

        if (!isBlank(request.getCategory())) {

            credential.setCategory(
                    request.getCategory()
                            .trim()
                            .toUpperCase()
            );
        }

        credential.setFavorite(request.isFavorite());

        /*
         * SECURE NOTE UPDATE
         */
        if ("SECURE_NOTE".equalsIgnoreCase(newType)) {

            credential.setUsername(null);
            credential.setPassword(null);

            if (!isBlank(request.getSecureNote())) {

                credential.setSecureNote(
                        encryptionService.encrypt(
                                request.getSecureNote()
                        )
                );
            }

        } else {

            credential.setUsername(
                    request.getUsername()
            );

            /*
             * Only replace password when a new password
             * has actually been supplied.
             *
             * This prevents accidentally encrypting
             * an empty password during an update.
             */
            if (!isBlank(request.getPassword())) {

                credential.setPassword(
                        encryptionService.encrypt(
                                request.getPassword()
                        )
                );
            }

            credential.setSecureNote(null);
        }

        Credential updatedCredential =
                credentialRepository.save(credential);

        return toResponse(updatedCredential);
    }

    // =========================================================
    // 7. DELETE CREDENTIAL
    // =========================================================

    public void deleteCredential(
            Long id,
            String email) {

        User user = getUserByEmail(email);

        Credential credential =
                credentialRepository
                        .findByIdAndUser(id, user)
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "You do not have permission to access this credential"
                                )
                        );

        credentialRepository.delete(credential);
    }

    // =========================================================
    // 8. ENTITY -> RESPONSE
    // =========================================================

    private CredentialResponse toResponse(
            Credential credential) {

        String decryptedPassword = null;
        String decryptedSecureNote = null;

        /*
         * Decrypt password only when it exists.
         */
        if (!isBlank(credential.getPassword())) {

            decryptedPassword =
                    encryptionService.decrypt(
                            credential.getPassword()
                    );
        }

        /*
         * Decrypt secure note only when it exists.
         */
        if (!isBlank(credential.getSecureNote())) {

            decryptedSecureNote =
                    encryptionService.decrypt(
                            credential.getSecureNote()
                    );
        }

        return new CredentialResponse(
                credential.getId(),
                credential.getTitle(),
                credential.getUsername(),
                decryptedPassword,
                credential.getCategory(),
                credential.getCredentialType(),
                credential.isFavorite(),
                decryptedSecureNote
        );
    }

    // =========================================================
    // 9. VALIDATION
    // =========================================================

    private void validateRequest(
            CredentialRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Credential request cannot be null"
            );
        }

        if (isBlank(request.getTitle())) {
            throw new IllegalArgumentException(
                    "Title is required"
            );
        }

        String type =
                isBlank(request.getCredentialType())
                        ? "LOGIN"
                        : request.getCredentialType()
                                .trim()
                                .toUpperCase();

        /*
         * Secure Note validation
         */
        if ("SECURE_NOTE".equals(type)) {

            if (isBlank(request.getSecureNote())) {

                throw new IllegalArgumentException(
                        "Secure note cannot be empty"
                );
            }

            return;
        }

        /*
         * Normal credential validation
         */
        if (isBlank(request.getUsername())) {

            throw new IllegalArgumentException(
                    "Username is required"
            );
        }

        if (isBlank(request.getPassword())) {

            throw new IllegalArgumentException(
                    "Password is required"
            );
        }
    }

    // =========================================================
    // 10. UTILITY
    // =========================================================

    private boolean isBlank(String value) {

        return value == null ||
                value.trim().isEmpty();
    }

    // =========================================================
    // 11. FIND USER
    // =========================================================

    private User getUserByEmail(
            String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );
    }
}