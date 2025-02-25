package cit.edu.mmr.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
public class UserEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="userid")
    private long id;

    private String username;

    private String email;

    private String password;

    @Lob
    private String profilePicture;

    private boolean isActive;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-friendships")
    private List<FriendShipEntity> friendshipsAsUser;

    @OneToMany(mappedBy = "friend", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("friend-friendships")
    private List<FriendShipEntity> friendshipsAsFriend;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-comments")
    private List<CommentEntity> comments;

    // Notifications (One-to-Many)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-notif")
    private List<NotificationEntity> notifications;

    @OneToMany(mappedBy="reporter",cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference("user-report")
    private List<ReportEntity> reports;


// Capsule Access (One-to-Many)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-capsuleAccess")
    private List<CapsuleAccessEntity> capsuleAccesses;

    @OneToMany(mappedBy = "uploadedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-capsuleOwnership")
    private List<CapsuleAccessEntity> uploadedCapsules;

    @OneToMany(mappedBy = "contentUploadedBy" ,cascade=CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference("user-capsuleContent")
    private List<CapsuleContentEntity> content;

    @OneToMany(mappedBy="createdBy",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-capsules")
    private List<TimeCapsuleEntity> timeCapsules = new ArrayList<>();


    @OneToMany(mappedBy="userid",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-commentReaction")
    private List<CommentReactionEntity> commentReaction = new ArrayList<>();

    private String role;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isOauthUser = false;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }



}
