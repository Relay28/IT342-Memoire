package cit.edu.mmr.controller;


import cit.edu.mmr.dto.TimeCapsuleDTO;
import cit.edu.mmr.service.TimeCapsuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/api/timecapsules")
@CrossOrigin(origins = "*")
public class TimeCapsuleController {

    @Autowired
    private TimeCapsuleService timeCapsuleService;

    @PostMapping
    public ResponseEntity<TimeCapsuleDTO> createTimeCapsule(
            @RequestBody TimeCapsuleDTO timeCapsuleDTO,
            @RequestParam Long userId) {
        try {
            TimeCapsuleDTO created = timeCapsuleService.createTimeCapsule(timeCapsuleDTO, userId);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeCapsuleDTO> getTimeCapsule(@PathVariable Long id) {
        try {
            TimeCapsuleDTO capsule = timeCapsuleService.getTimeCapsule(id);
            return ResponseEntity.ok(capsule);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TimeCapsuleDTO>> getUserTimeCapsules(@PathVariable Long userId) {
        try {
            List<TimeCapsuleDTO> capsules = timeCapsuleService.getUserTimeCapsules(userId);
            return ResponseEntity.ok(capsules);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<Page<TimeCapsuleDTO>> getAllTimeCapsules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        try {
            Sort.Direction direction = Sort.Direction.fromString(sortDirection);
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<TimeCapsuleDTO> capsules = timeCapsuleService.getAllTimeCapsules(pageRequest);
            return ResponseEntity.ok(capsules);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeCapsuleDTO> updateTimeCapsule(
            @PathVariable Long id,
            @RequestBody TimeCapsuleDTO timeCapsuleDTO) {
        try {
            TimeCapsuleDTO updated = timeCapsuleService.updateTimeCapsule(id, timeCapsuleDTO);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("deleteCapsule/{id}")
    public ResponseEntity<Void> deleteTimeCapsule(@PathVariable Long id) {
        try {
            timeCapsuleService.deleteTimeCapsule(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{id}/lock")
    public ResponseEntity<Void> lockTimeCapsule(@PathVariable Long id) {
        try {
            timeCapsuleService.lockTimeCapsule(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{id}/unlock")
    public ResponseEntity<Void> unlockTimeCapsule(@PathVariable Long id) {
        try {
            timeCapsuleService.unlockTimeCapsule(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}