package cit.edu.mmr.dto;


import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

@Data
public class UpdateRoleRequest {
    @NotNull(message = "New role cannot be null")
    @NotEmpty(message = "New role cannot be empty")
    private String newRole;
}