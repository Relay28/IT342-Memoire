package cit.edu.mmr.service;

import cit.edu.mmr.entity.CommentEntity;
import cit.edu.mmr.entity.CommentReactionEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.CommentReactionRepository;
import cit.edu.mmr.repository.CommentRepository;
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
public class CommentReactionServiceImpl implements CommentReactionService {

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

    @Override
    public CommentReactionEntity addReaction(Long commentId, Long userId, String type) {
        // Fetch CommentEntity
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id " + commentId));
        // Fetch UserEntity
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + userId));

        CommentReactionEntity reaction = new CommentReactionEntity();
        reaction.setComment(comment);
        reaction.setUserid(user);
        reaction.setType(type);
        reaction.setReactedAt(new Date());

        return reactionRepository.save(reaction);
    }

    @Override
    public CommentReactionEntity updateReaction(Long reactionId, String type) {
        CommentReactionEntity reaction = reactionRepository.findById(reactionId)
                .orElseThrow(() -> new EntityNotFoundException("Reaction not found with id " + reactionId));
        reaction.setType(type);
        reaction.setReactedAt(new Date());
        return reactionRepository.save(reaction);
    }

    @Override
    public void deleteReaction(Long reactionId) {
        if (!reactionRepository.existsById(reactionId)) {
            throw new EntityNotFoundException("Reaction not found with id " + reactionId);
        }
        reactionRepository.deleteById(reactionId);
    }

    @Override
    public Optional<CommentReactionEntity> getReactionById(Long reactionId) {
        return reactionRepository.findById(reactionId);
    }

    @Override
    public List<CommentReactionEntity> getReactionsByCommentId(Long commentId) {
        return reactionRepository.findByCommentId(commentId);
    }
}
