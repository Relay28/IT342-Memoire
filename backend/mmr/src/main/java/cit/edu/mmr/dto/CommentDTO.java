package cit.edu.mmr.dto;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Comment entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private Long capsuleId;
    private Long userId;
    private String username;
    private byte[] userProfileImage;
    private String text;
    private Date createdAt;
    private Date updatedAt;
    private int reactionCount;
    private List<ReactionSummaryDTO> reactionSummary;

    /**
     * DTO for summarizing reactions by type
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReactionSummaryDTO {
        private String type;
        private int count;
    }
}