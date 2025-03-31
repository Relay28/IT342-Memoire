package cit.edu.mmr.repository;

import cit.edu.mmr.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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
}