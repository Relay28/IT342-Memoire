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
    
    // Find all capsules a user has access to
    List<CapsuleAccessEntity> findByUser(UserEntity user);
    
    // Find all capsules uploaded by a specific user
    List<CapsuleAccessEntity> findByUploadedBy(UserEntity uploadedBy);
    
    // Find access entry for specific user and capsule
    Optional<CapsuleAccessEntity> findByCapsuleAndUser(TimeCapsuleEntity capsule, UserEntity user);
    
    // Find users with specific role in a capsule
    List<CapsuleAccessEntity> findByCapsuleAndRole(TimeCapsuleEntity capsule, String role);
}