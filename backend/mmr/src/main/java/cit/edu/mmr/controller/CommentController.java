package cit.edu.mmr.controller;

import cit.edu.mmr.dto.CommentDTO;
import cit.edu.mmr.dto.CommentRequest;
import cit.edu.mmr.exception.exceptions.DatabaseOperationException;
import cit.edu.mmr.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
@Validated
public class CommentController {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // Create a new comment with a request body for long text
    @PostMapping("/capsule/{capsuleId}")
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable Long capsuleId,
            @Valid @RequestBody CommentRequest commentRequest,
            Authentication auth) {

        logger.info("Received request to create comment for capsule ID: {}", capsuleId);

        try {
            if (capsuleId == null) {
                logger.warn("Invalid capsule ID: null");
                throw new IllegalArgumentException("Capsule ID must not be null");
            }

            if (commentRequest == null || commentRequest.getText() == null || commentRequest.getText().trim().isEmpty()) {
                logger.warn("Invalid comment request: empty or null comment text");
                throw new IllegalArgumentException("Comment text cannot be empty");
            }

            CommentDTO created = commentService.createComment(capsuleId, auth, commentRequest.getText());
            logger.info("Successfully created comment ID: {} for capsule ID: {}", created.getId(), capsuleId);

            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (EntityNotFoundException ex) {
            logger.warn("Entity not found during comment creation: {}", ex.getMessage());
            throw ex;
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid argument during comment creation: {}", ex.getMessage());
            throw ex;
        } catch (AccessDeniedException ex) {
            logger.warn("Access denied during comment creation: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error creating comment: {}", ex.getMessage(), ex);
            throw new DatabaseOperationException("Error creating comment", ex);
        }
    }

    // Update an existing comment
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest commentRequest,
            Authentication auth) {

        logger.info("Received request to update comment ID: {}", commentId);

        try {
            if (commentId == null) {
                logger.warn("Invalid comment ID: null");
                throw new IllegalArgumentException("Comment ID must not be null");
            }

            if (commentRequest == null || commentRequest.getText() == null || commentRequest.getText().trim().isEmpty()) {
                logger.warn("Invalid comment request: empty or null comment text");
                throw new IllegalArgumentException("Comment text cannot be empty");
            }

            CommentDTO updated = commentService.updateComment(commentId, commentRequest, auth);
            logger.info("Successfully updated comment ID: {}", commentId);

            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException ex) {
            logger.warn("Entity not found during comment update: {}", ex.getMessage());
            throw ex;
        } catch (AccessDeniedException ex) {
            logger.warn("Access denied during comment update: {}", ex.getMessage());
            throw ex;
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid argument during comment update: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error updating comment: {}", ex.getMessage(), ex);
            throw new DatabaseOperationException("Error updating comment", ex);
        }
    }

    // Delete a comment
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, Authentication auth) {
        logger.info("Received request to delete comment ID: {}", commentId);

        try {
            if (commentId == null) {
                logger.warn("Invalid comment ID: null");
                throw new IllegalArgumentException("Comment ID must not be null");
            }

            commentService.deleteComment(commentId, auth);
            logger.info("Successfully deleted comment ID: {}", commentId);

            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            logger.warn("Entity not found during comment deletion: {}", ex.getMessage());
            throw ex;
        } catch (AccessDeniedException ex) {
            logger.warn("Access denied during comment deletion: {}", ex.getMessage());
            throw ex;
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid argument during comment deletion: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error deleting comment: {}", ex.getMessage(), ex);
            throw new DatabaseOperationException("Error deleting comment", ex);
        }
    }

    // Retrieve a single comment by its id
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable Long commentId, Authentication auth) {
        logger.info("Received request to get comment ID: {}", commentId);

        try {
            if (commentId == null) {
                logger.warn("Invalid comment ID: null");
                throw new IllegalArgumentException("Comment ID must not be null");
            }

            Optional<CommentDTO> commentOpt = commentService.getCommentById(commentId, auth);

            if (commentOpt.isPresent()) {
                logger.info("Successfully retrieved comment ID: {}", commentId);
                return ResponseEntity.ok(commentOpt.get());
            } else {
                logger.warn("Comment not found with ID: {}", commentId);
                throw new EntityNotFoundException("Comment not found with ID: " + commentId);
            }
        } catch (EntityNotFoundException ex) {
            logger.warn("Entity not found during comment retrieval: {}", ex.getMessage());
            throw ex;
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid argument during comment retrieval: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error retrieving comment: {}", ex.getMessage(), ex);
            throw new DatabaseOperationException("Error retrieving comment", ex);
        }
    }

    // Retrieve all comments associated with a particular Time Capsule
    @GetMapping("/capsule/{capsuleId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByCapsule(@PathVariable Long capsuleId) {
        logger.info("Received request to get comments for capsule ID: {}", capsuleId);

        try {
            if (capsuleId == null) {
                logger.warn("Invalid capsule ID: null");
                throw new IllegalArgumentException("Capsule ID must not be null");
            }

            List<CommentDTO> comments = commentService.getCommentsByTimeCapsuleId(capsuleId);
            logger.info("Retrieved {} comments for capsule ID: {}", comments.size(), capsuleId);

            return ResponseEntity.ok(comments);
        } catch (EntityNotFoundException ex) {
            logger.warn("Entity not found during comments retrieval: {}", ex.getMessage());
            throw ex;
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid argument during comments retrieval: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error retrieving comments for capsule: {}", ex.getMessage(), ex);
            throw new DatabaseOperationException("Error retrieving capsule comments", ex);
        }
    }
}