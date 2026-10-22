package com.securevault.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securevault.backend.entity.Team;
import com.securevault.backend.entity.TeamMember;
import com.securevault.backend.service.TeamService;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    // =========================================================
    // CREATE TEAM
    // =========================================================

    @PostMapping
    public ResponseEntity<?> createTeam(
            @RequestBody Map<String, String> request,
            Authentication authentication
    ) {

        String teamName = request.get("name");

        Team team = teamService.createTeam(
                authentication.getName(),
                teamName
        );

        return ResponseEntity.ok(teamResponse(team));
    }

    // =========================================================
    // GET MY TEAMS
    // =========================================================

    @GetMapping
    public ResponseEntity<?> getMyTeams(
            Authentication authentication
    ) {

        List<Team> teams =
                teamService.getMyTeams(
                        authentication.getName()
                );

        List<Map<String, Object>> response =
                teams.stream()
                        .map(this::teamResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // GET TEAM MEMBERS
    // =========================================================

    @GetMapping("/{teamId}/members")
    public ResponseEntity<?> getTeamMembers(
            @PathVariable Long teamId,
            Authentication authentication
    ) {

        List<TeamMember> members =
                teamService.getTeamMembers(
                        teamId,
                        authentication.getName()
                );

        List<Map<String, Object>> response =
                members.stream()
                        .map(this::memberResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // ADD TEAM MEMBER
    // =========================================================

    @PostMapping("/{teamId}/members")
    public ResponseEntity<?> addMember(
            @PathVariable Long teamId,
            @RequestBody Map<String, String> request,
            Authentication authentication
    ) {

        teamService.addMember(
                teamId,
                authentication.getName(),
                request.get("email"),
                request.get("role")
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Team member added successfully"
                )
        );
    }

    // =========================================================
    // REMOVE TEAM MEMBER
    // =========================================================

    @DeleteMapping("/{teamId}/members/{memberUserId}")
    public ResponseEntity<?> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long memberUserId,
            Authentication authentication
    ) {

        teamService.removeMember(
                teamId,
                authentication.getName(),
                memberUserId
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Team member removed successfully"
                )
        );
    }

    // =========================================================
    // TEAM RESPONSE
    // =========================================================

    private Map<String, Object> teamResponse(
            Team team
    ) {

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "id",
                team.getId()
        );

        response.put(
                "name",
                team.getName()
        );

        response.put(
                "ownerId",
                team.getOwner().getId()
        );

        response.put(
                "ownerUsername",
                team.getOwner().getUsername()
        );

        response.put(
                "createdAt",
                team.getCreatedAt()
        );

        response.put(
                "updatedAt",
                team.getUpdatedAt()
        );

        return response;
    }

    // =========================================================
    // MEMBER RESPONSE
    // =========================================================

    private Map<String, Object> memberResponse(
            TeamMember member
    ) {

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "id",
                member.getId()
        );

        response.put(
                "userId",
                member.getUser().getId()
        );

        response.put(
                "username",
                member.getUser().getUsername()
        );

        response.put(
                "email",
                member.getUser().getEmail()
        );

        response.put(
                "role",
                member.getRole()
        );

        response.put(
                "joinedAt",
                member.getJoinedAt()
        );

        response.put(
                "updatedAt",
                member.getUpdatedAt()
        );

        return response;
    }
}