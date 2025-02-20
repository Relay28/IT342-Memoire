package cit.edu.mmr.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class CapsuleContentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ccID")
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    @JsonBackReference("user-capsuleContent")
    private UserEntity contentUploadedBy;


    @OneToMany(mappedBy="commentid",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("comment-reaction")
    private List<CommentReactionEntity> reactions = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "capsule_id",nullable = false)
    @JsonBackReference("capsule-content")
    private TimeCapsuleEntity capsule;

    @Lob
    private String filePath;


    private String contentType;

    private Date uploadedAt;



}
