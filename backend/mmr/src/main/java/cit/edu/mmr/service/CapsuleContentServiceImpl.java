package cit.edu.mmr.service;

import cit.edu.mmr.entity.CapsuleContentEntity;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.CapsuleContentRepository;
import cit.edu.mmr.repository.TimeCapsuleRepository;
import cit.edu.mmr.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
@Transactional
public class CapsuleContentServiceImpl implements CapsuleContentService {

    private final CapsuleContentRepository capsuleContentRepository;
    private final TimeCapsuleRepository timeCapsuleRepository;
    private final UserRepository userRepository;

    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    public CapsuleContentServiceImpl(CapsuleContentRepository capsuleContentRepository,
                                     TimeCapsuleRepository timeCapsuleRepository,
                                     UserRepository userRepository) {
        this.capsuleContentRepository = capsuleContentRepository;
        this.timeCapsuleRepository = timeCapsuleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CapsuleContentEntity uploadContent(Long capsuleId, Long userId, MultipartFile file) throws IOException {
        TimeCapsuleEntity capsule = timeCapsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new EntityNotFoundException("Capsule not found with id " + capsuleId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + userId));

        String filePath = saveFile(file);

        CapsuleContentEntity content = new CapsuleContentEntity();
        content.setCapsule(capsule);
        content.setContentUploadedBy(user);
        content.setFilePath(filePath);
        content.setContentType(file.getContentType());
        content.setUploadedAt(new Date());

        return capsuleContentRepository.save(content);
    }

    @Override
    public CapsuleContentEntity updateContent(Long id, MultipartFile file) throws IOException {
        CapsuleContentEntity existingContent = capsuleContentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Content not found with id " + id));

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
        CapsuleContentEntity content = capsuleContentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Content not found with id " + id));

        // Delete the stored file
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
        return capsuleContentRepository.findByCapsuleId(capsuleId);
    }

    @Override
    public byte[] getFileContent(Long id) throws IOException {
        CapsuleContentEntity content = capsuleContentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Content not found with id " + id));

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
}
