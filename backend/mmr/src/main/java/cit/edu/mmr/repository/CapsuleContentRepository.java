package cit.edu.mmr.repository;

import cit.edu.mmr.entity.CapsuleContentEntity;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CapsuleContentRepository extends JpaRepository<CapsuleContentEntity, Long> {
    // Find all content in a specific capsule
    List<CapsuleContentEntity> findByCapsule(TimeCapsuleEntity capsule);
    List<CapsuleContentEntity> findByCapsuleId(Long capsuleId);
    Optional<CapsuleContentEntity> findById(Long contentId);
    
    // Find content uploaded by a specific user
    List<CapsuleContentEntity> findByContentUploadedBy(UserEntity user);
    
    // Find content by type in a capsule
    List<CapsuleContentEntity> findByCapsuleAndContentType(TimeCapsuleEntity capsule, String contentType);
    
    // Count content items in a capsule
    Long countByCapsule(TimeCapsuleEntity capsule);



    @Query("SELECT c FROM CapsuleContentEntity c WHERE c.capsule.id = :capsuleId ORDER BY c.uploadedAt DESC")
    List<CapsuleContentEntity> findByCapsuleId2(@Param("capsuleId") Long capsuleId);

    // Find content by ID with capsule and uploader information
    @Query("SELECT c FROM CapsuleContentEntity c LEFT JOIN FETCH c.capsule LEFT JOIN FETCH c.contentUploadedBy WHERE c.id = :id")
    Optional<CapsuleContentEntity> findByIdWithDetails(@Param("id") Long id);

    // Check if a user has uploaded content to a capsule
    @Query("SELECT COUNT(c) > 0 FROM CapsuleContentEntity c WHERE c.id = :contentId AND c.contentUploadedBy.id = :userId")
    boolean isContentUploadedByUser(@Param("contentId") Long contentId, @Param("userId") Long userId);

    // Find recent media uploads for a capsule
    @Query("SELECT c FROM CapsuleContentEntity c WHERE c.capsule.id = :capsuleId AND (c.contentType LIKE 'image/%' OR c.contentType LIKE 'video/%') ORDER BY c.uploadedAt DESC")
    List<CapsuleContentEntity> findRecentMediaByCapsuleId(@Param("capsuleId") Long capsuleId, Pageable pageable);

    // Count media items in a capsule
    @Query("SELECT COUNT(c) FROM CapsuleContentEntity c WHERE c.capsule.id = :capsuleId AND (c.contentType LIKE 'image/%' OR c.contentType LIKE 'video/%')")
    long countMediaByCapsuleId(@Param("capsuleId") Long capsuleId);
}