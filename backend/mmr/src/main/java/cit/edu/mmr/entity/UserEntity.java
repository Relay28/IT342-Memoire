package cit.edu.mmr.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
@Table(name = "user_entity")
@Getter
@Setter
@NoArgsConstructor // Required for JPA
@AllArgsConstructor
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long id;

    private String username;
    private String email;
    private String password;

    private String name;

//    @Column(unique = true, nullable = true)
//    private String googleSub;

    @Lob
    @Column(name = "profile_picture_data", columnDefinition = "LONGBLOB")
    private byte[] profilePictureData;


    private String biography;

    private boolean isActive;
    private String role;  // Should be stored as "ROLE_USER" or "ROLE_ADMIN"

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-friendships")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<FriendShipEntity> friendshipsAsUser = new ArrayList<>();
    @OneToMany(mappedBy = "friend", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("friend-friendships")
    private List<FriendShipEntity> friendshipsAsFriend =new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-comments")
    private List<CommentEntity> comments  =new ArrayList<>();

    // Notifications (One-to-Many)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-notif")
    private List<NotificationEntity> notifications  =new ArrayList<>();

    @OneToMany(mappedBy="reporter",cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference("user-report")
    private List<ReportEntity> reports  =new ArrayList<>();


    // Capsule Access (One-to-Many)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-capsuleAccess")
    private List<CapsuleAccessEntity> capsuleAccesses  =new ArrayList<>();

    @OneToMany(mappedBy = "uploadedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-capsuleOwnership")
    private List<CapsuleAccessEntity> uploadedCapsules  =new ArrayList<>();

    @OneToMany(mappedBy = "contentUploadedBy" ,cascade=CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference("user-capsuleContent")
    private List<CapsuleContentEntity> content  =new ArrayList<>();

    @OneToMany(mappedBy="createdBy",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-capsules")
    private List<TimeCapsuleEntity> timeCapsules = new ArrayList<>();


    @OneToMany(mappedBy="user",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-commentReaction")
    private List<CommentReactionEntity> commentReaction = new ArrayList<>();

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean isOauthUser;

    @Column(name = "fcm_token")
    private String fcmToken;

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
