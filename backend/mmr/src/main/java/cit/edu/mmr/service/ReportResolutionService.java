package cit.edu.mmr.service;

import cit.edu.mmr.dto.CommentDTO;
import cit.edu.mmr.dto.TimeCapsuleDTO;
import cit.edu.mmr.entity.*;
import cit.edu.mmr.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReportResolutionService {
    private static final Logger logger = LoggerFactory.getLogger(ReportResolutionService.class);

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final TimeCapsuleRepository timeCapsuleRepository;
    private final CommentRepository commentRepository;
    private final ReportService reportService;

    @Autowired
    public ReportResolutionService(
            ReportRepository reportRepository,
            UserRepository userRepository,
            TimeCapsuleRepository timeCapsuleRepository,
            CommentRepository commentRepository,
            ReportService reportService) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.timeCapsuleRepository = timeCapsuleRepository;
        this.commentRepository = commentRepository;
        this.reportService = reportService;
    }

    /**
     * Resolves a report as either "good" (no action needed) or "bad" (confiscate content)
     *
     * @param reportId ID of the report to resolve
     * @param resolution "GOOD" or "BAD"
     * @param auth Authentication object to verify admin role
     * @return The updated report entity
     * @throws AccessDeniedException if the user is not an admin
     */
    @Transactional
    @CacheEvict(value = {"reports", "reportedEntities", "confiscatedContent"}, allEntries = true)
    public ReportEntity resolveReport(long reportId, String resolution, Authentication auth)
            throws AccessDeniedException {
        UserEntity admin = getAuthenticatedAdmin(auth);

        Optional<ReportEntity> optionalReport = reportRepository.findById(reportId);
        if (optionalReport.isEmpty()) {
            throw new IllegalArgumentException("Report not found with ID: " + reportId);
        }

        ReportEntity report = optionalReport.get();

        // Update report status based on resolution
        if ("GOOD".equalsIgnoreCase(resolution)) {
            // No action needed, just mark as resolved
            report.setStatus("RESOLVED_GOOD");
        } else if ("BAD".equalsIgnoreCase(resolution)) {
            // Mark as bad and confiscate the content
            report.setStatus("RESOLVED_BAD");

            // Confiscate the reported content
            confiscateContent(report, admin);
        } else {
            throw new IllegalArgumentException("Invalid resolution: " + resolution + ". Must be 'GOOD' or 'BAD'");
        }

        logger.info("Report with ID {} resolved as {}. Status set to: {}",
                reportId, resolution, report.getStatus());

        return reportRepository.save(report);
    }

    /**
     * Confiscates content by transferring ownership to the admin
     *
     * @param report The report containing the content to confiscate
     * @param admin The admin who will become the new owner
     */
    private void confiscateContent(ReportEntity report, UserEntity admin) {
        String itemType = report.getItemType();
        long reportedId = report.getReportedID();

        switch (itemType) {
            case "Comment":
                confiscateComment(reportedId, admin);
                break;

            case "TimeCapsule":
                confiscateTimeCapsule(reportedId, admin);
                break;

            default:
                throw new IllegalArgumentException("Unsupported item type for confiscation: " + itemType);
        }
    }

    /**
     * Confiscates a comment by transferring ownership to the admin
     *
     * @param commentId ID of the comment to confiscate
     * @param admin The admin who will become the new owner
     */
    private void confiscateComment(long commentId, UserEntity admin) {
        Optional<CommentEntity> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isPresent()) {
            CommentEntity comment = optionalComment.get();

            // Store original user ID somewhere if needed (audit logs, etc.)
            UserEntity originalOwner = comment.getUser();
            logger.info("Confiscating comment {} from user {}", commentId, originalOwner.getUsername());

            // Transfer ownership to admin
            comment.setUser(admin);
            comment.setText("[Content removed due to policy violation]");

            commentRepository.save(comment);
        } else {
            logger.warn("Comment with ID {} not found for confiscation", commentId);
        }
    }

    /**
     * Confiscates a time capsule by transferring ownership to the admin
     *
     * @param capsuleId ID of the time capsule to confiscate
     * @param admin The admin who will become the new owner
     */
    private void confiscateTimeCapsule(long capsuleId, UserEntity admin) {
        Optional<TimeCapsuleEntity> optionalCapsule = timeCapsuleRepository.findById(capsuleId);
        if (optionalCapsule.isPresent()) {
            TimeCapsuleEntity capsule = optionalCapsule.get();

            // Store original user ID somewhere if needed (audit logs, etc.)
            UserEntity originalOwner = capsule.getCreatedBy();
            logger.info("Confiscating time capsule {} from user {}", capsuleId, originalOwner.getUsername());

            // Transfer ownership to admin
            capsule.setCreatedBy(admin);
            capsule.setStatus("CONFISCATED");
            capsule.setTitle("[Confiscated] " + capsule.getTitle());

            timeCapsuleRepository.save(capsule);
        } else {
            logger.warn("Time capsule with ID {} not found for confiscation", capsuleId);
        }
    }

    /**
     * Retrieves all confiscated content for admin review
     *
     * @param auth Authentication object to verify admin role
     * @return List of confiscated content (comments and time capsules)
     * @throws AccessDeniedException if the user is not an admin
     */
    @Cacheable(value = "confiscatedContent")
    public ConfiscatedContentResponse getConfiscatedContent(Authentication auth) throws AccessDeniedException {
        UserEntity admin = getAuthenticatedAdmin(auth);

        // Find all confiscated time capsules
        List<TimeCapsuleDTO> confiscatedCapsules = timeCapsuleRepository
                .findByCreatedByAndStatus(admin, "CONFISCATED")
                .stream()
                .map(capsule -> TimeCapsuleDTO.builder()
                        .id(capsule.getId())
                        .title(capsule.getTitle())
                        .description(capsule.getDescription())
                        .createdAt(capsule.getCreatedAt())
                        .openDate(capsule.getOpenDate())
                        .isLocked(capsule.isLocked())
                        .createdById(capsule.getCreatedBy().getId())
                        .status(capsule.getStatus())
                        .build())
                .toList();

        // Find all confiscated comments
        List<CommentDTO> confiscatedComments = commentRepository
                .findByUser(admin).stream()
                .filter(comment -> comment.getText().contains("[Content removed due to policy violation]"))
                .map(comment -> CommentDTO.builder()
                        .id(comment.getId())
                        .capsuleId(comment.getTimeCapsule().getId())
                        .userId(comment.getUser().getId())
                        .text(comment.getText())
                        .createdAt(comment.getCreatedAt())
                        .updatedAt(comment.getUpdatedAt())
                        .build())
                .toList();

        // Create response
        ConfiscatedContentResponse response = new ConfiscatedContentResponse();
        response.setConfiscatedCapsules(confiscatedCapsules);
        response.setConfiscatedComments(confiscatedComments);

        return response;
    }

    /**
     * Verifies that the authenticated user is an admin
     *
     * @param authentication Authentication object
     * @return The admin user entity
     * @throws AccessDeniedException if the user is not an admin
     */
    private UserEntity getAuthenticatedAdmin(Authentication authentication) throws AccessDeniedException {
        String username = authentication.getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));

        if (!"ROLE_ADMIN".equals(user.getRole())) {
            throw new AccessDeniedException("Only administrators can perform this operation");
        }

        return user;
    }

    /**
     * Response class to hold confiscated content
     */
    public static class ConfiscatedContentResponse {
        private List<TimeCapsuleDTO> confiscatedCapsules = new ArrayList<>();
        private List<CommentDTO> confiscatedComments = new ArrayList<>();

        public List<TimeCapsuleDTO> getConfiscatedCapsules() {
            return confiscatedCapsules;
        }

        public void setConfiscatedCapsules(List<TimeCapsuleDTO> confiscatedCapsules) {
            this.confiscatedCapsules = confiscatedCapsules;
        }

        public List<CommentDTO> getConfiscatedComments() {
            return confiscatedComments;
        }

        public void setConfiscatedComments(List<CommentDTO> confiscatedComments) {
            this.confiscatedComments = confiscatedComments;
        }
    }
}