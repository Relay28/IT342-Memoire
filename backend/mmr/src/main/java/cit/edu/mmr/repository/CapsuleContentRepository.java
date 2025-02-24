package cit.edu.mmr.repository;

import cit.edu.mmr.entity.CapsuleContentEntity;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CapsuleContentRepository extends JpaRepository<CapsuleContentEntity, Long> {
    // Find all content in a specific capsule
    List<CapsuleContentEntity> findByCapsule(TimeCapsuleEntity capsule);
    List<CapsuleContentEntity> findByCapsuleId(Long capsuleId);
    
    // Find content uploaded by a specific user
    List<CapsuleContentEntity> findByContentUploadedBy(UserEntity user);
    
    // Find content by type in a capsule
    List<CapsuleContentEntity> findByCapsuleAndContentType(TimeCapsuleEntity capsule, String contentType);
    
    // Count content items in a capsule
    Long countByCapsule(TimeCapsuleEntity capsule);
}