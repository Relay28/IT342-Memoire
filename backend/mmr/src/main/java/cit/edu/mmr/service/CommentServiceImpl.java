package cit.edu.mmr.service;

import cit.edu.mmr.dto.CommentRequest;
import cit.edu.mmr.entity.CommentEntity;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.CommentRepository;
import cit.edu.mmr.repository.TimeCapsuleRepository;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.serviceInterfaces.CommentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
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

    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public CommentEntity createComment(Long capsuleId, Authentication auth ,String text) {
        // Fetch the TimeCapsuleEntity by its ID
        TimeCapsuleEntity timeCapsule = timeCapsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found with id " + capsuleId));

        // Fetch the UserEntity by its ID
        UserEntity user = getAuthenticatedUser(auth);

        CommentEntity comment = new CommentEntity();
        comment.setTimeCapsule(timeCapsule);
        comment.setUser(user);
        comment.setText(text);
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(new Date());
//        List<CommentEntity> cmnt =  timeCapsule.getComments();
//        cmnt.add(comment);
//        user.setComments(cmnt);

        // Reactions can be set later via separate logic if needed

        return commentRepository.save(comment);
    }

    @Override
    public CommentEntity updateComment(Long commentId, CommentRequest commentRequest, Authentication auth) {

        UserEntity user = getAuthenticatedUser(auth);

        CommentEntity existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id " + commentId));
        existingComment.setText(commentRequest.getText());

        if(existingComment.getUser().equals(user)){
            throw new AccessDeniedException("You are not the creator of comment");
        }


        existingComment.setUpdatedAt(new Date());
        return commentRepository.save(existingComment);
    }

    @Override
    public void deleteComment(Long id,Authentication auth) {
        UserEntity user =getAuthenticatedUser(auth);

        CommentEntity existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id " + id));
        if(existingComment.getUser().equals(user)){
            throw new AccessDeniedException("You are not the creator of comment");
        }
        commentRepository.deleteById(id);
    }

    @Override
    public Optional<CommentEntity> getCommentById(Long id,Authentication auth) {
        return commentRepository.findById(id);
    }

    @Override
    public List<CommentEntity> getCommentsByTimeCapsuleId(Long capsuleId) {
        return commentRepository.findByTimeCapsuleId(capsuleId);
    }
}
