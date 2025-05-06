package cit.edu.mmr.controller;

import cit.edu.mmr.dto.ChangePasswordRequest;
import cit.edu.mmr.dto.UserDTO;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
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

    // Get current authenticated user
    // Get current authenticated user
    @GetMapping
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
            }
            UserEntity currentUser = getAuthenticatedUser(authentication);

            // Convert to DTO to avoid exposing sensitive information
            UserDTO userDTO = new UserDTO(
                    currentUser.getId(),
                    currentUser.getUsername(),
                    currentUser.getEmail(),
                    currentUser.getProfilePictureData(),
                    currentUser.getRole(),
                    currentUser.getBiography(),
                    currentUser.isActive(),
                    currentUser.isOauthUser(),
                    currentUser.getCreatedAt()
            );

            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            logger.error("Error retrieving current user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve user: " + e.getMessage());
        }
    }

    // Admin creates a new user
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody UserEntity user) {
        try {
            UserEntity createdUser = userService.insertUserRecord(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create user: " + e.getMessage());
        }
    }

    // Admin dashboard: view all users
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<UserDTO> getAllUsers(Authentication auth) {
        return userService.getAllUsers(auth).stream()
                .map(user -> new UserDTO(
                        user.getId(), user.getUsername(), user.getEmail(),
                        user.getProfilePictureData(), user.getRole(),
                        user.getBiography(), user.isActive(), user.isOauthUser(), user.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // Update user details (WITHOUT updating profile picture here)
    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody UserEntity newUserDetails, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
            }
            UserEntity currentUser = getAuthenticatedUser(authentication);
            UserDTO updatedUser = userService.updateUserDetails(currentUser.getId(), newUserDetails, null);
            return ResponseEntity.ok(updatedUser);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update profile image: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update user: " + e.getMessage());
        }
    }

    // Upload or update profile picture separately
    @PutMapping(value = "/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfilePicture(
            @RequestParam("profileImg") MultipartFile profileImg,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
            }
            UserEntity currentUser = getAuthenticatedUser(authentication);
            UserEntity updatedUser = userService.updateProfilePicture(profileImg, currentUser);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update profile picture: " + e.getMessage());
        }
    }

    // Serve profile image for a user (binary download)
    @GetMapping(value = "/profile-picture/{userId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable Long userId) {
        try {
            byte[] imageData = userService.getProfileImageFromDatabase(userId);
            return ResponseEntity.ok()
                    .cacheControl(org.springframework.http.CacheControl.maxAge(30, TimeUnit.MINUTES))
                    .body(imageData);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Disable user (deactivate account)
    @PatchMapping("/disable")
    public ResponseEntity<?> disableUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
            }
            UserEntity currentUser = getAuthenticatedUser(authentication);
            String response = userService.disableUser(currentUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to disable user: " + e.getMessage());
        }
    }

    // Change password endpoint
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
            }
            userService.changePassword(authentication.getName(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password successfully changed");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to change password: " + e.getMessage());
        }
    }
}
