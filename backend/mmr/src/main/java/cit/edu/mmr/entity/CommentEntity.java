package cit.edu.mmr.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="commentid")
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

    private String text;

    private Date createdAt;

    private Date updatedAt;



    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TimeCapsuleEntity getTimeCapsule() {
        return timeCapsule;
    }

    public void setTimeCapsule(TimeCapsuleEntity timeCapsule) {
        this.timeCapsule = timeCapsule;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }




}
