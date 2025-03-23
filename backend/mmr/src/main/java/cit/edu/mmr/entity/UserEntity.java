package cit.edu.mmr.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="userid")
    private long id;

    private String username;
    private String email;
    private String password;

    @Column(unique = true, nullable = false)
    private String googleSub;

    @Lob
    private String profilePicture;

    private String biography;

    private boolean isActive;
    private String role;  // Should be stored as "ROLE_USER" or "ROLE_ADMIN"

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

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isOauthUser = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }

    // Implement UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
