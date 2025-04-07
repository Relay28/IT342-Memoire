package cit.edu.mmr.dto;


import jakarta.validation.constraints.Pattern;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

@Data
public class UpdateRoleRequest {
    @NotNull(message = "New role cannot be null")
    @NotEmpty(message = "New role cannot be empty")
    @Pattern(regexp = "EDITOR|VIEWER", message = "Role must be either EDITOR or VIEWER")
    private String newRole;
}