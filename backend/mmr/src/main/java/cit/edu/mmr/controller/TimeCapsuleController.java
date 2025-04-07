package cit.edu.mmr.controller;


import cit.edu.mmr.dto.LockRequest;
import cit.edu.mmr.dto.TimeCapsuleDTO;
import cit.edu.mmr.service.TimeCapsuleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
@RestController
@RequestMapping("/api/timecapsules")
//@CrossOrigin(origins = "*")
public class TimeCapsuleController {

    @Autowired
    private TimeCapsuleService timeCapsuleService;

    // Create a time capsule (requires authentication)
    @PostMapping
    public ResponseEntity<TimeCapsuleDTO> createTimeCapsule(
            @RequestBody TimeCapsuleDTO timeCapsuleDTO,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            TimeCapsuleDTO created = timeCapsuleService.createTimeCapsule(timeCapsuleDTO,authentication);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get a specific time capsule
    @GetMapping("/{id}")
    public ResponseEntity<TimeCapsuleDTO> getTimeCapsule(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(timeCapsuleService.getTimeCapsule(id));
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Get time capsules for the authenticated user
    @GetMapping("/user")
    public ResponseEntity<List<TimeCapsuleDTO>> getUserTimeCapsules(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            return ResponseEntity.ok(timeCapsuleService.getUserTimeCapsules(authentication));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all time capsules with pagination
    @GetMapping
    public ResponseEntity<Page<TimeCapsuleDTO>> getAllTimeCapsules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDirection);
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
            return ResponseEntity.ok(timeCapsuleService.getAllTimeCapsules(pageRequest));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update a time capsule (requires authentication)
    @PutMapping("/{id}")
    public ResponseEntity<TimeCapsuleDTO> updateTimeCapsule(
            @PathVariable Long id,
            @RequestBody TimeCapsuleDTO timeCapsuleDTO,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            return ResponseEntity.ok(timeCapsuleService.updateTimeCapsule(id, timeCapsuleDTO,authentication));
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    // Delete a time capsule (requires authentication)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeCapsule(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            timeCapsuleService.deleteTimeCapsule(id,authentication);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    // Lock a time capsule (requires authentication)
    @PatchMapping("/{id}/lock")
    public ResponseEntity<Void> lockTimeCapsule(@PathVariable Long id, @RequestBody @Valid LockRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            timeCapsuleService.lockTimeCapsule(id,request.getOpenDate(),authentication);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    // Unlock a time capsule (requires authentication)
    @PatchMapping("/{id}/unlock")
    public ResponseEntity<Void> unlockTimeCapsule(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            timeCapsuleService.unlockTimeCapsule(id,authentication);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}
