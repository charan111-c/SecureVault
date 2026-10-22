package com.securevault.backend.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securevault.backend.entity.Team;
import com.securevault.backend.entity.TeamMember;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.TeamMemberRepository;
import com.securevault.backend.repository.TeamRepository;
import com.securevault.backend.repository.UserRepository;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public TeamService(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            UserRepository userRepository
    ) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // CREATE TEAM
    // =========================================================

    @Transactional
    public Team createTeam(
            String ownerEmail,
            String teamName
    ) {

        User owner = getUserByEmail(ownerEmail);

        if (teamName == null || teamName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Team name is required"
            );
        }

        String name = teamName.trim();

        if (teamRepository.existsByNameAndOwnerId(
                name,
                owner.getId()
        )) {
            throw new IllegalArgumentException(
                    "You already have a team with this name"
            );
        }

        Team team = new Team();

        team.setName(name);
        team.setOwner(owner);

        Team savedTeam =
                teamRepository.save(team);

        // Owner automatically becomes OWNER member.
        TeamMember ownerMember =
                new TeamMember();

        ownerMember.setTeam(savedTeam);
        ownerMember.setUser(owner);
        ownerMember.setRole("OWNER");

        teamMemberRepository.save(ownerMember);

        return savedTeam;
    }

    // =========================================================
    // GET MY TEAMS
    // =========================================================

    public List<Team> getMyTeams(
            String email
    ) {

        User user = getUserByEmail(email);

        return teamRepository.findByOwnerId(
                user.getId()
        );
    }

    // =========================================================
    // GET TEAM
    // =========================================================

    public Team getTeamForOwner(
            Long teamId,
            String email
    ) {

        User user =
                getUserByEmail(email);

        return teamRepository
                .findByIdAndOwnerId(
                        teamId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "You do not have permission to access this team"
                        )
                );
    }

    // =========================================================
    // ADD MEMBER
    // OWNER / ADMIN
    // =========================================================

    @Transactional
    public void addMember(
            Long teamId,
            String requesterEmail,
            String memberEmail,
            String role
    ) {

        User requester =
                getUserByEmail(requesterEmail);

        Team team =
                getTeamForManagement(
                        teamId,
                        requester
                );

        if (memberEmail == null ||
                memberEmail.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Member email is required"
            );
        }

        User member =
                getUserByEmail(
                        memberEmail.trim()
                );

        if (member.getId().equals(
                team.getOwner().getId()
        )) {

            throw new IllegalArgumentException(
                    "Team owner is already a member"
            );
        }

        if (teamMemberRepository
                .existsByTeamIdAndUserId(
                        teamId,
                        member.getId()
                )) {

            throw new IllegalArgumentException(
                    "User is already a member of this team"
            );
        }

        String normalizedRole =
                normalizeRole(role);

        /*
         * ADMIN cannot create another OWNER.
         *
         * normalizeRole() already prevents OWNER,
         * but this keeps the rule explicit.
         */
        if ("OWNER".equals(normalizedRole)) {

            throw new IllegalArgumentException(
                    "OWNER role cannot be assigned"
            );
        }

        TeamMember teamMember =
                new TeamMember();

        teamMember.setTeam(team);
        teamMember.setUser(member);
        teamMember.setRole(normalizedRole);

        teamMemberRepository.save(
                teamMember
        );
    }

    // =========================================================
    // GET TEAM MEMBERS
    // =========================================================

    @Transactional(readOnly = true)
    public List<TeamMember> getTeamMembers(
            Long teamId,
            String requesterEmail
    ) {

        User requester =
                getUserByEmail(requesterEmail);

        getTeamMembership(
                teamId,
                requester
        );

        return teamMemberRepository
                .findByTeamId(teamId);
    }

    // =========================================================
    // UPDATE MEMBER ROLE
    // =========================================================

    @Transactional
    public void updateMemberRole(
            Long teamId,
            Long memberUserId,
            String newRole,
            String requesterEmail
    ) {

        User requester =
                getUserByEmail(requesterEmail);

        Team team =
                getTeamForManagement(
                        teamId,
                        requester
                );

        TeamMember target =
                teamMemberRepository
                        .findByTeamIdAndUserId(
                                teamId,
                                memberUserId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User is not a member of this team"
                                )
                        );

        // Owner role cannot be changed.
        if (target.getUser()
                .getId()
                .equals(team.getOwner().getId())) {

            throw new IllegalArgumentException(
                    "Team owner's role cannot be changed"
            );
        }

        String normalizedRole =
                normalizeRole(newRole);

        /*
         * Only OWNER can assign ADMIN.
         */
        if ("ADMIN".equals(normalizedRole) &&
                !isOwner(requester, team)) {

            throw new AccessDeniedException(
                    "Only the team owner can assign ADMIN role"
            );
        }

        target.setRole(
                normalizedRole
        );

        teamMemberRepository.save(
                target
        );
    }

    // =========================================================
    // REMOVE MEMBER
    // OWNER / ADMIN
    // =========================================================

    @Transactional
    public void removeMember(
            Long teamId,
            String requesterEmail,
            Long memberUserId
    ) {

        User requester =
                getUserByEmail(requesterEmail);

        Team team =
                getTeamForManagement(
                        teamId,
                        requester
                );

        if (team.getOwner()
                .getId()
                .equals(memberUserId)) {

            throw new IllegalArgumentException(
                    "Team owner cannot be removed"
            );
        }

        TeamMember member =
                teamMemberRepository
                        .findByTeamIdAndUserId(
                                teamId,
                                memberUserId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User is not a member of this team"
                                )
                        );

        /*
         * ADMIN cannot remove another ADMIN.
         */
        if ("ADMIN".equals(member.getRole())
                && !isOwner(requester, team)) {

            throw new AccessDeniedException(
                    "Only the team owner can remove an ADMIN"
            );
        }

        teamMemberRepository.delete(
                member
        );
    }

    // =========================================================
    // CHECK TEAM ACCESS
    // =========================================================

    public boolean isTeamMember(
            Long teamId,
            Long userId
    ) {

        return teamMemberRepository
                .existsByTeamIdAndUserId(
                        teamId,
                        userId
                );
    }

    // =========================================================
    // GET ROLE
    // =========================================================

    public String getMemberRole(
            Long teamId,
            Long userId
    ) {

        TeamMember member =
                teamMemberRepository
                        .findByTeamIdAndUserId(
                                teamId,
                                userId
                        )
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "You are not a member of this team"
                                )
                        );

        return member.getRole();
    }

    // =========================================================
    // CAN MANAGE TEAM
    // OWNER / ADMIN
    // =========================================================

    public boolean canManageTeam(
            Long teamId,
            Long userId
    ) {

        String role =
                getMemberRole(
                        teamId,
                        userId
                );

        return "OWNER".equals(role)
                || "ADMIN".equals(role);
    }

    // =========================================================
    // INTERNAL MANAGEMENT CHECK
    // =========================================================

    private Team getTeamForManagement(
            Long teamId,
            User requester
    ) {

        Team team =
                teamRepository
                        .findById(teamId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Team not found"
                                )
                        );

        TeamMember member =
                teamMemberRepository
                        .findByTeamIdAndUserId(
                                teamId,
                                requester.getId()
                        )
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "You are not a member of this team"
                                )
                        );

        String role =
                member.getRole();

        if (!"OWNER".equals(role)
                && !"ADMIN".equals(role)) {

            throw new AccessDeniedException(
                    "Only OWNER or ADMIN can manage this team"
            );
        }

        return team;
    }

    // =========================================================
    // GET MEMBERSHIP
    // =========================================================

    private TeamMember getTeamMembership(
            Long teamId,
            User user
    ) {

        return teamMemberRepository
                .findByTeamIdAndUserId(
                        teamId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "You are not a member of this team"
                        )
                );
    }

    // =========================================================
    // OWNER CHECK
    // =========================================================

    private boolean isOwner(
            User user,
            Team team
    ) {

        return team.getOwner()
                .getId()
                .equals(user.getId());
    }

    // =========================================================
    // ROLE VALIDATION
    // =========================================================

    private String normalizeRole(
            String role
    ) {

        if (role == null ||
                role.trim().isEmpty()) {

            return "MEMBER";
        }

        String normalized =
                role.trim().toUpperCase();

        if (!normalized.equals("MEMBER")
                && !normalized.equals("ADMIN")) {

            throw new IllegalArgumentException(
                    "Invalid team role. Use MEMBER or ADMIN"
            );
        }

        return normalized;
    }

    // =========================================================
    // USER
    // =========================================================

    private User getUserByEmail(
            String email
    ) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );
    }
}