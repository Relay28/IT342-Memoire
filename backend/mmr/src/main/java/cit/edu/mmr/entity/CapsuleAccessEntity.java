package cit.edu.mmr.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

@Entity

public class CapsuleAccessEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="accessid")
    private Long accessId;

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
}
