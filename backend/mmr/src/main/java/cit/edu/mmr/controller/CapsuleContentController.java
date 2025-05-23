package cit.edu.mmr.controller;

import cit.edu.mmr.dto.CapsuleContentDetailsDTO;
import cit.edu.mmr.dto.ErrorResponse;
import cit.edu.mmr.entity.CapsuleAccessEntity;
import cit.edu.mmr.entity.CapsuleContentEntity;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.CapsuleAccessRepository;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.serviceInterfaces.CapsuleContentService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/capsule-content")
public class CapsuleContentController {

    private static final Logger logger = LoggerFactory.getLogger(CapsuleContentController.class);

    private final CapsuleContentService capsuleContentService;
    private final CapsuleAccessRepository capsuleAccessRepository;
    private final UserRepository userRepository;

    @Autowired
    public CapsuleContentController(CapsuleContentService capsuleContentService, CapsuleAccessRepository capsuleAccessRepository, UserRepository userRepository) {
        this.capsuleContentService = capsuleContentService;
        this.capsuleAccessRepository = capsuleAccessRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/{capsuleId}/upload")
    public ResponseEntity<?> uploadContent(
            @PathVariable Long capsuleId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            logger.info("Attempting to upload content for capsule ID: {}", capsuleId);
            if (file.isEmpty()) {
                logger.warn("Empty file provided for capsule ID: {}", capsuleId);
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "File cannot be empty"));
            }

            CapsuleContentEntity savedContent = capsuleContentService.uploadContent(capsuleId, file, authentication);
            logger.info("Successfully uploaded content with ID: {} for capsule ID: {}", savedContent.getId(), capsuleId);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedContent);
        } catch (IOException e) {
            logger.error("File upload failed for capsule ID: {}", capsuleId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "File upload failed"));
        } catch (EntityNotFoundException e) {
            logger.warn("Capsule not found with ID: {}", capsuleId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (AccessDeniedException e) {
            logger.warn("Access denied for user {} to upload content to capsule ID: {}", authentication.getName(), capsuleId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during content upload for capsule ID: {}", capsuleId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred"));
        }
    }
    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadContent(@PathVariable Long id, Authentication authentication) {
        try {
            UserEntity user = getAuthenticatedUser(authentication);
            CapsuleContentEntity content = capsuleContentService.getContentById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Content not found"));

            TimeCapsuleEntity capsule = content.getCapsule();
            if (!(capsule.getCreatedBy().getId() == user.getId())) {
                Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, capsule);
                if (accessOpt.isEmpty() || !("VIEWER".equals(accessOpt.get().getRole()) || "EDITOR".equals(accessOpt.get().getRole()))) {
                    throw new AccessDeniedException("You do not have permission to download this content.");
                }
            }

            byte[] fileContent = capsuleContentService.getFileContent(id, authentication);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(content.getContentType()));
            headers.setContentDispositionFormData("attachment", "file");

            return ResponseEntity.ok().headers(headers).body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred"));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContent(@PathVariable Long id, Authentication authentication) {
        try {
            logger.info("Attempting to delete content with ID: {}", id);
            capsuleContentService.deleteContent(id, authentication);
            logger.info("Successfully deleted content with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.warn("Content not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (AccessDeniedException e) {
            logger.warn("Access denied for user {} to delete content ID: {}", authentication.getName(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to delete content with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to delete content"));
        }
    }

    @GetMapping("/{capsuleId}")
    public ResponseEntity<?> getContentsByCapsule(@PathVariable Long capsuleId, Authentication authentication) {
        try {
            logger.info("Fetching contents for capsule ID: {}", capsuleId);
            List<CapsuleContentDetailsDTO> contents = capsuleContentService.getContentsbyCaps(capsuleId, authentication);
            logger.info("Successfully fetched {} contents for capsule ID: {}", contents.size(), capsuleId);
            return ResponseEntity.ok(contents);
        } catch (EntityNotFoundException e) {
            logger.warn("Capsule not found with ID: {}", capsuleId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (AccessDeniedException e) {
            logger.warn("Access denied for user {} to view contents of capsule ID: {}", authentication.getName(), capsuleId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to fetch contents for capsule ID: {}", capsuleId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch contents"));
        }
    }

    @GetMapping("/{id}/metadata")
    @Cacheable(value = "contentMetadata", key = "#id")
    public ResponseEntity<?> getContentMetadata(@PathVariable Long id, Authentication authentication) {
        try {
            logger.info("Fetching metadata for content ID: {}", id);
            Optional<CapsuleContentEntity> content = capsuleContentService.getContentById(id);
            if (content.isEmpty()) {
                logger.warn("Content not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Content not found"));
            }

            TimeCapsuleEntity capsule = content.get().getCapsule();
            UserEntity user = (UserEntity) authentication.getPrincipal();

            if (!(capsule.getCreatedBy().getId() ==(user.getId()))) {
                Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, capsule);
                if (accessOpt.isEmpty()) {
                    logger.warn("User {} attempted unauthorized access to content ID: {}", user.getUsername(), id);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Access denied"));
                }
            }

            return ResponseEntity.ok(content.get());

        } catch (Exception e) {
            logger.error("Error fetching metadata for content ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error fetching content metadata"));
        }

    }

    @GetMapping("/public/content/{id}/download")
    public ResponseEntity<?> downloadPublicContent(@PathVariable Long id) {
        try {
            byte[] fileContent = capsuleContentService.getPublicFileContent(id);
            CapsuleContentEntity content = capsuleContentService.getContentById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Content not found"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(content.getContentType()));
            headers.setContentDispositionFormData("attachment", "file");

            return ResponseEntity.ok().headers(headers).body(fileContent);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/public/{capsuleId}/contents")
    public ResponseEntity<List<CapsuleContentDetailsDTO>> getPublicCapsuleContents(@PathVariable Long capsuleId) {
        try {
            List<CapsuleContentDetailsDTO> contents = capsuleContentService.getPublicCapsuleContents(capsuleId);
            return ResponseEntity.ok(contents);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/renderable/{capsuleId}")
    public ResponseEntity<List<Map<String, Object>>> getRenderableContents(
            @PathVariable Long capsuleId,
            Authentication authentication) {
        try {
            List<Map<String, Object>> contents =
                    capsuleContentService.getRenderableContentsByCapsuleId(capsuleId, authentication);
            return ResponseEntity.ok(contents);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
