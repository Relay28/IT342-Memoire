package cit.edu.mmr.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
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
    private CommentEntity comment;

    private String type;

   private Date reactedAt;

}
