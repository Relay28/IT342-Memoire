package cit.edu.mmr.dto;

import lombok.Data;
import lombok.NonNull;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

@Data
public class GrantAccessRequest {
    @NotNull(message = "Capsule ID cannot be null")
    private Long capsuleId;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Uploader ID cannot be null")
    private Long uploaderId;

    @NotNull(message = "Role cannot be null")
    @NotEmpty(message = "Role cannot be empty")
    private String role;
}

