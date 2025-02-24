package cit.edu.mmr.service;

import cit.edu.mmr.entity.CommentReactionEntity;

import java.util.List;
import java.util.Optional;

public interface CommentReactionService {

    CommentReactionEntity addReaction(Long commentId, Long userId, String type);

    CommentReactionEntity updateReaction(Long reactionId, String type);

    void deleteReaction(Long reactionId);

    Optional<CommentReactionEntity> getReactionById(Long reactionId);

    List<CommentReactionEntity> getReactionsByCommentId(Long commentId);
}
