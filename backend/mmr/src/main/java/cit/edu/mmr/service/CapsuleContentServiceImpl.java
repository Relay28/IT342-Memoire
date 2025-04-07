package cit.edu.mmr.service;

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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;

import java.util.Objects;

@Service
@Transactional
public class CapsuleContentServiceImpl implements CapsuleContentService {

    private static final String CONTENT_METADATA_CACHE = "contentMetadata";
    private static final String CAPSULE_CONTENTS_CACHE = "capsuleContents";
    private final CapsuleContentRepository capsuleContentRepository;
    private final TimeCapsuleRepository timeCapsuleRepository;
    private final CapsuleAccessRepository capsuleAccessRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(CapsuleContentServiceImpl.class);
    private static final String UPLOAD_DIR = "uploads/";

    public CapsuleContentServiceImpl(CapsuleContentRepository capsuleContentRepository,
                                     TimeCapsuleRepository timeCapsuleRepository,
                                     CapsuleAccessRepository capsuleAccessRepository,
                                     UserRepository userRepository) {
        this.capsuleContentRepository = capsuleContentRepository;
        this.timeCapsuleRepository = timeCapsuleRepository;
        this.capsuleAccessRepository = capsuleAccessRepository;
        this.userRepository = userRepository;
    }
    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    @CacheEvict(value = {CAPSULE_CONTENTS_CACHE, CONTENT_METADATA_CACHE}, allEntries = true)
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

            boolean isOwner = capsule.getCreatedBy().getId()==(user.getId());
            if (!isOwner) {
                Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, capsule);
                if (accessOpt.isEmpty() || !"EDITOR".equals(accessOpt.get().getRole())) {
                    logger.warn("User {} attempted unauthorized upload to capsule ID: {}", user.getUsername(), capsuleId);
                    throw new AccessDeniedException("You do not have permission to upload content.");
                }
            }

            String filePath = saveFile(file);
            logger.debug("File saved successfully at path: {}", filePath);

            CapsuleContentEntity content = new CapsuleContentEntity();
            content.setCapsule(capsule);
            content.setContentUploadedBy(user);
            content.setFilePath(filePath);
            content.setContentType(file.getContentType());
            content.setUploadedAt(new Date());

            List<CapsuleContentEntity> caps = capsule.getContents();
            caps.add(content);
            capsule.setContents(caps);

            CapsuleContentEntity savedContent = capsuleContentRepository.save(content);
            logger.info("Successfully uploaded content with ID: {} for capsule ID: {}", savedContent.getId(), capsuleId);
            return savedContent;
        } catch (Exception e) {
            logger.error("Error uploading content for capsule ID: {}", capsuleId, e);
            throw e;
        }
    }



    @CacheEvict(value = {CAPSULE_CONTENTS_CACHE, CONTENT_METADATA_CACHE}, allEntries = true)
    @Override
    public void deleteContent(Long id, Authentication authentication) {
        try {
            logger.debug("Starting content deletion for ID: {}", id);
            UserEntity user = getAuthenticatedUser(authentication);
            CapsuleContentEntity content = capsuleContentRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Content not found with ID: {}", id);
                        return new EntityNotFoundException("Content not found with id " + id);
                    });

            TimeCapsuleEntity capsule = content.getCapsule();
            boolean isUploader = content.getContentUploadedBy().getId()==(user.getId());
            boolean isOwner = capsule.getCreatedBy().getId()==(user.getId());

            if (!isUploader && !isOwner) {
                Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, capsule);
                if (accessOpt.isEmpty() || !"EDITOR".equals(accessOpt.get().getRole())) {
                    logger.warn("User {} attempted unauthorized deletion of content ID: {}", user.getUsername(), id);
                    throw new AccessDeniedException("You do not have permission to delete content.");
                }
            }

            try {
                Files.deleteIfExists(Path.of(content.getFilePath()));
                logger.debug("File deleted successfully from path: {}", content.getFilePath());
            } catch (IOException e) {
                logger.error("Failed to delete file at path: {}", content.getFilePath(), e);
                throw new RuntimeException("Failed to delete file: " + content.getFilePath());
            }

            capsuleContentRepository.delete(content);
            logger.info("Successfully deleted content with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting content with ID: {}", id, e);
            throw e;
        }
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

            if (!(capsule.getCreatedBy().getId() ==(user.getId()))) {
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
            if (!(capsule.getCreatedBy().getId() ==(user.getId()))) {
                Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, capsule);
                if (accessOpt.isEmpty()) {
                    logger.warn("User {} attempted unauthorized access to content ID: {}", user.getUsername(), id);
                    throw new AccessDeniedException("You do not have permission to view this content.");
                }
            }

            byte[] fileBytes = Files.readAllBytes(Path.of(content.getFilePath()));
            logger.debug("Successfully read file content for ID: {}", id);
            return fileBytes;
        } catch (Exception e) {
            logger.error("Error reading file content for ID: {}", id, e);
            throw e;
        }
    }


    @CacheEvict(value = {CAPSULE_CONTENTS_CACHE, CONTENT_METADATA_CACHE}, key = "#id")
    @Override
    public CapsuleContentEntity updateContent(Long id, MultipartFile file,Authentication authentication) throws IOException {
        UserEntity user = getAuthenticatedUser(authentication);
        CapsuleContentEntity existingContent = capsuleContentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Content not found with id " + id));

        // 🔹 Check if the user is the uploader (owner)
        boolean isOwner = existingContent.getContentUploadedBy().getId()==(user.getId());

        if (!isOwner) {
            // 🔹 If not the owner, check if user has EDITOR access
            Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, existingContent.getCapsule());

            if (accessOpt.isEmpty() || !("EDITOR" ==(accessOpt.get().getRole()))) {
                throw new AccessDeniedException("You do not have permission to update this content.");
            }
        }

        // Delete old file
        Files.deleteIfExists(Path.of(existingContent.getFilePath()));

        // Save new file
        String newFilePath = saveFile(file);
        existingContent.setFilePath(newFilePath);
        existingContent.setContentType(file.getContentType());

        return capsuleContentRepository.save(existingContent);
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


    private String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Path.of(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }
}
