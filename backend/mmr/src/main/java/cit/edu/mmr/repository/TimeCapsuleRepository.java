package cit.edu.mmr.repository;

import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
public interface TimeCapsuleRepository extends JpaRepository<TimeCapsuleEntity, Long> {
    // Get all capsules created by a specific user
    List<TimeCapsuleEntity> findByCreatedBy(UserEntity user);
    
    // Get capsules by their current status
    List<TimeCapsuleEntity> findByStatus(String status);
    
    // Find capsules that are ready to be opened
    @Query("SELECT t FROM TimeCapsuleEntity t WHERE t.openDate <= ?1 AND t.isLocked = true")
    List<TimeCapsuleEntity> findCapsulesToOpen(Date currentDate);
    
    // Search capsules by title
    List<TimeCapsuleEntity> findByTitleContaining(String keyword);
}