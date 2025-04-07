package cit.edu.mmr.service.serviceInterfaces.report;

import cit.edu.mmr.entity.CommentEntity;
import cit.edu.mmr.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentHandler implements ReportableEntity {
    private final CommentRepository commentRepository;

    @Autowired
    public CommentHandler(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public Long getId() {
        return null; // Not used for the handler itself
    }

    @Override
    public String getEntityType() {
        return "Comment";
    }

    @Override
    public void validate() throws IllegalArgumentException {
        // Validation logic specific to Comment
    }

    public CommentEntity getEntity(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + id));
    }
}