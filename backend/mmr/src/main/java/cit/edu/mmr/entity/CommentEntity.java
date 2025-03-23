package cit.edu.mmr.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor // Required for JPA
@AllArgsConstructor
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="commentId")
    public long id;

    @ManyToOne
    @JoinColumn(name = "capsule_id",nullable = false)
    @JsonBackReference("capsule-comments")
    private TimeCapsuleEntity timeCapsule;

    @OneToMany(mappedBy="comment",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("comment-reaction")
    private List<CommentReactionEntity> reactions = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    @JsonBackReference("user-comments")
    private UserEntity user;

    @Column(length = 500)
    private String text;

    private Date createdAt;

    private Date updatedAt;





}
