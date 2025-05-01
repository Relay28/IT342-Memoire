package cit.edu.mmr.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import cit.edu.mmr.dto.CommentDTO;
import cit.edu.mmr.dto.CommentDTO.ReactionSummaryDTO;
import cit.edu.mmr.entity.CommentEntity;
import cit.edu.mmr.entity.CommentReactionEntity;

/**
 * Mapper class to convert between Comment entities and DTOs
 */
@Component
public class CommentMapper {

    /**
     * Converts a Comment entity to a DTO
     */
    public CommentDTO toDto(CommentEntity entity) {
        if (entity == null) {
            return null;
        }

        CommentDTO dto = new CommentDTO();
        dto.setId(entity.getId());
        dto.setCapsuleId(entity.getTimeCapsule().getId());
        dto.setUserId(entity.getUser().getId());
        dto.setUsername(entity.getUser().getUsername());
        // Set profile image if available
        dto.setUserProfileImage(entity.getUser().getProfilePictureData());
        dto.setText(entity.getText());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Handle reactions - this avoids lazy loading issues
        // We'll only set the count here, assuming reactions might be lazy loaded
        if (entity.getReactions() != null && entity.getReactions().size() > 0) {
            dto.setReactionCount(entity.getReactions().size());

            // Summarize reactions by type
            Map<String, Long> reactionsByType = entity.getReactions().stream()
                    .collect(Collectors.groupingBy(CommentReactionEntity::getType, Collectors.counting()));

            List<ReactionSummaryDTO> summaries = new ArrayList<>();
            reactionsByType.forEach((type, count) -> {
                summaries.add(new ReactionSummaryDTO(type, count.intValue()));
            });

            dto.setReactionSummary(summaries);
        } else {
            dto.setReactionCount(0);
            dto.setReactionSummary(new ArrayList<>());
        }

        return dto;
    }

    /**
     * Converts a list of Comment entities to DTOs
     */
    public List<CommentDTO> toDtoList(List<CommentEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}