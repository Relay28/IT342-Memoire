package cit.edu.mmr.service;

import cit.edu.mmr.entity.CommentEntity;
import cit.edu.mmr.entity.CommentReactionEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.CommentReactionRepository;
import cit.edu.mmr.repository.CommentRepository;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.serviceInterfaces.CommentReactionService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CommentReactionServiceImpl implements CommentReactionService {

    private static final Logger logger = LoggerFactory.getLogger(CommentReactionServiceImpl.class);

    private final CommentReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentReactionServiceImpl(CommentReactionRepository reactionRepository,
                                      CommentRepository commentRepository,
                                      UserRepository userRepository) {
        this.reactionRepository = reactionRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    @Cacheable(value = "contentMetadata", key = "'commentReactionsCount_' + #commentId")
    public int getReactionCountByCommentId(Long commentId) {
        logger.info("Fetching reaction count for commentId: {}", commentId);
        return reactionRepository.findByCommentId(commentId).size();
    }
    @Override
    public boolean isReacted(Long commentId, Authentication auth) {
        logger.info("Checking if user has reacted to commentId: {}", commentId);

        UserEntity user = getAuthenticatedUser(auth);
        logger.debug("Authenticated user ID: {}", user.getId());
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id " + commentId));
        boolean exists = reactionRepository.existsByUserAndComment(user, comment);
        logger.debug("Reaction exists for commentId: {} and userId: {}: {}", commentId, user.getId(), exists);

        return exists;
    }
    @Override
    @CacheEvict(value = "contentMetadata", key = "'commentReactions_' + #commentId")
    public int addReaction(Long commentId, String type, Authentication auth) {
        logger.info("Adding reaction to commentId: {} by user: {}", commentId, auth.getName());

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id " + commentId));

        UserEntity user = getAuthenticatedUser(auth);

        CommentReactionEntity reaction = new CommentReactionEntity();
        reaction.setComment(comment);
        reaction.setUser(user);
        reaction.setType(type);
        reaction.setReactedAt(new Date());

        reactionRepository.save(reaction);

        // Return the updated count of reactions for the comment
        return reactionRepository.findByCommentId(commentId).size();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "contentMetadata", key = "'commentReactions_' + #reaction.comment.id"),
            @CacheEvict(value = "contentMetadata", key = "'reaction_' + #reactionId")
    })
    public CommentReactionEntity updateReaction(Long reactionId, String type, Authentication auth) {
        logger.info("Updating reactionId: {} by user: {}", reactionId, auth.getName());

        UserEntity user = getAuthenticatedUser(auth);

        CommentReactionEntity reaction = reactionRepository.findById(reactionId)
                .orElseThrow(() -> new EntityNotFoundException("Reaction not found with id " + reactionId));

        if (!reaction.getUser().equals(user)) {
            throw new AccessDeniedException("You are not the creator of this reaction");
        }

        reaction.setType(type);
        reaction.setReactedAt(new Date());

        CommentReactionEntity updated = reactionRepository.save(reaction);
        logger.debug("Reaction updated: {}", updated);

        return updated;
    }

    @Override
    @Transactional
    @CacheEvict(value = "contentMetadata", allEntries = true)  // This will clear all related caches
    public void deleteReaction(Long reactionId, Authentication auth) {
        logger.info("Deleting reactionId: {} by user: {}", reactionId, auth.getName());

        UserEntity user = getAuthenticatedUser(auth);

        CommentReactionEntity reaction = reactionRepository.findById(reactionId)
                .orElseThrow(() -> new EntityNotFoundException("Reaction not found with id " + reactionId));

        if (!reaction.getUser().equals(user)) {
            throw new AccessDeniedException("You are not the creator of this reaction");
        }

        // Store the comment ID before deletion for logging
        Long commentId = reaction.getComment() != null ? reaction.getComment().getId() : null;

        reactionRepository.deleteById(reactionId);
        logger.debug("Reaction deleted with id: {} for comment: {}", reactionId, commentId);
    }
    @Override
    @Cacheable(value = "contentMetadata", key = "'reaction_' + #reactionId")
    public Optional<CommentReactionEntity> getReactionById(Long reactionId) {
        logger.info("Fetching reaction by id: {}", reactionId);
        return reactionRepository.findById(reactionId);
    }

    @Override
    @Cacheable(value = "contentMetadata", key = "'commentReactions_' + #commentId")
    public List<CommentReactionEntity> getReactionsByCommentId(Long commentId) {
        logger.info("Fetching reactions for commentId: {}", commentId);
        return reactionRepository.findByCommentId(commentId);
    }
}