package cit.edu.mmr.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor // Required for JPA
@AllArgsConstructor
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reportId")
    private long id;

    private long reportedID;

    private String itemType;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    @JsonBackReference("user-report")
    private UserEntity reporter;

    private String status;
    private Date date;
}
