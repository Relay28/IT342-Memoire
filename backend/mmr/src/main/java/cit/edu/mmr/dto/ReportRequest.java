package cit.edu.mmr.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReportRequest {
    private long reportedID;
    private String itemType;
    private long reporterId;
    private String status;

}
