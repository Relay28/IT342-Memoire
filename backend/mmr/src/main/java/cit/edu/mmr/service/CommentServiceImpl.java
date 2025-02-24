package cit.edu.mmr.service;

import cit.edu.mmr.entity.CommentEntity;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.CommentRepository;
import cit.edu.mmr.repository.TimeCapsuleRepository;
import cit.edu.mmr.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TimeCapsuleRepository timeCapsuleRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                              TimeCapsuleRepository timeCapsuleRepository,
                              UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.timeCapsuleRepository = timeCapsuleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CommentEntity createComment(Long capsuleId, Long userId, String text) {
        // Fetch the TimeCapsuleEntity by its ID
        TimeCapsuleEntity timeCapsule = timeCapsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found with id " + capsuleId));

        // Fetch the UserEntity by its ID
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + userId));

        CommentEntity comment = new CommentEntity();
        comment.setTimeCapsule(timeCapsule);
        comment.setUser(user);
        comment.setText(text);
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(new Date());
        // Reactions can be set later via separate logic if needed

        return commentRepository.save(comment);
    }

    @Override
    public CommentEntity updateComment(CommentEntity comment) {
        if (!commentRepository.existsById(comment.getId())) {
            throw new EntityNotFoundException("Comment not found with id " + comment.getId());
        }
        comment.setUpdatedAt(new Date());
        return commentRepository.save(comment);
    }

    @Override
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new EntityNotFoundException("Comment not found with id " + id);
        }
        commentRepository.deleteById(id);
    }

    @Override
    public Optional<CommentEntity> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    @Override
    public List<CommentEntity> getCommentsByTimeCapsuleId(Long capsuleId) {
        return commentRepository.findByTimeCapsuleId(capsuleId);
    }
}
