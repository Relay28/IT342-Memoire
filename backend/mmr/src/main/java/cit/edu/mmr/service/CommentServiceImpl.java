package cit.edu.mmr.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;


import cit.edu.mmr.dto.websocket.CommentUpdateDTO;
import cit.edu.mmr.exception.exceptions.AuthenticationException;
import cit.edu.mmr.exception.exceptions.DatabaseOperationException;
import cit.edu.mmr.util.CommentMapper;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cit.edu.mmr.controller.websocket.CommentWebSocketController;
import cit.edu.mmr.dto.CommentDTO;
import cit.edu.mmr.dto.CommentRequest;
import cit.edu.mmr.entity.CommentEntity;
import cit.edu.mmr.entity.NotificationEntity;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.CommentRepository;
import cit.edu.mmr.repository.TimeCapsuleRepository;
import cit.edu.mmr.repository.UserRepository;

@Service
public class CommentServiceImpl implements cit.edu.mmr.service.CommentService {
    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;
    private final TimeCapsuleRepository timeCapsuleRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final CommentWebSocketController webSocketController;
    private final SimpMessagingTemplate messagingTemplate;
    private final CommentMapper commentMapper;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                              TimeCapsuleRepository timeCapsuleRepository,
                              UserRepository userRepository,
                              NotificationService notificationService,
                              CommentWebSocketController webSocketController,
                              SimpMessagingTemplate messagingTemplate,
                              CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.timeCapsuleRepository = timeCapsuleRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.webSocketController = webSocketController;
        this.messagingTemplate = messagingTemplate;
        this.commentMapper = commentMapper;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "contentMetadata", key = "'comments_capsule_' + #capsuleId"),
            @CacheEvict(value = "contentMetadata", key = "'comment_' + #result.id", condition = "#result != null")
    })
    public CommentDTO createComment(Long capsuleId, Authentication auth, String text) {
        logger.info("Creating comment for capsule ID: {} with text length: {}", capsuleId,
                text != null ? text.length() : 0);

        // Validate inputs
        if (capsuleId == null) {
            logger.warn("Invalid capsule ID: null");
            throw new IllegalArgumentException("Capsule ID must not be null");
        }

        if (text == null || text.trim().isEmpty()) {
            logger.warn("Invalid comment text: empty or null");
            throw new IllegalArgumentException("Comment text cannot be empty");
        }

        // Get time capsule
        TimeCapsuleEntity timeCapsule;
        try {
            timeCapsule = timeCapsuleRepository.findById(capsuleId)
                    .orElseThrow(() -> new EntityNotFoundException("Time capsule not found with ID: " + capsuleId));
            logger.debug("Found time capsule with ID: {}", capsuleId);
        } catch (Exception e) {
            logger.error("Error retrieving time capsule with ID {}: {}", capsuleId, e.getMessage());
            throw new DatabaseOperationException("Error retrieving time capsule", e);
        }

        // Get authenticated user
        UserEntity user = getAuthenticatedUser(auth);
        logger.debug("Authenticated user: {}", user.getUsername());

        // Create and save comment
        try {
            CommentEntity comment = new CommentEntity();
            comment.setTimeCapsule(timeCapsule);
            comment.setUser(user);
            comment.setText(text);
            comment.setCreatedAt(new Date());
            comment.setUpdatedAt(new Date());

            CommentEntity savedComment = commentRepository.save(comment);
            logger.info("Successfully created comment with ID: {} for capsule ID: {}", savedComment.getId(), capsuleId);

            // Create DTO for return
            CommentDTO commentDTO = commentMapper.toDto(savedComment);

            if (!timeCapsule.getCreatedBy().equals(user)) {
                // Only send notification if the commenter is not the owner
                NotificationEntity notification = new NotificationEntity();
                notification.setType("COMMENT");
                notification.setText(user.getUsername() + " commented on your time capsule: " + text);
                notification.setRelatedItemId(savedComment.getId());
                notification.setItemType("COMMENT");

                notificationService.sendNotificationToUser(timeCapsule.getCreatedBy().getId(), notification);
            }

            // Broadcast the new comment to all subscribers
            try {
                webSocketController.broadcastCommentUpdate(savedComment, "CREATE");
                logger.debug("Broadcast comment creation event for comment ID: {}", savedComment.getId());
            } catch (Exception e) {
                // Don't fail the operation if broadcast fails, just log it
                logger.warn("Failed to broadcast comment creation: {}", e.getMessage(), e);
            }

            return commentDTO;
        } catch (Exception e) {
            logger.error("Error saving comment: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error saving comment", e);
        }
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "contentMetadata", key = "'comment_' + #commentId"),
            @CacheEvict(value = "contentMetadata", key = "'comments_capsule_' + #result.capsuleId", condition = "#result != null")
    })
    public CommentDTO updateComment(Long commentId, CommentRequest commentRequest, Authentication auth) {
        logger.info("Updating comment ID: {}", commentId);

        // Validate inputs
        if (commentId == null) {
            logger.warn("Invalid comment ID: null");
            throw new IllegalArgumentException("Comment ID must not be null");
        }

        if (commentRequest == null || commentRequest.getText() == null || commentRequest.getText().trim().isEmpty()) {
            logger.warn("Invalid comment text: empty or null");
            throw new IllegalArgumentException("Comment text cannot be empty");
        }

        // Get authenticated user
        UserEntity user = getAuthenticatedUser(auth);
        logger.debug("Authenticated user: {}", user.getUsername());

        // Retrieve existing comment
        CommentEntity existingComment;
        try {
            existingComment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));
            logger.debug("Found comment with ID: {}", commentId);
        } catch (EntityNotFoundException e) {
            logger.warn("Comment not found with ID: {}", commentId);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving comment with ID {}: {}", commentId, e.getMessage());
            throw new DatabaseOperationException("Error retrieving comment", e);
        }

        // Check if user is the creator of the comment
        if (!existingComment.getUser().equals(user)) {
            logger.warn("Access denied: User {} attempted to update comment {} created by user {}",
                    user.getUsername(), commentId, existingComment.getUser().getUsername());
            throw new AccessDeniedException("You are not the creator of this comment");
        }

        // Update and save comment
        try {
            existingComment.setText(commentRequest.getText());
            existingComment.setUpdatedAt(new Date());

            CommentEntity updatedComment = commentRepository.save(existingComment);
            logger.info("Successfully updated comment ID: {}", commentId);

            // Create DTO for return
            CommentDTO commentDTO = commentMapper.toDto(updatedComment);

            // Broadcast the updated comment
            try {
                webSocketController.broadcastCommentUpdate(updatedComment, "UPDATE");
                logger.debug("Broadcast comment update event for comment ID: {}", updatedComment.getId());
            } catch (Exception e) {
                // Don't fail the operation if broadcast fails, just log it
                logger.warn("Failed to broadcast comment update: {}", e.getMessage(), e);
            }

            return commentDTO;
        } catch (Exception e) {
            logger.error("Error updating comment: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error updating comment", e);
        }
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "contentMetadata", key = "'comment_' + #id"),
            @CacheEvict(value = "contentMetadata", key = "'comments_capsule_' + #existingComment.timeCapsule.id")
    })
    public void deleteComment(Long id, Authentication auth) {
        logger.info("Deleting comment ID: {}", id);

        // Validate input
        if (id == null) {
            logger.warn("Invalid comment ID: null");
            throw new IllegalArgumentException("Comment ID must not be null");
        }

        // Get authenticated user
        UserEntity user = getAuthenticatedUser(auth);
        logger.debug("Authenticated user: {}", user.getUsername());

        // Retrieve existing comment
        CommentEntity existingComment;
        try {
            existingComment = commentRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + id));
            logger.debug("Found comment with ID: {}", id);
        } catch (EntityNotFoundException e) {
            logger.warn("Comment not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving comment with ID {}: {}", id, e.getMessage());
            throw new DatabaseOperationException("Error retrieving comment", e);
        }

        // Check if user is the creator of the comment
        if (!existingComment.getUser().equals(user)) {
            logger.warn("Access denied: User {} attempted to delete comment {} created by user {}",
                    user.getUsername(), id, existingComment.getUser().getUsername());
            throw new AccessDeniedException("You are not the creator of this comment");
        }

        // Delete comment and broadcast deletion
        try {
            // Create a DTO before deleting to broadcast
            CommentUpdateDTO deletionNotice = new CommentUpdateDTO(existingComment, "DELETE");
            Long capsuleId = existingComment.getTimeCapsule().getId();

            // Delete the comment
            commentRepository.deleteById(id);
            logger.info("Successfully deleted comment ID: {}", id);

            // Broadcast the deletion
            try {
                messagingTemplate.convertAndSend(
                        "/topic/capsule/" + capsuleId + "/comments",
                        deletionNotice
                );
                logger.debug("Broadcast comment deletion event for comment ID: {}", id);
            } catch (Exception e) {
                // Don't fail the operation if broadcast fails, just log it
                logger.warn("Failed to broadcast comment deletion: {}", e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.error("Error deleting comment: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error deleting comment", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "contentMetadata", key = "'comment_' + #id")
    public Optional<CommentDTO> getCommentById(Long id, Authentication auth) {
        logger.info("Retrieving comment ID: {}", id);

        // Validate input
        if (id == null) {
            logger.warn("Invalid comment ID: null");
            throw new IllegalArgumentException("Comment ID must not be null");
        }

        // Get authenticated user (for logging purposes)
        try {
            UserEntity user = getAuthenticatedUser(auth);
            logger.debug("Authenticated user: {} retrieving comment ID: {}", user.getUsername(), id);
        } catch (Exception e) {
            logger.warn("Failed to retrieve authenticated user: {}", e.getMessage());
            // Continue with the operation since authentication is not strictly required
        }

        // Retrieve comment
        try {
            Optional<CommentEntity> commentEntity = commentRepository.findById(id);

            if (commentEntity.isPresent()) {
                logger.info("Successfully retrieved comment ID: {}", id);
                return Optional.of(commentMapper.toDto(commentEntity.get()));
            } else {
                logger.debug("Comment not found with ID: {}", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error retrieving comment with ID {}: {}", id, e.getMessage());
            throw new DatabaseOperationException("Error retrieving comment", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommentEntity> getCommentEntityById(Long id) {
        return commentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "contentMetadata", key = "'comments_capsule_' + #capsuleId")
    public List<CommentDTO> getCommentsByTimeCapsuleId(Long capsuleId) {
        logger.info("Retrieving comments for capsule ID: {}", capsuleId);

        // Validate input
        if (capsuleId == null) {
            logger.warn("Invalid capsule ID: null");
            throw new IllegalArgumentException("Capsule ID must not be null");
        }

        // Check if capsule exists
        try {
            boolean capsuleExists = timeCapsuleRepository.existsById(capsuleId);
            if (!capsuleExists) {
                logger.warn("Time capsule not found with ID: {}", capsuleId);
                throw new EntityNotFoundException("Time capsule not found with ID: " + capsuleId);
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error checking if capsule exists with ID {}: {}", capsuleId, e.getMessage());
            throw new DatabaseOperationException("Error checking capsule existence", e);
        }

        // Retrieve comments
        try {
            List<CommentEntity> commentEntities = commentRepository.findByTimeCapsuleId(capsuleId);
            List<CommentDTO> commentDTOs = commentMapper.toDtoList(commentEntities);

            logger.info("Retrieved {} comments for capsule ID: {}", commentDTOs.size(), capsuleId);
            return commentDTOs;
        } catch (Exception e) {
            logger.error("Error retrieving comments for capsule ID {}: {}", capsuleId, e.getMessage());
            throw new DatabaseOperationException("Error retrieving capsule comments", e);
        }
    }

    private UserEntity getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) {
            logger.error("Authentication object is null");
            throw new AuthenticationException("Authentication failed: no authentication provided");
        }

        String username = authentication.getName();
        logger.debug("Getting authenticated user: {}", username);

        try {
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("User not found during authentication: {}", username);
                        return new UsernameNotFoundException("User not found: " + username);
                    });
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving user by username {}: {}", username, e.getMessage());
            throw new DatabaseOperationException("Error retrieving user", e);
        }
    }
}