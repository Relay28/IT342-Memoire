package cit.edu.mmr.service;

import cit.edu.mmr.entity.CapsuleContentEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface CapsuleContentService {

    CapsuleContentEntity uploadContent(Long capsuleId, Long userId, MultipartFile file) throws IOException;

    CapsuleContentEntity updateContent(Long id, MultipartFile file) throws IOException;

    void deleteContent(Long id);

    Optional<CapsuleContentEntity> getContentById(Long id);

    List<CapsuleContentEntity> getContentsByCapsuleId(Long capsuleId);

    byte[] getFileContent(Long id) throws IOException;
}
