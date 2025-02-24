package cit.edu.mmr.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Setter
@Getter
public class NotificationEntity {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notifcationId")
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    @JsonBackReference("user-notif")
    private UserEntity user;

    private String type;

    private String text;

    private  long relatedItemId;

    private String itemType;

    private boolean isRead;

    private Date createdAt;
}
