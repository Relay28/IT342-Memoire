package cit.edu.mmr.service.serviceInterfaces;

import cit.edu.mmr.entity.CapsuleContentEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CapsuleContentService {

    CapsuleContentEntity uploadContent(Long capsuleId, MultipartFile file, Authentication authentication) throws IOException;

    CapsuleContentEntity updateContent(Long id, MultipartFile file,Authentication authentication) throws IOException;

    void deleteContent(Long id,Authentication authentication);

    List<Map<String, Object>> getRenderableContentsByCapsuleId(Long capsuleId, Authentication authentication);

    Optional<CapsuleContentEntity> getContentById(Long id);

    List<CapsuleContentEntity> getContentsByCapsuleId(Long capsuleId,Authentication authentication);

    byte[] getFileContent(Long id,Authentication authentication) throws IOException;



    void handleConnectionRequest(Long capsuleId, Authentication authentication, String sessionId);
}
