package cit.edu.mmr.dto;


import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;

    @Lob
    private byte[] profilePicture;
    private String role;

    private String biography;
    private boolean isActive;
    private boolean isOauthUser;
    private Date createdAt;

}
