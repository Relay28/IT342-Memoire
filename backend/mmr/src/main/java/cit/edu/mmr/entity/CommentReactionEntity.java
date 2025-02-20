package cit.edu.mmr.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.Date;

@Entity
public class CommentReactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="reactionId")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    @JsonBackReference("user-commentReaction")
    private UserEntity userid;

    @ManyToOne
    @JoinColumn(name = "comment_id",nullable = false)
    @JsonBackReference("comment-reaction")
    private CommentEntity commentid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUserid() {
        return userid;
    }

    public void setUserid(UserEntity userid) {
        this.userid = userid;
    }

    public CommentEntity getCommentid() {
        return commentid;
    }

    public void setCommentid(CommentEntity commentid) {
        this.commentid = commentid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getReactedAt() {
        return reactedAt;
    }

    public void setReactedAt(Date reactedAt) {
        this.reactedAt = reactedAt;
    }

    private String type;

   private Date reactedAt;

}
