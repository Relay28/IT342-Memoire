package cit.edu.mmr.controller;

import cit.edu.mmr.dto.ErrorResponse;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.exception.exceptions.AuthenticationException;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody UserEntity user) {
        try {
            logger.info("Attempting to create new user: {}", user.getUsername());
            UserEntity createdUser = userService.insertUserRecord(user);
            logger.info("User created successfully with ID: {}", createdUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            logger.error("Failed to create user: {}", user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to create user"));
        }
    }

    @PutMapping
    public ResponseEntity<?> updateUser(
            @RequestBody UserEntity newUserDetails,
            @RequestParam(value = "profileImg", required = false) MultipartFile profileImg,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthorized attempt to update user details");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Unauthorized access"));
            }

            String username = authentication.getName();
            logger.info("Updating user details for: {}", username);

            UserEntity currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("User not found: {}", username);
                        return new EntityNotFoundException("User not found");
                    });

            UserEntity updatedUser = userService.updateUserDetails(currentUser.getId(), newUserDetails, profileImg);
            logger.info("User updated successfully: {}", updatedUser.getId());

            return ResponseEntity.ok(updatedUser);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (IOException e) {
            logger.error("Profile image upload failed for user update", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Image upload failed"));
        } catch (Exception e) {
            logger.error("Unexpected error while updating user details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error occurred"));
        }
    }

    @PatchMapping("/disable")
    public ResponseEntity<?> disableUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthenticated user attempted to disable their account");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "User not authenticated"));
            }

            String username = authentication.getName();
            logger.info("Disabling user: {}", username);

            UserEntity currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("User not found for disabling: {}", username);
                        return new EntityNotFoundException("User not found");
                    });

            String result = userService.disableUser(currentUser.getId());
            logger.info("User disabled: {}", currentUser.getId());

            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to disable user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to disable user"));
        }
    }
}
