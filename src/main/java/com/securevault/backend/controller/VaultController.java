package com.securevault.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.securevault.backend.dto.CredentialRequest;
import com.securevault.backend.dto.CredentialResponse;
import com.securevault.backend.service.CredentialService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vault")
public class VaultController {

    private final CredentialService credentialService;

    public VaultController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    // =========================================================
    // ADD CREDENTIAL
    // =========================================================

    @PostMapping("/credentials")
    public ResponseEntity<CredentialResponse> addCredential(
            @Valid @RequestBody CredentialRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        CredentialResponse response =
                credentialService.addCredential(request, email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================================================
    // GET ALL CREDENTIALS
    // =========================================================

    @GetMapping("/credentials")
    public ResponseEntity<List<CredentialResponse>> getCredentials(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                credentialService.getCredentials(email)
        );
    }

    // =========================================================
    // SEARCH
    // =========================================================

    @GetMapping("/credentials/search")
    public ResponseEntity<List<CredentialResponse>> searchCredentials(
            @RequestParam String query,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                credentialService.searchCredentials(query, email)
        );
    }

    // =========================================================
    // FILTER
    // =========================================================

    @GetMapping("/credentials/filter")
    public ResponseEntity<List<CredentialResponse>> filterCredentials(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String credentialType,
            @RequestParam(required = false) Boolean favorite,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                credentialService.filterCredentials(
                        query,
                        category,
                        credentialType,
                        favorite,
                        email
                )
        );
    }

    // =========================================================
    // MARK / UNMARK FAVORITE
    // =========================================================

    @PutMapping("/credentials/{id}/favorite")
    public ResponseEntity<CredentialResponse> setFavorite(
            @PathVariable Long id,
            @RequestParam boolean favorite,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                credentialService.setFavorite(
                        id,
                        favorite,
                        email
                )
        );
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping("/credentials/{id}")
    public ResponseEntity<CredentialResponse> updateCredential(
            @PathVariable Long id,
            @Valid @RequestBody CredentialRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                credentialService.updateCredential(
                        id,
                        request,
                        email
                )
        );
    }

    // =========================================================
    // DELETE
    // =========================================================

    @DeleteMapping("/credentials/{id}")
    public ResponseEntity<Void> deleteCredential(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        credentialService.deleteCredential(id, email);

        return ResponseEntity.noContent().build();
    }
}