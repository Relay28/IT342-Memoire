package cit.edu.mmr.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
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


    public List<TimeCapsuleEntity> getTimeCapsules() {
        return timeCapsules;
    }

    public void setTimeCapsules(List<TimeCapsuleEntity> timeCapsules) {
        this.timeCapsules = timeCapsules;
    }




    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    private Date createdAt;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    private String role;



}
