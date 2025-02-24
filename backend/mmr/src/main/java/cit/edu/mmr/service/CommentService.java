package cit.edu.mmr.service;

import cit.edu.mmr.entity.CommentEntity;

import java.util.List;
import java.util.Optional;

public interface CommentService {

    // New method for creating a comment with relationships
    CommentEntity createComment(Long capsuleId, Long userId, String text);

    CommentEntity updateComment(CommentEntity comment);

    void deleteComment(Long id);

    Optional<CommentEntity> getCommentById(Long id);

    List<CommentEntity> getCommentsByTimeCapsuleId(Long capsuleId);
}
