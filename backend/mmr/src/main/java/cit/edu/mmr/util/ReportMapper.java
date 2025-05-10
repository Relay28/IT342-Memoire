package cit.edu.mmr.util;

import cit.edu.mmr.dto.ReportDTO;
import cit.edu.mmr.dto.UserDTO;
import cit.edu.mmr.entity.ReportEntity;
import cit.edu.mmr.entity.UserEntity;

import java.util.List;
import java.util.stream.Collectors;

public class ReportMapper {

    public static ReportDTO toDTO(ReportEntity entity) {
        if (entity == null) {
            return null;
        }

        return ReportDTO.builder()
                .id(entity.getId())
                .reportedID(entity.getReportedID())
                .itemType(entity.getItemType())
                .reporter(convertUserToDTO(entity.getReporter()))
                .status(entity.getStatus())
                .date(entity.getDate())
                .build();
    }

    public static List<ReportDTO> toDTOList(List<ReportEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(ReportMapper::toDTO)
                .collect(Collectors.toList());
    }

    private static UserDTO convertUserToDTO(UserEntity user) {
        // Assuming you have a UserDTO with appropriate fields
        // Replace this with your actual UserDTO conversion logic
        if (user == null) {
            return null;
        }

        // This is a placeholder - replace with your actual UserDTO conversion
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getProfilePictureData(),
                user.getRole(),
                user.getBiography(),
                user.isActive(),
                user.isOauthUser(),
                user.getCreatedAt()
        );
    }
}