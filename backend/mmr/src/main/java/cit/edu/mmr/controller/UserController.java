package cit.edu.mmr.controller;

import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private final OAuth2AuthorizedClientService authorizedClientService;

    public UserController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        logger.debug("Getting authenticated user for username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found for username: {}", username);
                    return new UsernameNotFoundException("User not found");
                });
    }

    // Get user by JWT token
    @GetMapping
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        logger.info("Request received to get current user details");

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthorized access attempt to get current user details");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
            }

            String username = authentication.getName();
            logger.debug("Getting current user for username: {}", username);

            UserEntity currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("User not found for username: {}", username);
                        return new NoSuchElementException("User not found");
                    });

            logger.info("Successfully retrieved user details for: {}", username);
            return ResponseEntity.ok(currentUser);

        } catch (NoSuchElementException e) {
            logger.error("Error getting current user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (Exception e) {
            logger.error("Unexpected error getting current user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve user details: " + e.getMessage());
        }
    }

    // Create a new user (assuming this endpoint is used for account creation for admins)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody UserEntity user) {
        logger.info("Request received to create new user with username: {}", user.getUsername());

        try {
            UserEntity createdUser = userService.insertUserRecord(user);
            logger.info("Successfully created new user with ID: {}", createdUser.getId());
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid user creation request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create user: " + e.getMessage());
        }
    }



    // Update user details including optional profile image
    @PutMapping
    public ResponseEntity<?> updateUser(
            @RequestBody UserEntity newUserDetails,
            @RequestParam(value = "profileImg", required = false) MultipartFile profileImg,
            Authentication authentication) {

        logger.info("Request received to update user details");

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthorized access attempt to update user details");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
            }

            String username = authentication.getName();
            logger.debug("Updating user details for username: {}", username);

            UserEntity currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("User not found for username: {}", username);
                        return new NoSuchElementException("User not found");
                    });

            UserEntity updatedUser = userService.updateUserDetails(currentUser.getId(), newUserDetails, profileImg);
            logger.info("Successfully updated user details for user ID: {}", currentUser.getId());
            return ResponseEntity.ok(updatedUser);

        } catch (NoSuchElementException e) {
            logger.error("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid user update request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            logger.error("Error processing profile image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process profile image: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update user details: " + e.getMessage());
        }
    }

    // Disable user account
    @PatchMapping("/disable")
    public ResponseEntity<?> disableUser(Authentication authentication) {
        logger.info("Request received to disable user account");

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthorized access attempt to disable user account");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
            }

            String username = authentication.getName();
            logger.debug("Disabling account for username: {}", username);

            UserEntity currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("User not found for username: {}", username);
                        return new NoSuchElementException("User not found");
                    });

            String response = userService.disableUser(currentUser.getId());
            logger.info("Successfully disabled account for user ID: {}", currentUser.getId());
            return ResponseEntity.ok(response);

        } catch (NoSuchElementException e) {
            logger.error("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (Exception e) {
            logger.error("Unexpected error disabling user account: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to disable user account: " + e.getMessage());
        }
    }

    @PutMapping(value = "/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfilePicture(
            @RequestParam("profileImg") MultipartFile profileImg,
            Authentication authentication) {

        try {
            // Get authenticated user
            UserEntity currentUser = getAuthenticatedUser(authentication);
            logger.info("Received request to update profile picture for user: {}", currentUser.getUsername());

            UserEntity updatedUser = userService.updateProfilePicture(profileImg, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile picture updated successfully");
            response.put("profilePicture", updatedUser.getProfilePicture());

            logger.info("Profile picture successfully updated for user: {}", currentUser.getUsername());
            return ResponseEntity.ok(response);

        } catch (UsernameNotFoundException e) {
            logger.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for profile picture update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating profile picture: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update profile picture: " + e.getMessage());
        }
    }

    @GetMapping("/profile-picture")
    public ResponseEntity<byte[]> getProfilePicture(Authentication auth) {
        try {
           UserEntity user = getAuthenticatedUser(auth);
            if (user == null || user.getProfilePicture() == null) {
                // Return default profile picture if none exists
                ClassPathResource defaultImage = new ClassPathResource("static/default-profile.png");
                byte[] defaultImageBytes = StreamUtils.copyToByteArray(defaultImage.getInputStream());
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(defaultImageBytes);
            }

            byte[] imageBytes = userService.getProfileImage(user.getProfilePicture());

            String contentType = determineContentType(user.getProfilePicture());

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS)) // Cache for 7 days
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            default:
                return "application/octet-stream";
        }
    }
}