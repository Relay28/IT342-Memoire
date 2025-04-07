package cit.edu.mmr.dto.websocket;

import cit.edu.mmr.entity.CommentEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentUpdateDTO {
    private Long commentId;
    private Long capsuleId;
    private String action; // "CREATE", "UPDATE", "DELETE"
    private CommentEntity comment;

    public CommentUpdateDTO(CommentEntity comment, String action) {
        this.commentId = comment.getId();
        this.capsuleId = comment.getTimeCapsule().getId();
        this.action = action;
        this.comment = comment;
    }
}