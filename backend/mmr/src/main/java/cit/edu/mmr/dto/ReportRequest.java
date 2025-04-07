package cit.edu.mmr.dto;

import com.google.firebase.database.annotations.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReportRequest {
    @NotNull
    private Long reporterId;

    @NotNull
    private Long reportedID;

    @NotBlank
    private String itemType;

    @NotBlank
    private String status;


}
