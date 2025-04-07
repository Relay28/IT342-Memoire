package cit.edu.mmr.controller;

import cit.edu.mmr.dto.ErrorResponse;
import cit.edu.mmr.dto.ProfileDTO;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.exception.exceptions.DisabledAccountException;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.ProfileService;
import cit.edu.mmr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    /**
     * Get public profile information for any user
     */
    @GetMapping("/view/{userId}")
    public ResponseEntity<ProfileDTO> getPublicProfile(@PathVariable long userId) {
        logger.info("Received request to view public profile for user ID: {}", userId);

        try {
            ProfileDTO profile = profileService.getPublicProfile(userId);
            logger.info("Successfully retrieved public profile for user ID: {}", userId);
            return ResponseEntity.ok(profile);
        } catch (NoSuchElementException | DisabledAccountException e) {
            logger.warn("Failed to retrieve public profile: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving public profile for user ID {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while retrieving the profile", e);
        }
    }

    /**
     * Endpoint to retrieve the authenticated user's own profile
     * @return ProfileDTO with the authenticated user's details
     */
    @GetMapping("/me")
    public ResponseEntity<?> getOwnProfile() {
        logger.info("Received request to view own profile");

        try {
            ProfileDTO profileDTO = profileService.getOwnProfile();
            logger.info("Successfully retrieved own profile");
            return ResponseEntity.ok(profileDTO);
        } catch (SecurityException ex) {
            logger.warn("Unauthorized access attempt to own profile: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Authentication required"));
        } catch (NoSuchElementException ex) {
            logger.warn("Profile not found for authenticated user: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Profile not found"));
        } catch (Exception ex) {
            logger.error("Unexpected error retrieving own profile: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while retrieving your profile"));
        }
    }

    /**
     * Get detailed profile information for a specific user (admin only)
     */
    @GetMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDetailedProfileAsAdmin(@PathVariable long userId) {
        logger.info("Admin request to view detailed profile for user ID: {}", userId);

        try {
            // For admin users, we can provide the detailed profile of any user
            UserEntity user = userService.findById(userId);
            if (user == null) {
                logger.warn("Admin attempted to view non-existent user with ID: {}", userId);
                throw new NoSuchElementException("User not found");
            }

            ProfileDTO profileDTO = new ProfileDTO();
            profileDTO.setUserId(user.getId());
            profileDTO.setUsername(user.getUsername());
            profileDTO.setEmail(user.getEmail());
            profileDTO.setProfilePicture(user.getProfilePicture());
            profileDTO.setCreatedAt(user.getCreatedAt());
            profileDTO.setRole(user.getRole());
            profileDTO.setOauthUser(user.isOauthUser());

            logger.info("Admin successfully retrieved detailed profile for user ID: {}", userId);
            return ResponseEntity.ok(profileDTO);
        } catch (NoSuchElementException e) {
            logger.warn("Admin profile view failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during admin profile view for user ID {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while retrieving the profile", e);
        }
    }


    // Add to ProfileController.java

    /**
     * Search for user profiles by username or email
     * @param query The search query (username or email)
     * @param page Page number (default 0)
     * @param size Page size (default 10)
     * @return List of matching profiles
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchProfiles(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Received search request for query: '{}', page: {}, size: {}", query, page, size);

        try {
            // Sanitize and validate input
            if (query == null || query.trim().isEmpty()) {
                logger.warn("Empty search query provided");
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Search query cannot be empty"));
            }

            // Limit page size to prevent excessive load
            size = Math.min(size, 50);

            // Perform search
            List<ProfileDTO> results = profileService.searchProfiles(query, page, size);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("results", results);
            response.put("page", page);
            response.put("size", results.size());

            logger.info("Search completed successfully for query: '{}', found {} results", query, results.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during profile search for query '{}': {}", query, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred during search"));
        }
    }

}