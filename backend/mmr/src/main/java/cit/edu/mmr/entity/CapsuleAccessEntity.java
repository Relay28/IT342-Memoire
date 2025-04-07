package cit.edu.mmr.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor // Required for JPA
@AllArgsConstructor
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setCapsule(TimeCapsuleEntity capsule) {
        this.capsule = capsule;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public void setUploadedBy(UserEntity uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public void setRole(String role) {
        this.role = role;
    } // Can Edit of Role EDITOR if VIEWER Only View
}
