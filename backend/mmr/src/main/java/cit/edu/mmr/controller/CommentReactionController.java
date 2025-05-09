package cit.edu.mmr.controller;

import cit.edu.mmr.dto.ErrorResponse;
import cit.edu.mmr.dto.ReactionRequest;
import cit.edu.mmr.entity.CommentReactionEntity;
import cit.edu.mmr.service.serviceInterfaces.CommentReactionService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment-reactions")
public class CommentReactionController {

    private static final Logger logger = LoggerFactory.getLogger(CommentReactionController.class);
    private final CommentReactionService commentReactionService;

    @Autowired
    public CommentReactionController(CommentReactionService commentReactionService) {
        this.commentReactionService = commentReactionService;
    }

    @PostMapping("/comment/{commentId}")
    public ResponseEntity<?> addReaction(
            @PathVariable Long commentId,
            @RequestBody ReactionRequest reactionRequest,
            Authentication auth) {
        try {
            int reactionCount = commentReactionService.addReaction(commentId, reactionRequest.getType(), auth);
            return ResponseEntity.status(HttpStatus.CREATED).body(reactionCount);
        } catch (EntityNotFoundException ex) {
            logger.warn("Add reaction failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Unexpected error while adding reaction", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(500, "Failed to add reaction"));
        }
    }

    @GetMapping("/comment/{commentId}/count")
    public ResponseEntity<?> getReactionCountByCommentId(@PathVariable Long commentId) {
        try {
            int reactionCount = commentReactionService.getReactionCountByCommentId(commentId);
            return ResponseEntity.ok(reactionCount);
        } catch (Exception ex) {
            logger.error("Failed to fetch reaction count for commentId: {}", commentId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(500, "Failed to fetch reaction count"));
        }
    }

    @GetMapping("/comment/{commentId}/is-reacted")
    public ResponseEntity<?> isReacted(@PathVariable Long commentId, Authentication auth) {
        try {
            boolean reacted = commentReactionService.isReacted(commentId, auth);
            return ResponseEntity.ok(reacted);
        } catch (Exception ex) {
            logger.error("Failed to check if user reacted to commentId: {}", commentId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(500, "Failed to check reaction status"));
        }
    }

    @PutMapping("/{reactionId}")
    public ResponseEntity<?> updateReaction(
            @PathVariable Long reactionId,
            @RequestBody ReactionRequest reactionRequest,
            Authentication auth) {
        try {
            CommentReactionEntity updated = commentReactionService.updateReaction(reactionId, reactionRequest.getType(), auth);
            return ResponseEntity.ok(updated);
        } catch (AccessDeniedException ex) {
            logger.warn("Access denied on update: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(403, ex.getMessage()));
        } catch (EntityNotFoundException ex) {
            logger.warn("Reaction not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Unexpected error while updating reaction", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(500, "Failed to update reaction"));
        }
    }

    @DeleteMapping("/{reactionId}")
    public ResponseEntity<?> deleteReaction(@PathVariable Long reactionId, Authentication auth) {
        try {
            commentReactionService.deleteReaction(reactionId, auth);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException ex) {
            logger.warn("Access denied on delete: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(403, ex.getMessage()));
        } catch (EntityNotFoundException ex) {
            logger.warn("Reaction not found on delete: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Unexpected error while deleting reaction", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(500, "Failed to delete reaction"));
        }
    }

    @GetMapping("/{reactionId}")
    public ResponseEntity<Object> getReactionById(@PathVariable Long reactionId) {
        return commentReactionService.getReactionById(reactionId)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Reaction not found with id: {}", reactionId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ErrorResponse(404, "Reaction not found with id " + reactionId));
                });
    }

    @GetMapping("/getReaction/comment/{commentId}")
    public ResponseEntity<?> getReactionsByCommentId(@PathVariable Long commentId) {
        try {
            List<CommentReactionEntity> reactions = commentReactionService.getReactionsByCommentId(commentId);
            return ResponseEntity.ok(reactions);
        } catch (Exception ex) {
            logger.error("Failed to fetch reactions for commentId: {}", commentId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(500, "Failed to fetch reactions"));
        }
    }
}
