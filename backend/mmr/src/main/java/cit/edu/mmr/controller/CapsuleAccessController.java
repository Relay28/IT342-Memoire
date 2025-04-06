package cit.edu.mmr.controller;



import cit.edu.mmr.dto.CapsuleAccessDTO;
import cit.edu.mmr.dto.GrantAccessRequest;
import cit.edu.mmr.dto.UpdateRoleRequest;
import cit.edu.mmr.dto.ErrorResponse;
import cit.edu.mmr.entity.CapsuleAccessEntity;
import cit.edu.mmr.exception.exceptions.DatabaseOperationException;
import cit.edu.mmr.service.CapsuleAccessService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/capsule-access")
@Validated
public class CapsuleAccessController {
    private static final Logger logger = LoggerFactory.getLogger(CapsuleAccessController.class);

    private final CapsuleAccessService capsuleAccessService;
    private final ModelMapper modelMapper;

    @Autowired
    public CapsuleAccessController(CapsuleAccessService capsuleAccessService, ModelMapper modelMapper) {
        this.capsuleAccessService = capsuleAccessService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CapsuleAccessDTO> grantAccess(@Valid @RequestBody GrantAccessRequest request, Authentication authentication) {
        logger.info("Received request to grant access: capsuleId={}, userId={}, role={}",
                request.getCapsuleId(), request.getUserId(), request.getRole());

        try {
            CapsuleAccessEntity access = capsuleAccessService.grantAccess(
                    request.getCapsuleId(),
                    request.getUserId(),
                    request.getRole(),
                    authentication
            );

            CapsuleAccessDTO accessDTO = modelMapper.map(access, CapsuleAccessDTO.class);
            logger.info("Successfully granted access with ID: {}", access.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(accessDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid grant access request: {}", e.getMessage());
            throw e;
        } catch (EntityNotFoundException e) {
            logger.warn("Entity not found for grant access: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error granting access: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error processing access grant request", e);
        }
    }

    @PutMapping("/{accessId}/role")
    public ResponseEntity<CapsuleAccessDTO> updateAccessRole(
            @PathVariable Long accessId,
            @Valid @RequestBody UpdateRoleRequest request,
            Authentication authentication) {

        logger.info("Received request to update access role: accessId={}, newRole={}",
                accessId, request.getNewRole());

        try {
            CapsuleAccessEntity updated = capsuleAccessService.updateAccessRole(
                    accessId,
                    request.getNewRole(),
                    authentication
            );

            CapsuleAccessDTO updatedDTO = modelMapper.map(updated, CapsuleAccessDTO.class);
            logger.info("Successfully updated access role for ID: {}", accessId);

            return ResponseEntity.ok(updatedDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid update access role request: {}", e.getMessage());
            throw e;
        } catch (EntityNotFoundException e) {
            logger.warn("Access entry not found for update: {}", e.getMessage());
            throw e;
        } catch (AccessDeniedException e) {
            logger.warn("Access denied for role update: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error updating access role: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error processing access role update", e);
        }
    }

    @DeleteMapping("/{accessId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> removeAccess(@PathVariable Long accessId, Authentication authentication) {
        logger.info("Received request to remove access: accessId={}", accessId);

        try {
            capsuleAccessService.removeAccess(accessId, authentication);
            logger.info("Successfully removed access ID: {}", accessId);

            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid remove access request: {}", e.getMessage());
            throw e;
        } catch (EntityNotFoundException e) {
            logger.warn("Access entry not found for removal: {}", e.getMessage());
            throw e;
        } catch (AccessDeniedException e) {
            logger.warn("Access denied for access removal: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error removing access: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error processing access removal request", e);
        }
    }

    @GetMapping("/capsule/{capsuleId}")
    public ResponseEntity<List<CapsuleAccessDTO>> getAccessesByCapsule(
            @PathVariable Long capsuleId,
            Authentication authentication) {

        logger.info("Received request to get accesses by capsule: capsuleId={}", capsuleId);

        try {
            List<CapsuleAccessEntity> accesses = capsuleAccessService.getAccessesByCapsule(capsuleId, authentication);

            List<CapsuleAccessDTO> accessDTOs = accesses.stream()
                    .map(access -> modelMapper.map(access, CapsuleAccessDTO.class))
                    .collect(Collectors.toList());

            logger.info("Retrieved {} access entries for capsule ID: {}", accessDTOs.size(), capsuleId);

            return ResponseEntity.ok(accessDTOs);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid get accesses by capsule request: {}", e.getMessage());
            throw e;
        } catch (EntityNotFoundException e) {
            logger.warn("Capsule not found for access retrieval: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error retrieving capsule accesses: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error retrieving capsule accesses", e);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CapsuleAccessDTO>> getAccessesByUser(
            @PathVariable Long userId,
            Authentication auth) {

        logger.info("Received request to get accesses by user: userId={}", userId);

        try {
            List<CapsuleAccessEntity> accesses = capsuleAccessService.getAccessesByUser(userId, auth);

            List<CapsuleAccessDTO> accessDTOs = accesses.stream()
                    .map(access -> modelMapper.map(access, CapsuleAccessDTO.class))
                    .collect(Collectors.toList());

            logger.info("Retrieved {} access entries for user ID: {}", accessDTOs.size(), userId);

            return ResponseEntity.ok(accessDTOs);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid get accesses by user request: {}", e.getMessage());
            throw e;
        } catch (EntityNotFoundException e) {
            logger.warn("User not found for access retrieval: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error retrieving user accesses: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error retrieving user accesses", e);
        }
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkAccess(
            @RequestParam Long capsuleId,
            @RequestParam String role,
            Authentication auth) {

        logger.info("Received request to check access: capsuleId={}, role={}", capsuleId, role);

        try {
            boolean hasAccess = capsuleAccessService.hasAccess(capsuleId, role, auth);
            logger.info("Access check result for capsule ID {}, role {}: {}", capsuleId, role, hasAccess);

            return ResponseEntity.ok(hasAccess);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid check access request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error checking access: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error checking access", e);
        }
    }

    // Since we're using @ControllerAdvice for global exception handling,
    // we can remove the controller-specific exception handlers.
    // The global handlers will be used instead.
}