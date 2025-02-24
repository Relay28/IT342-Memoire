package cit.edu.mmr.repository;

import cit.edu.mmr.entity.CommentEntity;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    // Get all comments for a specific time capsule
    List<CommentEntity> findByTimeCapsule(TimeCapsuleEntity timeCapsule);

    List<CommentEntity> findByTimeCapsuleId(Long capsuleId);
    
    // Get all comments by a specific user
    List<CommentEntity> findByUser(UserEntity user);
    
    // Get comments for a capsule ordered by creation date
    List<CommentEntity> findByTimeCapsuleOrderByCreatedAtDesc(TimeCapsuleEntity timeCapsule);
    
    // Count number of comments on a specific capsule
    Long countByTimeCapsule(TimeCapsuleEntity timeCapsule);
}