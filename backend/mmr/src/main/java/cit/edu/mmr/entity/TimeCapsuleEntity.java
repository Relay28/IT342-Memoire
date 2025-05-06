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
public class TimeCapsuleEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="capsuleId")
    private Long id;

    private String title;

    private String description;

    private Date createdAt;

    private Date openDate;

    private boolean isPublic=false;

    private boolean isLocked;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    @JsonBackReference("user-capsules")
    private UserEntity createdBy; // but

    @OneToMany(mappedBy = "capsule", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("capsule-access")
    private List<CapsuleAccessEntity> capsuleAccesses = new ArrayList<>();

    @OneToMany(mappedBy = "timeCapsule", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("capsule-comments")
    private List<CommentEntity> comments = new ArrayList<>();

    @OneToMany(mappedBy = "capsule", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("capsule-content")
    private List<CapsuleContentEntity> contents = new ArrayList<>();



    private String status;




}
