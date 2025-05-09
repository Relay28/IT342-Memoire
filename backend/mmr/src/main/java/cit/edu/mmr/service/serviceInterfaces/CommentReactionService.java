package cit.edu.mmr.service.serviceInterfaces;

import cit.edu.mmr.entity.CommentReactionEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public interface CommentReactionService {

    @Cacheable(value = "contentMetadata", key = "'commentReactionsCount_' + #commentId")
    int getReactionCountByCommentId(Long commentId);

    boolean isReacted(Long commentId, Authentication auth);

    int addReaction(Long commentId, String type, Authentication auth);

    CommentReactionEntity updateReaction(Long reactionId, String type,Authentication auth);

    void deleteReaction(Long reactionId,Authentication auth);

    Optional<CommentReactionEntity> getReactionById(Long reactionId);

    List<CommentReactionEntity> getReactionsByCommentId(Long commentId);
}
