package cit.edu.mmr.repository;

import cit.edu.mmr.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // Find a user by their email address
    Optional<UserEntity> findByEmail(String email);
    
    // Find a user by their username
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    //Optional<UserEntity> findByGoogleSub(String googleSub);
    // Get list of all active users
    List<UserEntity> findByIsActiveTrue();
    
    // Get users by their role (e.g., ADMIN, USER)
    List<UserEntity> findByRole(String role);

    // In UserRepository.java
    Page<UserEntity> findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(
            String username,
            String name,
            Pageable pageable);

    @Query("SELECT u.fcmToken FROM UserEntity u WHERE u.id = :userId")
    String findFcmTokenByUserId(@Param("userId") Long userId);

    @Query("SELECT u FROM UserEntity u WHERE " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND u.id != :excludeUserId")
    List<UserEntity> searchUsers(@Param("query") String query, @Param("excludeUserId") Long excludeUserId);
}