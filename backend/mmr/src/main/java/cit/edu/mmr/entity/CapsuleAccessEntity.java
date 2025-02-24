package cit.edu.mmr.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

@Entity
public class CapsuleAccessEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="accessid")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "capsule_id", nullable = false)
    @JsonBackReference("capsule-access")
    private TimeCapsuleEntity capsule;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-capsuleAccess")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    @JsonBackReference("user-capsuleOwnership")
    private UserEntity uploadedBy;

    @Column(nullable = false)
    private String role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TimeCapsuleEntity getCapsule() {
        return capsule;
    }

    public void setCapsule(TimeCapsuleEntity capsule) {
        this.capsule = capsule;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public UserEntity getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(UserEntity uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
