package cit.edu.mmr.repository;

import cit.edu.mmr.entity.CapsuleAccessEntity;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CapsuleAccessRepository extends JpaRepository<CapsuleAccessEntity, Long> {
    // Find all users with access to a specific capsule
    List<CapsuleAccessEntity> findByCapsule(TimeCapsuleEntity capsule);
    @Query("SELECT ca.user.id FROM CapsuleAccessEntity ca WHERE ca.capsule.id = :capsuleId")
    List<Long> findUserIdsWithAccessToCapsule(@Param("capsuleId") Long capsuleId);
    Optional<CapsuleAccessEntity> findByCapsuleAndUser(TimeCapsuleEntity capsule, UserEntity user);

    boolean existsByUserIdAndCapsuleIdAndRole(Long userId, Long capsuleId, String role);
    // Find all capsules a user has access to
    List<CapsuleAccessEntity> findByUser(UserEntity user);
    
    // Find all capsules uploaded by a specific user
    List<CapsuleAccessEntity> findByUploadedBy(UserEntity uploadedBy);

    Optional<CapsuleAccessEntity> findByCapsuleIdAndUserId(Long capsuleId, Long userId);

    // Find all access entries for a specific user
    List<CapsuleAccessEntity> findByUserId(Long userId);

    // Find all access entries for a specific capsule
    List<CapsuleAccessEntity> findByCapsuleId(Long capsuleId);

    // Find all access entries for a specific role
    List<CapsuleAccessEntity> findByRole(String role);

    // Find all access entries for a specific user and role
    List<CapsuleAccessEntity> findByUserIdAndRole(Long userId, String role);


    @Modifying
    @Transactional
    void deleteByCapsuleId(Long capsuleId);

    // Delete access entry for a specific capsule and user
    @Modifying
    @Transactional
    void deleteByCapsuleIdAndUserId(Long capsuleId, Long userId);
    // Find users with specific role in a capsule
    List<CapsuleAccessEntity> findByCapsuleAndRole(TimeCapsuleEntity capsule, String role);

    Optional<CapsuleAccessEntity> findByUserAndCapsule(UserEntity user, TimeCapsuleEntity capsule);

//    @Query("SELECT CASE WHEN COUNT(ca) > 0 THEN true ELSE false END FROM CapsuleAccessEntity ca WHERE ca.user.id = :userId AND ca.capsule.id = :capsuleId AND ca.role = :role")
//    boolean existsByUserIdAndCapsuleIdAndRole(@Param("userId") Long userId, @Param("capsuleId") Long capsuleId, @Param("role") String role);

    // Find access record for specific user and capsule
    @Query("SELECT ca FROM CapsuleAccessEntity ca WHERE ca.user.id = :userId AND ca.capsule.id = :capsuleId")
    Optional<CapsuleAccessEntity> findByUserIdAndCapsuleId(@Param("userId") Long userId, @Param("capsuleId") Long capsuleId);

    // Find all editors for a capsule
    @Query("SELECT ca.user FROM CapsuleAccessEntity ca WHERE ca.capsule.id = :capsuleId AND ca.role = 'EDITOR'")
    List<UserEntity> findEditorsByCapsuleId(@Param("capsuleId") Long capsuleId);

    // Find all users with access to a capsule
    @Query("SELECT DISTINCT ca.user FROM CapsuleAccessEntity ca WHERE ca.capsule.id = :capsuleId")
    List<UserEntity> findAllUsersWithAccess(@Param("capsuleId") Long capsuleId);

    // Find all capsules a user can access
    @Query("SELECT ca.capsule FROM CapsuleAccessEntity ca WHERE ca.user.id = :userId")
    List<TimeCapsuleEntity> findAccessibleCapsules(@Param("userId") Long userId);
}