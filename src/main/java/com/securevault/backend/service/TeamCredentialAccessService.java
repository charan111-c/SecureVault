package com.securevault.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securevault.backend.dto.TeamCredentialAccessRequest;
import com.securevault.backend.dto.TeamCredentialAccessResponse;
import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.Team;
import com.securevault.backend.entity.TeamCredentialAccess;
import com.securevault.backend.entity.TeamCredentialAccess.PermissionLevel;
import com.securevault.backend.entity.TeamMember;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.CredentialRepository;
import com.securevault.backend.repository.TeamCredentialAccessRepository;
import com.securevault.backend.repository.TeamMemberRepository;
import com.securevault.backend.repository.TeamRepository;
import com.securevault.backend.repository.UserRepository;

@Service
public class TeamCredentialAccessService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamCredentialAccessRepository accessRepository;
    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;

    public TeamCredentialAccessService(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            TeamCredentialAccessRepository accessRepository,
            CredentialRepository credentialRepository,
            UserRepository userRepository
    ) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.accessRepository = accessRepository;
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // GRANT ACCESS
    // FULL_MANAGEMENT ONLY
    // =========================================================

    @Transactional
    public TeamCredentialAccessResponse grantAccess(
            Long teamId,
            Long credentialId,
            TeamCredentialAccessRequest request,
            String currentUserEmail
    ) {

        User currentUser = getUserByEmail(currentUserEmail);
        Team team = getTeam(teamId);
        Credential credential = getCredential(credentialId);

        TeamMember currentMember =
                getTeamMember(teamId, currentUser.getId());

        verifyCredentialOwner(credential, team, currentUser);

        /*
         * Team owner always has full management.
         *
         * Other users must have FULL_MANAGEMENT permission
         * on this credential to grant access.
         */
        requireFullManagement(
                teamId,
                credentialId,
                currentUser,
                currentMember
        );

        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }

        User targetUser =
                userRepository.findById(request.getUserId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Target user not found"
                                )
                        );

        // Target must be a team member
        if (!teamMemberRepository.existsByTeamIdAndUserId(
                teamId,
                targetUser.getId()
        )) {

            throw new IllegalArgumentException(
                    "User is not a member of this team"
            );
        }

        // Do not create duplicate access
        if (accessRepository
                .existsByTeamIdAndCredentialIdAndUserId(
                        teamId,
                        credentialId,
                        targetUser.getId()
                )) {

            throw new IllegalArgumentException(
                    "User already has access to this credential"
            );
        }

        PermissionLevel permission =
                request.getPermissionLevel();

        if (permission == null) {
            permission = PermissionLevel.VIEW_ONLY;
        }

        TeamCredentialAccess access =
                new TeamCredentialAccess();

        access.setTeam(team);
        access.setCredential(credential);
        access.setUser(targetUser);
        access.setPermissionLevel(permission);
        access.setExpirationDate(
                request.getExpirationDate()
        );

        TeamCredentialAccess saved =
                accessRepository.save(access);

        return toResponse(saved);
    }

    // =========================================================
    // GET MY TEAM CREDENTIALS
    // =========================================================

    @Transactional(readOnly = true)
    public List<TeamCredentialAccessResponse> getMyTeamCredentials(
            String email
    ) {

        User user = getUserByEmail(email);

        return accessRepository
                .findByUserId(user.getId())
                .stream()
                .filter(this::isNotExpired)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET ONE TEAM CREDENTIAL
    //
    // VIEW_ONLY = allowed
    // EDIT_ACCESS = allowed
    // FULL_MANAGEMENT = allowed
    // =========================================================

    @Transactional(readOnly = true)
    public TeamCredentialAccessResponse getCredentialForMember(
            Long teamId,
            Long credentialId,
            String email
    ) {

        User user = getUserByEmail(email);

        TeamMember member =
                getTeamMember(teamId, user.getId());

        /*
         * Owner can always view their credentials.
         */
        if (isOwner(member)) {

            Credential credential =
                    getCredential(credentialId);

            verifyCredentialOwner(
                    credential,
                    member.getTeam(),
                    user
            );

            return createOwnerResponse(
                    teamId,
                    credential,
                    user
            );
        }

        TeamCredentialAccess access =
                accessRepository
                        .findByTeamIdAndCredentialIdAndUserId(
                                teamId,
                                credentialId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "You do not have access to this credential"
                                )
                        );

        if (!isNotExpired(access)) {
            throw new AccessDeniedException(
                    "Credential access has expired"
            );
        }

        return toResponse(access);
    }

    // =========================================================
    // GET WHO HAS ACCESS
    //
    // FULL_MANAGEMENT ONLY
    // =========================================================

    @Transactional(readOnly = true)
    public List<TeamCredentialAccessResponse> getCredentialAccess(
            Long teamId,
            Long credentialId,
            String email
    ) {

        User currentUser = getUserByEmail(email);

        TeamMember currentMember =
                getTeamMember(
                        teamId,
                        currentUser.getId()
                );

        requireFullManagement(
                teamId,
                credentialId,
                currentUser,
                currentMember
        );

        return accessRepository
                .findByTeamIdAndCredentialId(
                        teamId,
                        credentialId
                )
                .stream()
                .filter(this::isNotExpired)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // UPDATE TEAM CREDENTIAL
    //
    // EDIT_ACCESS + FULL_MANAGEMENT
    // =========================================================

    @Transactional
    public TeamCredentialAccessResponse updateCredential(
            Long teamId,
            Long credentialId,
            String title,
            String username,
            String password,
            String email
    ) {

        User currentUser = getUserByEmail(email);

        TeamMember member =
                getTeamMember(
                        teamId,
                        currentUser.getId()
                );

        TeamCredentialAccess access =
                accessRepository
                        .findByTeamIdAndCredentialIdAndUserId(
                                teamId,
                                credentialId,
                                currentUser.getId()
                        )
                        .orElse(null);

        boolean owner = isOwner(member);

        boolean canEdit =
                owner ||
                (
                        access != null
                        && isNotExpired(access)
                        && canEdit(access)
                );

        if (!canEdit) {
            throw new AccessDeniedException(
                    "You do not have edit permission for this credential"
            );
        }

        Credential credential =
                getCredential(credentialId);

        /*
         * Extra ownership validation.
         */
        if (!owner &&
                !credential.getUser()
                        .getId()
                        .equals(currentUser.getId())) {

            /*
             * The credential itself can belong to another user
             * when it has been shared with the current user.
             * Therefore, do not reject an EDIT_ACCESS user here.
             */
        }

        // Update only supplied fields
        if (title != null) {
            credential.setTitle(title);
        }

        if (username != null) {
            credential.setUsername(username);
        }

        if (password != null) {
            credential.setPassword(password);
        }

        Credential saved =
                credentialRepository.save(credential);

        // Return access information
        if (access != null) {
            return toResponse(access);
        }

        /*
         * Owner may not have an access row.
         * Return owner-level response.
         */
        return createOwnerResponse(
                teamId,
                saved,
                currentUser
        );
    }

    // =========================================================
    // UPDATE PERMISSION
    //
    // FULL_MANAGEMENT ONLY
    // =========================================================

    @Transactional
    public TeamCredentialAccessResponse updatePermission(
            Long teamId,
            Long credentialId,
            Long userId,
            TeamCredentialAccessRequest request,
            String email
    ) {

        User currentUser = getUserByEmail(email);

        TeamMember currentMember =
                getTeamMember(
                        teamId,
                        currentUser.getId()
                );

        requireFullManagement(
                teamId,
                credentialId,
                currentUser,
                currentMember
        );

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request is required"
            );
        }

        PermissionLevel permission =
                request.getPermissionLevel();

        if (permission == null) {
            throw new IllegalArgumentException(
                    "Permission level is required"
            );
        }

        TeamCredentialAccess access =
                accessRepository
                        .findByTeamIdAndCredentialIdAndUserId(
                                teamId,
                                credentialId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Credential access not found"
                                )
                        );

        // Do not allow changing owner's permission
        Team team = getTeam(teamId);

        if (access.getUser()
                .getId()
                .equals(
                        team.getOwner().getId()
                )) {

            throw new IllegalArgumentException(
                    "Team owner always has full management access"
            );
        }

        access.setPermissionLevel(permission);

        access.setExpirationDate(
                request.getExpirationDate()
        );

        TeamCredentialAccess saved =
                accessRepository.save(access);

        return toResponse(saved);
    }

    // =========================================================
    // REMOVE ACCESS
    //
    // FULL_MANAGEMENT ONLY
    // =========================================================

    @Transactional
    public void removeAccess(
            Long teamId,
            Long credentialId,
            Long userId,
            String email
    ) {

        User currentUser = getUserByEmail(email);

        TeamMember currentMember =
                getTeamMember(
                        teamId,
                        currentUser.getId()
                );

        requireFullManagement(
                teamId,
                credentialId,
                currentUser,
                currentMember
        );

        TeamCredentialAccess access =
                accessRepository
                        .findByTeamIdAndCredentialIdAndUserId(
                                teamId,
                                credentialId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Credential access not found"
                                )
                        );

        // Cannot remove owner's access
        Team team = getTeam(teamId);

        if (access.getUser()
                .getId()
                .equals(
                        team.getOwner().getId()
                )) {

            throw new IllegalArgumentException(
                    "Team owner access cannot be removed"
            );
        }

        accessRepository.delete(access);
    }

    // =========================================================
    // REMOVE ALL ACCESS FOR MEMBER
    //
    // FULL_MANAGEMENT ONLY
    // =========================================================

    @Transactional
    public void removeMemberAccess(
            Long teamId,
            Long userId,
            String email
    ) {

        User currentUser = getUserByEmail(email);

        TeamMember currentMember =
                getTeamMember(
                        teamId,
                        currentUser.getId()
                );

        Team team = getTeam(teamId);

        /*
         * This operation affects potentially multiple
         * credentials, so the current user must be
         * the team owner.
         *
         * A FULL_MANAGEMENT permission on one credential
         * must not automatically give permission to remove
         * access from every other credential.
         */
        if (!isOwner(currentMember)) {

            throw new AccessDeniedException(
                    "Only the team owner can remove all credential access for a member"
            );
        }

        // Owner cannot have all access removed
        if (team.getOwner()
                .getId()
                .equals(userId)) {

            throw new IllegalArgumentException(
                    "Team owner's access cannot be removed"
            );
        }

        if (!teamMemberRepository
                .existsByTeamIdAndUserId(
                        teamId,
                        userId
                )) {

            throw new IllegalArgumentException(
                    "User is not a member of this team"
            );
        }

        List<TeamCredentialAccess> accesses =
                accessRepository
                        .findByTeamId(teamId)
                        .stream()
                        .filter(access ->
                                access.getUser()
                                        .getId()
                                        .equals(userId)
                        )
                        .collect(Collectors.toList());

        accessRepository.deleteAll(accesses);
    }

    // =========================================================
    // PERMISSION CHECK
    //
    // FULL_MANAGEMENT ONLY
    // =========================================================

    private void requireFullManagement(
            Long teamId,
            Long credentialId,
            User currentUser,
            TeamMember currentMember
    ) {

        /*
         * Team owner always has full management.
         */
        if (isOwner(currentMember)) {
            return;
        }

        TeamCredentialAccess access =
                accessRepository
                        .findByTeamIdAndCredentialIdAndUserId(
                                teamId,
                                credentialId,
                                currentUser.getId()
                        )
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "You do not have access to this credential"
                                )
                        );

        if (!isNotExpired(access)) {
            throw new AccessDeniedException(
                    "Credential access has expired"
            );
        }

        if (access.getPermissionLevel()
                != PermissionLevel.FULL_MANAGEMENT) {

            throw new AccessDeniedException(
                    "Full Management permission is required"
            );
        }
    }

    // =========================================================
    // HELPER - GET USER
    // =========================================================

    private User getUserByEmail(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );
    }

    // =========================================================
    // HELPER - GET TEAM
    // =========================================================

    private Team getTeam(Long teamId) {

        return teamRepository
                .findById(teamId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Team not found"
                        )
                );
    }

    // =========================================================
    // HELPER - GET CREDENTIAL
    // =========================================================

    private Credential getCredential(
            Long credentialId
    ) {

        return credentialRepository
                .findById(credentialId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Credential not found"
                        )
                );
    }

    // =========================================================
    // HELPER - TEAM MEMBER
    // =========================================================

    private TeamMember getTeamMember(
            Long teamId,
            Long userId
    ) {

        return teamMemberRepository
                .findByTeamIdAndUserId(
                        teamId,
                        userId
                )
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "You are not a member of this team"
                        )
                );
    }

    // =========================================================
    // HELPER - OWNER CHECK
    // =========================================================

    private boolean isOwner(
            TeamMember member
    ) {

        return member.getTeam()
                .getOwner()
                .getId()
                .equals(
                        member.getUser().getId()
                )
                || "OWNER".equalsIgnoreCase(
                        member.getRole()
                );
    }

    // =========================================================
    // HELPER - EDIT PERMISSION
    // =========================================================

    private boolean canEdit(
            TeamCredentialAccess access
    ) {

        if (access == null) {
            return false;
        }

        PermissionLevel permission =
                access.getPermissionLevel();

        return permission == PermissionLevel.EDIT_ACCESS
                || permission == PermissionLevel.FULL_MANAGEMENT;
    }

    // =========================================================
    // HELPER - EXPIRATION
    // =========================================================

    private boolean isNotExpired(
            TeamCredentialAccess access
    ) {

        if (access.getExpirationDate() == null) {
            return true;
        }

        return access.getExpirationDate()
                .isAfter(LocalDateTime.now());
    }

    // =========================================================
    // HELPER - VERIFY CREDENTIAL OWNER
    // =========================================================

    private void verifyCredentialOwner(
            Credential credential,
            Team team,
            User currentUser
    ) {

        /*
         * This check is intentionally used when the operation
         * requires the current user to own the credential.
         */
        if (!credential.getUser()
                .getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "You can only manage your own credentials"
            );
        }
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    private TeamCredentialAccessResponse toResponse(
            TeamCredentialAccess access
    ) {

        Credential credential =
                access.getCredential();

        return new TeamCredentialAccessResponse(
                access.getId(),
                access.getTeam().getId(),
                access.getTeam().getName(),
                credential.getId(),
                credential.getTitle(),
                credential.getUsername(),
                credential.getPassword(),
                access.getUser().getId(),
                access.getUser().getUsername(),
                access.getUser().getEmail(),
                access.getPermissionLevel(),
                access.getExpirationDate(),
                access.getCreatedAt(),
                access.getUpdatedAt()
        );
    }

    // =========================================================
    // OWNER RESPONSE
    // =========================================================

    private TeamCredentialAccessResponse createOwnerResponse(
            Long teamId,
            Credential credential,
            User owner
    ) {

        Team team =
                getTeam(teamId);

        return new TeamCredentialAccessResponse(
                null,
                team.getId(),
                team.getName(),
                credential.getId(),
                credential.getTitle(),
                credential.getUsername(),
                credential.getPassword(),
                owner.getId(),
                owner.getUsername(),
                owner.getEmail(),
                PermissionLevel.FULL_MANAGEMENT,
                null,
                null,
                null
        );
    }
}