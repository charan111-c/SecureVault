package com.securevault.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.User;

public interface CredentialRepository
        extends JpaRepository<Credential, Long> {

    List<Credential> findByUser(User user);

    Optional<Credential> findByIdAndUser(
            Long id,
            User user
    );

    List<Credential> findByUserAndTitleContainingIgnoreCaseOrUserAndUsernameContainingIgnoreCase(
            User user1,
            String title,
            User user2,
            String username
    );

    List<Credential> findByUserAndCategory(
            User user,
            String category
    );

    List<Credential> findByUserAndCredentialType(
            User user,
            String credentialType
    );

    List<Credential> findByUserAndFavorite(
            User user,
            boolean favorite
    );

    List<Credential> findByUserAndCategoryAndCredentialType(
            User user,
            String category,
            String credentialType
    );

    List<Credential> findByUserAndCategoryAndFavorite(
            User user,
            String category,
            boolean favorite
    );

    List<Credential> findByUserAndCredentialTypeAndFavorite(
            User user,
            String credentialType,
            boolean favorite
    );

    List<Credential> findByUserAndCategoryAndCredentialTypeAndFavorite(
            User user,
            String category,
            String credentialType,
            boolean favorite
    );

    @Query("""
        SELECT c
        FROM Credential c
        WHERE c.user = :user
        AND (
            :query IS NULL
            OR :query = ''
            OR LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(COALESCE(c.username, '')) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(c.category) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(c.credentialType) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        AND (
            :category IS NULL
            OR :category = ''
            OR c.category = :category
        )
        AND (
            :credentialType IS NULL
            OR :credentialType = ''
            OR c.credentialType = :credentialType
        )
        AND (
            :favorite IS NULL
            OR c.favorite = :favorite
        )
        ORDER BY c.updatedAt DESC
    """)
    List<Credential> searchAndFilter(
            @Param("user") User user,
            @Param("query") String query,
            @Param("category") String category,
            @Param("credentialType") String credentialType,
            @Param("favorite") Boolean favorite
    );
}