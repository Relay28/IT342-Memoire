package cit.edu.mmr.repository;

import cit.edu.mmr.entity.CommentReactionEntity;
import cit.edu.mmr.entity.CommentEntity;
import cit.edu.mmr.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReactionEntity, Long> {
	// Finds a single reaction by combining user and comment IDs, returns Optional in case reaction doesn't exist
    Optional<CommentReactionEntity> findByUserAndComment(UserEntity user, CommentEntity comment);
   boolean existsByCommentIdAndUserId(Long commentId, Long userId);
    // Find reactions by comment id
    List<CommentReactionEntity> findByCommentId(Long commentId);

    // Gets all reactions for a specific comment, returns them as a List
    List<CommentReactionEntity> findByComment(CommentEntity comment);

    // Checks if a reaction exists for this user and comment combination, returns true/false
    boolean existsByUserAndComment(UserEntity user, CommentEntity comment);

}