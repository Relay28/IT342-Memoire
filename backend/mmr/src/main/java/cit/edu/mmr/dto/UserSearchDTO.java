package cit.edu.mmr.dto;

import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSearchDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    @Lob
    private byte[] profilePicture;
}