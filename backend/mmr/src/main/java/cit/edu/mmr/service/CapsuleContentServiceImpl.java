package cit.edu.mmr.service;

import cit.edu.mmr.dto.CapsuleContentDetailsDTO;
import cit.edu.mmr.entity.CapsuleAccessEntity;
import cit.edu.mmr.entity.CapsuleContentEntity;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.CapsuleAccessRepository;
import cit.edu.mmr.repository.CapsuleContentRepository;
import cit.edu.mmr.repository.TimeCapsuleRepository;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.serviceInterfaces.CapsuleContentService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;

@Service
@Transactional
public class CapsuleContentServiceImpl implements CapsuleContentService {

    private static final String CONTENT_METADATA_CACHE = "contentMetadata";
    private static final String CAPSULE_CONTENTS_CACHE = "capsuleContents";
    private final CapsuleContentRepository capsuleContentRepository;
    private final TimeCapsuleRepository timeCapsuleRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final Set<Long> activeCapsuleUpdates = ConcurrentHashMap.newKeySet();
    private final CapsuleAccessRepository capsuleAccessRepository;

    private final Set<Long> recentlyUpdatedCapsules = ConcurrentHashMap.newKeySet();
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(CapsuleContentServiceImpl.class);

    @Autowired
    public CapsuleContentServiceImpl(CapsuleContentRepository capsuleContentRepository,
                                     TimeCapsuleRepository timeCapsuleRepository,
                                     SimpMessagingTemplate messagingTemplate,
                                     CapsuleAccessRepository capsuleAccessRepository,
                                     UserRepository userRepository) {
        this.capsuleContentRepository = capsuleContentRepository;
        this.timeCapsuleRepository = timeCapsuleRepository;
        this.messagingTemplate = messagingTemplate;
        this.capsuleAccessRepository = capsuleAccessRepository;
        this.userRepository = userRepository;
    }

    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public List<Map<String, Object>> getRenderableContentsByCapsuleId(Long capsuleId, Authentication authentication) {
        try {
            logger.debug("Fetching renderable contents for capsule ID: {}", capsuleId);
            UserEntity user = getAuthenticatedUser(authentication);
            TimeCapsuleEntity capsule = timeCapsuleRepository.findById(capsuleId)
                    .orElseThrow(() -> {
                        logger.warn("Capsule not found with ID: {}", capsuleId);
                        return new EntityNotFoundException("Capsule not found with id " + capsuleId);
                    });

            // Check user access rights
            boolean isOwner = capsule.getCreatedBy().getId() == user.getId();
            String userRole = null;

            if (!isOwner) {
                Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, capsule);
                if (accessOpt.isEmpty()) {
                    logger.warn("User {} attempted unauthorized access to capsule ID: {}", user.getUsername(), capsuleId);
                    throw new AccessDeniedException("You do not have permission to view this content.");
                }
                userRole = accessOpt.get().getRole();
            } else {
                userRole = "OWNER"; // Owner has full access
            }

            List<CapsuleContentEntity> contents = capsuleContentRepository.findByCapsuleId(capsuleId);
            List<Map<String, Object>> renderableContents = new ArrayList<>();

            for (CapsuleContentEntity content : contents) {
                Map<String, Object> contentMap = new HashMap<>();
                contentMap.put("id", content.getId());
                contentMap.put("contentType", content.getContentType());
                contentMap.put("uploadedAt", content.getUploadedAt());
                contentMap.put("uploadedBy", content.getContentUploadedBy().getUsername());

                // Create a URL for accessing the content
                contentMap.put("contentUrl", "/api/capsule-contents/file/" + content.getId());

                // Determine if the user can edit this content
                boolean canEdit = isOwner || "EDITOR".equals(userRole);
                contentMap.put("canEdit", canEdit);

                // For images, we can add width and height if available
                if (content.getContentType() != null && content.getContentType().startsWith("image/")) {
                    contentMap.put("isImage", true);
                } else {
                    contentMap.put("isImage", false);
                }

                renderableContents.add(contentMap);
            }

            logger.debug("Prepared {} renderable contents for capsule ID: {}", renderableContents.size(), capsuleId);
            return renderableContents;
        } catch (Exception e) {
            logger.error("Error fetching renderable contents for capsule ID: {}", capsuleId, e);
            throw e;
        }
    }

    @CacheEvict(value = {CAPSULE_CONTENTS_CACHE, CONTENT_METADATA_CACHE}, key = "#capsuleId")
    @Override
    public CapsuleContentEntity uploadContent(Long capsuleId, MultipartFile file, Authentication authentication) throws IOException {
        try {
            logger.debug("Starting content upload for capsule ID: {}", capsuleId);
            UserEntity user = getAuthenticatedUser(authentication);
            TimeCapsuleEntity capsule = timeCapsuleRepository.findById(capsuleId)
                    .orElseThrow(() -> {
                        logger.warn("Capsule not found with ID: {}", capsuleId);
                        return new EntityNotFoundException("Capsule not found with id " + capsuleId);
                    });

            boolean isOwner = capsule.getCreatedBy().getId() == user.getId();
            if (!isOwner) {
                Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, capsule);
                if (accessOpt.isEmpty() || !"EDITOR".equals(accessOpt.get().getRole())) {
                    logger.warn("User {} attempted unauthorized upload to capsule ID: {}", user.getUsername(), capsuleId);
                    throw new AccessDeniedException("You do not have permission to upload content.");
                }
            }

            CapsuleContentEntity content = new CapsuleContentEntity();
            content.setCapsule(capsule);
            content.setContentUploadedBy(user);
            content.setFileData(file.getBytes());  // Store file data directly as byte array
            content.setContentType(file.getContentType());
            content.setUploadedAt(new Date());

            List<CapsuleContentEntity> caps = capsule.getContents();
            caps.add(content);
            capsule.setContents(caps);

            CapsuleContentEntity savedContent = capsuleContentRepository.save(content);
            logger.info("Successfully uploaded content with ID: {} for capsule ID: {}", savedContent.getId(), capsuleId);

            notifyContentUpdate(capsuleId, savedContent, "add");
            return savedContent;
        } catch (Exception e) {
            logger.error("Error uploading content for capsule ID: {}", capsuleId, e);
            throw e;
        }
    }

    @Transactional
    @CacheEvict(value = {CAPSULE_CONTENTS_CACHE, CONTENT_METADATA_CACHE}, allEntries = true)
    @Override
    public void deleteContent(Long id, Authentication authentication) {
        logger.debug("Starting content deletion for ID: {}", id);

        // First get the content with its relationships
        CapsuleContentEntity content = capsuleContentRepository.findByIdWithRelations(id)
                .orElseThrow(() -> {
                    logger.warn("Content not found with ID: {}", id);
                    return new EntityNotFoundException("Content not found with id " + id);
                });

        // Store critical IDs before any deletion attempts
        Long contentId = content.getId();
        Long capsuleId = content.getCapsule().getId();

        try {
            UserEntity user = getAuthenticatedUser(authentication);

            // Check permissions
            if (!canDeleteContent(user, content)) {
                logger.warn("User {} attempted unauthorized deletion of content ID: {}", user.getUsername(), id);
                throw new AccessDeniedException("You do not have permission to delete content.");
            }

            // Just delete from database - no file system cleanup needed
            capsuleContentRepository.delete(content);
            capsuleContentRepository.flush();
            logger.info("Successfully deleted content with ID: {}", contentId);

            // Send notification
            notifyContentDeletion(capsuleId, contentId);

        } catch (Exception e) {
            logger.error("Error deleting content with ID: {}. Error: {}", contentId, e.getMessage());
            throw new RuntimeException("Failed to delete content with ID: " + contentId, e);
        }
    }

    private boolean hasEditPermission(UserEntity user, TimeCapsuleEntity capsule) {
        if (capsule.getCreatedBy().getId() == user.getId()) {
            return true;
        }
        return capsuleAccessRepository.existsByUserIdAndCapsuleIdAndRole(
                user.getId(), capsule.getId(), "EDITOR");
    }

    private boolean canDeleteContent(UserEntity user, CapsuleContentEntity content) {
        if (content.getContentUploadedBy().getId() == user.getId()) {
            return true;
        }
        if (content.getCapsule().getCreatedBy().getId() == user.getId()) {
            return true;
        }
        return capsuleAccessRepository.existsByUserIdAndCapsuleIdAndRole(
                user.getId(), content.getCapsule().getId(), "EDITOR");
    }

    @Cacheable(value = CAPSULE_CONTENTS_CACHE, key = "#capsuleId")
    @Override
    public List<CapsuleContentEntity> getContentsByCapsuleId(Long capsuleId, Authentication authentication) {
        try {
            logger.debug("Fetching contents for capsule ID: {} from DB", capsuleId);
            UserEntity user = getAuthenticatedUser(authentication);
            TimeCapsuleEntity capsule = timeCapsuleRepository.findById(capsuleId)
                    .orElseThrow(() -> {
                        logger.warn("Capsule not found with ID: {}", capsuleId);
                        return new EntityNotFoundException("Capsule not found with id " + capsuleId);
                    });

            if (!(capsule.getCreatedBy().getId() == user.getId())) {
                Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, capsule);
                if (accessOpt.isEmpty()) {
                    logger.warn("User {} attempted unauthorized access to capsule ID: {}", user.getUsername(), capsuleId);
                    throw new AccessDeniedException("You do not have permission to view this content.");
                }
            }

            List<CapsuleContentEntity> contents = capsuleContentRepository.findByCapsuleId(capsuleId);
            logger.debug("Found {} contents for capsule ID: {}", contents.size(), capsuleId);
            return contents;
        } catch (Exception e) {
            logger.error("Error fetching contents for capsule ID: {}", capsuleId, e);
            throw e;
        }
    }

    @Cacheable(value = CAPSULE_CONTENTS_CACHE, key = "#capsuleId")
    @Override
    public List<CapsuleContentDetailsDTO> getContentsbyCaps(Long capsuleId, Authentication authentication) {
        try {
            logger.debug("Fetching contents for capsule ID: {} from DB", capsuleId);
            UserEntity user = getAuthenticatedUser(authentication);
            TimeCapsuleEntity capsule = timeCapsuleRepository.findById(capsuleId)
                    .orElseThrow(() -> {
                        logger.warn("Capsule not found with ID: {}", capsuleId);
                        return new EntityNotFoundException("Capsule not found with id " + capsuleId);
                    });

            if (!(capsule.getCreatedBy().getId() == user.getId())) {
                Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, capsule);
                if (accessOpt.isEmpty()) {
                    logger.warn("User {} attempted unauthorized access to capsule ID: {}", user.getUsername(), capsuleId);
                    throw new AccessDeniedException("You do not have permission to view this content.");
                }
            }


            // Fetch contents from the repository



            List<CapsuleContentEntity> contents = capsuleContentRepository.findByCapsuleId(capsuleId);
            logger.debug("Found {} contents for capsule ID: {}", contents.size(), capsuleId);

            // Convert entities to DTOs
            return contents.stream()
                    .map(content -> new CapsuleContentDetailsDTO(
                            content.getId(),
                            content.getContentType(),
                            content.getUploadedAt(),
                            content.getContentUploadedBy().getUsername()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching contents for capsule ID: {}", capsuleId, e);
            throw e;
        }
    }


    @Override
    public List<CapsuleContentDetailsDTO> getPublicCapsuleContents(Long capsuleId) {
        // Find the capsule by ID
        TimeCapsuleEntity capsule = timeCapsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        // Ensure the capsule is public and published
        if (!capsule.isPublic() || !"PUBLISHED".equals(capsule.getStatus())) {
            throw new AccessDeniedException("This capsule is not publicly accessible");
        }

        // Fetch the contents of the capsule
        List<CapsuleContentEntity> contents = capsuleContentRepository.findByCapsuleId(capsuleId);

        // Convert the contents to DTOs
        return contents.stream()
                .map(content -> new CapsuleContentDetailsDTO(
                        content.getId(),
                        content.getContentType(),
                        content.getUploadedAt(),
                        content.getContentUploadedBy().getUsername()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public byte[] getPublicFileContent(Long contentId) throws IOException {
        // Find the content by ID
        CapsuleContentEntity content = capsuleContentRepository.findById(contentId)
                .orElseThrow(() -> new EntityNotFoundException("Content not found"));

        // Ensure the associated capsule is public and published
        TimeCapsuleEntity capsule = content.getCapsule();
        if (!capsule.isPublic() || !"PUBLISHED".equals(capsule.getStatus())) {
            throw new AccessDeniedException("This content is not publicly accessible");
        }

        // Return the file data
        return content.getFileData();
    }
    @Override
    public byte[] getFileContent(Long id, Authentication authentication) throws IOException {
        try {
            logger.debug("Fetching file content for ID: {}", id);
            UserEntity user = getAuthenticatedUser(authentication);
            CapsuleContentEntity content = capsuleContentRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Content not found with ID: {}", id);
                        return new EntityNotFoundException("Content not found with id " + id);
                    });

            TimeCapsuleEntity capsule = content.getCapsule();
            if (!(capsule.getCreatedBy().getId() == user.getId())) {
                Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, capsule);
                if (accessOpt.isEmpty()) {
                    logger.warn("User {} attempted unauthorized access to content ID: {}", user.getUsername(), id);
                    throw new AccessDeniedException("You do not have permission to view this content.");
                }
            }

            return content.getFileData();  // Directly return the stored byte array
        } catch (Exception e) {
            logger.error("Error reading file content for ID: {}", id, e);
            throw e;
        }
    }

    @CacheEvict(value = {CAPSULE_CONTENTS_CACHE, CONTENT_METADATA_CACHE}, key = "#id")
    @Override
    public CapsuleContentEntity updateContent(Long id, MultipartFile file, Authentication authentication) throws IOException {
        UserEntity user = getAuthenticatedUser(authentication);
        CapsuleContentEntity existingContent = capsuleContentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Content not found with id " + id));

        // Check if the user is the uploader (owner)
        boolean isOwner = existingContent.getContentUploadedBy().getId() == user.getId();

        if (!isOwner) {
            // If not the owner, check if user has EDITOR access
            Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, existingContent.getCapsule());

            if (accessOpt.isEmpty() || !("EDITOR".equals(accessOpt.get().getRole()))) {
                throw new AccessDeniedException("You do not have permission to update this content.");
            }
        }

        // Update content data
        existingContent.setFileData(file.getBytes());
        existingContent.setContentType(file.getContentType());
        existingContent.setUploadedAt(new Date());

        CapsuleContentEntity updatedContent = capsuleContentRepository.save(existingContent);

        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("id", updatedContent.getId());
        contentMap.put("contentType", updatedContent.getContentType());
        contentMap.put("uploadedAt", updatedContent.getUploadedAt());
        contentMap.put("uploadedBy", user.getUsername());
        contentMap.put("contentUrl", "/api/capsule-contents/file/" + updatedContent.getId());
        contentMap.put("isImage", updatedContent.getContentType() != null && updatedContent.getContentType().startsWith("image/"));
        contentMap.put("action", "update");

        // Send WebSocket notification
        messagingTemplate.convertAndSend(
                "/topic/capsule-content/updates/" + id,
                contentMap
        );

        return updatedContent;
    }

    @Cacheable(value = CONTENT_METADATA_CACHE, key = "#id")
    @Override
    public Optional<CapsuleContentEntity> getContentById(Long id) {
        logger.debug("Fetching content metadata from DB for ID: {}", id);
        return capsuleContentRepository.findById(id);
    }

    private CapsuleAccessEntity getCapsuleAccess(UserEntity user, TimeCapsuleEntity capsule) {
        return capsuleAccessRepository.findByUserAndCapsule(user, capsule)
                .orElseThrow(() -> new AccessDeniedException("You do not have access to this capsule."));
    }

    private void notifyContentUpdate(Long capsuleId, CapsuleContentEntity content, String action) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", content.getId());
        payload.put("contentType", content.getContentType());
        payload.put("action", action);
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("contentUrl", "/api/capsule-content/" + content.getId() + "/download");
        payload.put("isImage", content.getContentType() != null && content.getContentType().startsWith("image/"));
        logger.info("Capsule Updates Notified");
        messagingTemplate.convertAndSend(
                "/topic/capsule-content/updates/" + capsuleId,
                payload
        );
    }

    private void notifyContentDeletion(Long capsuleId, Long contentId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", "delete");
            payload.put("contentId", contentId);
            payload.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend(
                    "/topic/capsule-content/updates/" + capsuleId,
                    payload
            );
        } catch (Exception e) {
            logger.error("Failed to send deletion notification for content {} in capsule {}", contentId, capsuleId, e);
        }
    }

    @Override
    public void handleConnectionRequest(Long capsuleId, Authentication authentication, String sessionId) {
        try {
            UserEntity user1 = getAuthenticatedUser(authentication);
            String username = user1.getUsername();
            logger.info("Handling WS connection for capsule {} by user {} (session: {})",
                    capsuleId, username, sessionId);

            Authentication auth = (Authentication) authentication;

            // Get the basic capsule contents
            List<CapsuleContentEntity> contentEntities = getContentsByCapsuleId(capsuleId, auth);
            List<Map<String, Object>> renderableContents = new ArrayList<>();

            for (CapsuleContentEntity content : contentEntities) {
                Map<String, Object> contentMap = new HashMap<>();
                contentMap.put("id", content.getId());
                contentMap.put("contentType", content.getContentType());
                contentMap.put("uploadedAt", content.getUploadedAt());
                contentMap.put("uploadedBy", content.getContentUploadedBy().getUsername());

                // Use the download endpoint URL
                contentMap.put("contentUrl", "/api/capsule-content/" + content.getId() + "/download");

                // Add flag for image content
                contentMap.put("isImage", content.getContentType() != null && content.getContentType().startsWith("image/"));

                UserEntity user = getAuthenticatedUser(auth);
                TimeCapsuleEntity capsule = content.getCapsule();

                // Check if user can edit this content
                boolean isOwner = capsule.getCreatedBy().getId() == user.getId();
                boolean canEdit = isOwner;

                if (!isOwner) {
                    Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, capsule);
                    if (accessOpt.isPresent() && "EDITOR".equals(accessOpt.get().getRole())) {
                        canEdit = true;
                    }
                }

                contentMap.put("canEdit", canEdit);
                renderableContents.add(contentMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("capsuleId", capsuleId);
            response.put("contents", renderableContents);
            response.put("timestamp", System.currentTimeMillis());
            response.put("sessionId", sessionId);

            messagingTemplate.convertAndSendToUser(
                    user1.getUsername(),
                    "/queue/capsule-content/initial",
                    response
            );

            logger.debug("Sent initial content data for capsule {} to session {}", capsuleId, sessionId);
        } catch (Exception e) {
            logger.error("Failed to handle connection request for capsule {}: {}", capsuleId, e.getMessage());
            throw e;
        }
    }
}