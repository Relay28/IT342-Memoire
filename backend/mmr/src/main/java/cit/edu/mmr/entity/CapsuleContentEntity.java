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

@Setter
@Getter
@Entity
@NoArgsConstructor // Required for JPA
@AllArgsConstructor
public class CapsuleContentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ccID")
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    @JsonBackReference("user-capsuleContent")
    private UserEntity contentUploadedBy;




    @ManyToOne
    @JoinColumn(name = "capsule_id",nullable = false)
    @JsonBackReference("capsule-content")
    private TimeCapsuleEntity capsule;

    @Lob
    private String filePath;


    private String contentType;

    private Date uploadedAt;

}
