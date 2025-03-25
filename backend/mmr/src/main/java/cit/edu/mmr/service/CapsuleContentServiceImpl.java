package cit.edu.mmr.service;

import cit.edu.mmr.entity.CapsuleAccessEntity;
import cit.edu.mmr.entity.CapsuleContentEntity;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.CapsuleAccessRepository;
import cit.edu.mmr.repository.CapsuleContentRepository;
import cit.edu.mmr.repository.TimeCapsuleRepository;
import cit.edu.mmr.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class CapsuleContentServiceImpl implements CapsuleContentService {

    private final CapsuleContentRepository capsuleContentRepository;
    private final TimeCapsuleRepository timeCapsuleRepository;
    private final CapsuleAccessRepository capsuleAccessRepository;
    private final UserRepository userRepository;
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
    private UserEntity getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("USERNAME "+username);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return user;
    }

    @Override
    public CapsuleContentEntity uploadContent(Long capsuleId, MultipartFile file) throws IOException {
        UserEntity user = getAuthenticatedUser();
        TimeCapsuleEntity capsule = timeCapsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new EntityNotFoundException("Capsule not found with id " + capsuleId));

        // 🔹 Check if the user is the owner (creator) of the capsule
        boolean isOwner = capsule.getCreatedBy().getId()==(user.getId());

        if (!isOwner) {
            // 🔹 If not the owner, check if user has EDITOR access
            Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, capsule);

            if (accessOpt.isEmpty() || !"EDITOR".equals(accessOpt.get().getRole())) {
                throw new AccessDeniedException("You do not have permission to upload content.");
            }
        }

        String filePath = saveFile(file);

        CapsuleContentEntity content = new CapsuleContentEntity();
        content.setCapsule(capsule);
        content.setContentUploadedBy(user);
        content.setFilePath(filePath);
        content.setContentType(file.getContentType());
        content.setUploadedAt(new Date());
        List<CapsuleContentEntity> caps = capsule.getContents();
        caps.add(content);
        capsule.setContents(caps);

        return capsuleContentRepository.save(content);
    }

    @Override
    public CapsuleContentEntity updateContent(Long id, MultipartFile file) throws IOException {
        UserEntity user = getAuthenticatedUser();
        CapsuleContentEntity existingContent = capsuleContentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Content not found with id " + id));

        // 🔹 Check if the user is the uploader (owner)
        boolean isOwner = existingContent.getContentUploadedBy().getId()==(user.getId());

        if (!isOwner) {
            // 🔹 If not the owner, check if user has EDITOR access
            Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, existingContent.getCapsule());

            if (accessOpt.isEmpty() || !"EDITOR".equals(accessOpt.get().getRole())) {
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


    @Override
    public void deleteContent(Long id) {
        UserEntity user = getAuthenticatedUser();
        CapsuleContentEntity content = capsuleContentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Content not found with id " + id));

        TimeCapsuleEntity capsule = content.getCapsule();

        // 🔹 Check if the user is the uploader
        boolean isUploader = content.getContentUploadedBy().getId()==user.getId();

        // 🔹 Check if the user is the capsule owner
        boolean isOwner = capsule.getCreatedBy().getId()==user.getId();

        if (!isUploader && !isOwner) {
            // 🔹 If not the uploader or owner, check if user is an EDITOR
            Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, capsule);

            if (accessOpt.isEmpty() || !"EDITOR".equals(accessOpt.get().getRole())) {
                throw new AccessDeniedException("You do not have permission to delete content.");
            }
        }

        // 🔹 Delete the stored file
        try {
            Files.deleteIfExists(Path.of(content.getFilePath()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + content.getFilePath());
        }

        capsuleContentRepository.delete(content);
    }

    @Override
    public Optional<CapsuleContentEntity> getContentById(Long id) {
        return capsuleContentRepository.findById(id);
    }

    @Override
    public List<CapsuleContentEntity> getContentsByCapsuleId(Long capsuleId) {
        UserEntity user = getAuthenticatedUser();
        TimeCapsuleEntity capsule = timeCapsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new EntityNotFoundException("Capsule not found with id " + capsuleId));

        // 🔹 Check if the user is the capsule owner
        if (capsule.getCreatedBy().getId()== user.getId()) {
            return capsuleContentRepository.findByCapsuleId(capsuleId);
        }

        // 🔹 Check if the user has access (EDITOR or VIEWER)
        Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, capsule);
        if (accessOpt.isEmpty()) {
            throw new AccessDeniedException("You do not have permission to view this content.");
        }

        return capsuleContentRepository.findByCapsuleId(capsuleId);
    }


    @Override
    public byte[] getFileContent(Long id) throws IOException {
        UserEntity user = getAuthenticatedUser();
        CapsuleContentEntity content = capsuleContentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Content not found with id " + id));

        TimeCapsuleEntity capsule = content.getCapsule();

        // 🔹 Check if the user is the capsule owner
        if (Objects.equals(capsule.getCreatedBy().getId(), user.getId())) {
            return Files.readAllBytes(Path.of(content.getFilePath()));
        }

        // 🔹 Check if the user has access (EDITOR or VIEWER)
        Optional<CapsuleAccessEntity> accessOpt = capsuleAccessRepository.findByUserAndCapsule(user, capsule);
        if (accessOpt.isEmpty()) {
            throw new AccessDeniedException("You do not have permission to view this content.");
        }

        return Files.readAllBytes(Path.of(content.getFilePath()));
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



    private CapsuleAccessEntity getCapsuleAccess(UserEntity user, TimeCapsuleEntity capsule) {
        return capsuleAccessRepository.findByUserAndCapsule(user, capsule)
                .orElseThrow(() -> new AccessDeniedException("You do not have access to this capsule."));
    }
}
