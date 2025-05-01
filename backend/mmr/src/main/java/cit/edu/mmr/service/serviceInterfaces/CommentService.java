package cit.edu.mmr.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;

import cit.edu.mmr.dto.CommentDTO;
import cit.edu.mmr.dto.CommentRequest;
import cit.edu.mmr.entity.CommentEntity;

/**
 * Service interface for Comment operations
 */
public interface CommentService {

    /**
     * Creates a new comment
     */
    CommentDTO createComment(Long capsuleId, Authentication auth, String text);

    /**
     * Updates an existing comment
     */
    CommentDTO updateComment(Long commentId, CommentRequest commentRequest, Authentication auth);

    /**
     * Deletes a comment
     */
    void deleteComment(Long id, Authentication auth);

    /**
     * Retrieves a comment by ID
     */
    Optional<CommentDTO> getCommentById(Long id, Authentication auth);

    /**
     * Retrieves all comments for a time capsule
     */
    List<CommentDTO> getCommentsByTimeCapsuleId(Long capsuleId);

    /**
     * Gets the comment entity by ID (for internal use)
     */
    Optional<CommentEntity> getCommentEntityById(Long id);
}