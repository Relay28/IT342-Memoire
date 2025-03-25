package cit.edu.mmr.repository;

import cit.edu.mmr.entity.CapsuleAccessEntity;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CapsuleAccessRepository extends JpaRepository<CapsuleAccessEntity, Long> {
    // Find all users with access to a specific capsule
    List<CapsuleAccessEntity> findByCapsule(TimeCapsuleEntity capsule);

    Optional<CapsuleAccessEntity> findByCapsuleAndUser(TimeCapsuleEntity capsule, UserEntity user);

    boolean existsByUserIdAndCapsuleIdAndRole(Long userId, Long capsuleId, String role);
    // Find all capsules a user has access to
    List<CapsuleAccessEntity> findByUser(UserEntity user);
    
    // Find all capsules uploaded by a specific user
    List<CapsuleAccessEntity> findByUploadedBy(UserEntity uploadedBy);

    
    // Find users with specific role in a capsule
    List<CapsuleAccessEntity> findByCapsuleAndRole(TimeCapsuleEntity capsule, String role);

    Optional<CapsuleAccessEntity> findByUserAndCapsule(UserEntity user, TimeCapsuleEntity capsule);
}