package cit.edu.mmr.controller;

import cit.edu.mmr.dto.ReactionRequest;
import cit.edu.mmr.entity.CommentReactionEntity;
import cit.edu.mmr.service.CommentReactionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment-reactions")
public class CommentReactionController {

    private final CommentReactionService commentReactionService;

    @Autowired
    public CommentReactionController(CommentReactionService commentReactionService) {
        this.commentReactionService = commentReactionService;
    }

    // Add a new reaction to a comment
    @PostMapping("/comment/{commentId}/user/{userId}")
    public ResponseEntity<CommentReactionEntity> addReaction(
            @PathVariable Long commentId,
            @PathVariable Long userId,
            @RequestBody ReactionRequest reactionRequest) {
        try {
            CommentReactionEntity reaction = commentReactionService.addReaction(commentId, userId, reactionRequest.getType());
            return ResponseEntity.status(HttpStatus.CREATED).body(reaction);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Update an existing reaction
    @PutMapping("/{reactionId}")
    public ResponseEntity<CommentReactionEntity> updateReaction(
            @PathVariable Long reactionId,
            @RequestBody ReactionRequest reactionRequest) {
        try {
            CommentReactionEntity updated = commentReactionService.updateReaction(reactionId, reactionRequest.getType());
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Delete a reaction
    @DeleteMapping("/{reactionId}")
    public ResponseEntity<Void> deleteReaction(@PathVariable Long reactionId) {
        try {
            commentReactionService.deleteReaction(reactionId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Get a single reaction by its id
    @GetMapping("getReaction/{reactionId}")
    public ResponseEntity<CommentReactionEntity> getReactionById(@PathVariable Long reactionId) {
        return commentReactionService.getReactionById(reactionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Get all reactions for a specific comment
    @GetMapping("getReaction/comment/{commentId}")
    public ResponseEntity<List<CommentReactionEntity>> getReactionsByCommentId(@PathVariable Long commentId) {
        List<CommentReactionEntity> reactions = commentReactionService.getReactionsByCommentId(commentId);
        return ResponseEntity.ok(reactions);
    }

    // Optional: Global exception handler for EntityNotFoundException
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}