package cit.edu.mmr.service.serviceInterfaces;

import cit.edu.mmr.entity.CommentReactionEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public interface CommentReactionService {

    CommentReactionEntity addReaction(Long commentId, String type, Authentication auth);

    CommentReactionEntity updateReaction(Long reactionId, String type,Authentication auth);

    void deleteReaction(Long reactionId,Authentication auth);

    Optional<CommentReactionEntity> getReactionById(Long reactionId);

    List<CommentReactionEntity> getReactionsByCommentId(Long commentId);
}
