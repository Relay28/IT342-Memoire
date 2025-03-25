package cit.edu.mmr.controller;

import cit.edu.mmr.entity.CapsuleContentEntity;
import cit.edu.mmr.service.CapsuleContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/capsule-content") // 🔹 Ensures authentication for all requests
public class CapsuleContentController {

    private final CapsuleContentService capsuleContentService;

    public CapsuleContentController(CapsuleContentService capsuleContentService) {
        this.capsuleContentService = capsuleContentService;
    }

    @PostMapping("/{capsuleId}/upload")
    public ResponseEntity<CapsuleContentEntity> uploadContent(
            @PathVariable Long capsuleId,
            @RequestParam("file") MultipartFile file) {
        try {
            CapsuleContentEntity savedContent = capsuleContentService.uploadContent(capsuleId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedContent);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadContent(@PathVariable Long id) {
        try {
            byte[] fileContent = capsuleContentService.getFileContent(id);
            CapsuleContentEntity content = capsuleContentService.getContentById(id)
                    .orElseThrow(() -> new RuntimeException("Content not found"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(content.getContentType()));
            headers.setContentDispositionFormData("attachment", "file");

            return ResponseEntity.ok().headers(headers).body(fileContent);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }




    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        capsuleContentService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{capsuleId}")
    public ResponseEntity<List<CapsuleContentEntity>> getContentsByCapsule(@PathVariable Long capsuleId) {
        return ResponseEntity.ok(capsuleContentService.getContentsByCapsuleId(capsuleId));
    }


}
