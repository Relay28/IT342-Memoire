package cit.edu.mmr.dto;

import lombok.Data;

@Data
public class CapsuleAccessDTO {
    private Long id;
    private Long capsuleId;
    private Long userId;
    private Long uploadedById;
    private String role;
}
