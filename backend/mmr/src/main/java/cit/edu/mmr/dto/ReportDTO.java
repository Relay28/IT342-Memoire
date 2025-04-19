package cit.edu.mmr.dto;

import cit.edu.mmr.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportDTO {
    private long id;
    private long reportedID;
    private String itemType;
    private UserDTO reporter;  // Changed from UserEntity to UserDTO
    private String status;
    private Date date;
}