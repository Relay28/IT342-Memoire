package cit.edu.mmr.controller;



import cit.edu.mmr.dto.CapsuleAccessDTO;
import cit.edu.mmr.dto.GrantAccessRequest;
import cit.edu.mmr.dto.UpdateRoleRequest;
import cit.edu.mmr.dto.ErrorResponse;
import cit.edu.mmr.entity.CapsuleAccessEntity;
import cit.edu.mmr.service.CapsuleAccessService;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        CapsuleAccessEntity access = capsuleAccessService.grantAccess(
                request.getCapsuleId(),
                request.getUserId(),
                request.getRole(),
                authentication
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(access, CapsuleAccessDTO.class));
    }

    @PutMapping("/{accessId}/role")
    public ResponseEntity<CapsuleAccessDTO> updateAccessRole(
            @PathVariable Long accessId,
            @Valid @RequestBody UpdateRoleRequest request,
            Authentication authentication) {
        CapsuleAccessEntity updated = capsuleAccessService.updateAccessRole(accessId, request.getNewRole(),authentication);
        return ResponseEntity.ok(modelMapper.map(updated, CapsuleAccessDTO.class));
    }

    @DeleteMapping("/{accessId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> removeAccess(@PathVariable Long accessId,Authentication authentication) {
        capsuleAccessService.removeAccess(accessId,authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{capsuleId}")
    public ResponseEntity<List<CapsuleAccessDTO>> getAccessesByCapsule(@PathVariable Long capsuleId,Authentication authentication) {
        List<CapsuleAccessEntity> accesses = capsuleAccessService.getAccessesByCapsule(capsuleId,authentication);
        List<CapsuleAccessDTO> accessDTOs = accesses.stream()
                .map(access -> modelMapper.map(access, CapsuleAccessDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(accessDTOs);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CapsuleAccessDTO>> getAccessesByUser(@PathVariable Long userId,Authentication auth) {
        List<CapsuleAccessEntity> accesses = capsuleAccessService.getAccessesByUser(userId,auth);
        List<CapsuleAccessDTO> accessDTOs = accesses.stream()
                .map(access -> modelMapper.map(access, CapsuleAccessDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(accessDTOs);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkAccess(
            @RequestParam Long capsuleId,
            @RequestParam String role,Authentication auth) {
        boolean hasAccess = capsuleAccessService.hasAccess( capsuleId, role,auth);
        return ResponseEntity.ok(hasAccess);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
}
