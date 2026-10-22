package com.securevault.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securevault.backend.dto.TeamCredentialAccessRequest;
import com.securevault.backend.dto.TeamCredentialAccessResponse;
import com.securevault.backend.service.TeamCredentialAccessService;

@RestController
@RequestMapping("/api/team-access")
public class TeamCredentialAccessController {

    private final TeamCredentialAccessService accessService;

    public TeamCredentialAccessController(
            TeamCredentialAccessService accessService) {
        this.accessService = accessService;
    }

    // =========================================================
    // 1. GRANT CREDENTIAL ACCESS
    // =========================================================

    @PostMapping("/teams/{teamId}/credentials/{credentialId}")
    public ResponseEntity<?> grantAccess(
            @PathVariable Long teamId,
            @PathVariable Long credentialId,
            @RequestBody TeamCredentialAccessRequest request,
            Authentication authentication) {

        TeamCredentialAccessResponse response =
                accessService.grantAccess(
                        teamId,
                        credentialId,
                        request,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // 2. GET MY TEAM CREDENTIALS
    // =========================================================

    @GetMapping("/my-credentials")
    public ResponseEntity<?> getMyTeamCredentials(
            Authentication authentication) {

        List<TeamCredentialAccessResponse> response =
                accessService.getMyTeamCredentials(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // 3. GET ONE TEAM CREDENTIAL
    // =========================================================

    @GetMapping("/teams/{teamId}/credentials/{credentialId}")
    public ResponseEntity<?> getCredential(
            @PathVariable Long teamId,
            @PathVariable Long credentialId,
            Authentication authentication) {

        TeamCredentialAccessResponse response =
                accessService.getCredentialForMember(
                        teamId,
                        credentialId,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // 4. GET USERS WHO HAVE ACCESS
    // =========================================================

    @GetMapping(
            "/teams/{teamId}/credentials/{credentialId}/access"
    )
    public ResponseEntity<?> getCredentialAccess(
            @PathVariable Long teamId,
            @PathVariable Long credentialId,
            Authentication authentication) {

        List<TeamCredentialAccessResponse> response =
                accessService.getCredentialAccess(
                        teamId,
                        credentialId,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // 5. UPDATE TEAM CREDENTIAL
    // =========================================================

    @PutMapping(
            "/teams/{teamId}/credentials/{credentialId}"
    )
    public ResponseEntity<?> updateCredential(
            @PathVariable Long teamId,
            @PathVariable Long credentialId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        TeamCredentialAccessResponse response =
                accessService.updateCredential(
                        teamId,
                        credentialId,
                        request.get("title"),
                        request.get("username"),
                        request.get("password"),
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // 6. REMOVE ACCESS FROM ONE CREDENTIAL
    // =========================================================

    @DeleteMapping(
            "/teams/{teamId}/credentials/{credentialId}/users/{userId}"
    )
    public ResponseEntity<?> removeAccess(
            @PathVariable Long teamId,
            @PathVariable Long credentialId,
            @PathVariable Long userId,
            Authentication authentication) {

        accessService.removeAccess(
                teamId,
                credentialId,
                userId,
                authentication.getName()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Credential access removed successfully"
                )
        );
    }

    // =========================================================
    // 7. REMOVE ALL ACCESS FOR A TEAM MEMBER
    // =========================================================

    @DeleteMapping(
            "/teams/{teamId}/members/{userId}/access"
    )
    public ResponseEntity<?> removeMemberAccess(
            @PathVariable Long teamId,
            @PathVariable Long userId,
            Authentication authentication) {

        accessService.removeMemberAccess(
                teamId,
                userId,
                authentication.getName()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "All team credential access removed successfully"
                )
        );
    }
}