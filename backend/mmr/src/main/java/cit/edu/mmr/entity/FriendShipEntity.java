package cit.edu.mmr.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.crypto.Data;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor // Required for JPA
@AllArgsConstructor
public class FriendShipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friendshipId")
    private long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    @JsonBackReference("user-friendships")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "friend_id", nullable = false) // Changed name to friend_id
    @JsonBackReference("friend-friendships")
    private UserEntity friend;

    private String Status;

    private Date createdAt;

}
