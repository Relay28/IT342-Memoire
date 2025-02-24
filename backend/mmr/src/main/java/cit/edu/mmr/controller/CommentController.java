package cit.edu.mmr.controller;

import cit.edu.mmr.dto.CommentRequest;
import cit.edu.mmr.entity.CommentEntity;
import cit.edu.mmr.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@Validated
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // Create a new comment with a request body for long text
    @PostMapping("/capsule/{capsuleId}/user/{userId}")
    public ResponseEntity<CommentEntity> createComment(
            @PathVariable Long capsuleId,
            @PathVariable Long userId,
            @RequestBody CommentRequest commentRequest) {
        try {
            CommentEntity created = commentService.createComment(capsuleId, userId, commentRequest.getText());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Update an existing comment
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentEntity> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequest commentRequest) {
        try {
            CommentEntity existing = commentService.getCommentById(commentId)
                    .orElseThrow(() -> new EntityNotFoundException("Comment not found with id " + commentId));
            existing.setText(commentRequest.getText());
            CommentEntity updated = commentService.updateComment(existing);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Delete a comment
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Retrieve a single comment by its id
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentEntity> getCommentById(@PathVariable Long commentId) {
        return commentService.getCommentById(commentId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Retrieve all comments associated with a particular Time Capsule
    @GetMapping("/capsule/{capsuleId}")
    public ResponseEntity<List<CommentEntity>> getCommentsByCapsule(@PathVariable Long capsuleId) {
        List<CommentEntity> comments = commentService.getCommentsByTimeCapsuleId(capsuleId);
        return ResponseEntity.ok(comments);
    }

    // Global exception handling for EntityNotFoundException (optional)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
