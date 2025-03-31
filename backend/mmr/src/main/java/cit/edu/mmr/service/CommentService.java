package cit.edu.mmr.service;

import cit.edu.mmr.dto.CommentRequest;
import cit.edu.mmr.entity.CommentEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public interface CommentService {

    // New method for creating a comment with relationships
    CommentEntity createComment(Long capsuleId, Authentication auth ,String text);

    CommentEntity updateComment(Long commentId, CommentRequest commentRequest, Authentication auth);

    void deleteComment(Long id,Authentication auth);

    Optional<CommentEntity> getCommentById(Long id,Authentication auth);

    List<CommentEntity> getCommentsByTimeCapsuleId(Long capsuleId);
}
