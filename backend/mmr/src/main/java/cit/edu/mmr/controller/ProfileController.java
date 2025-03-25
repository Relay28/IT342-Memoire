package cit.edu.mmr.controller;

import cit.edu.mmr.dto.ProfileDTO;
import cit.edu.mmr.entity.UserEntity;
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

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;


@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserRepository userRepository;
    /**
     * Get public profile information for any user
     */
    @GetMapping("/view/{userId}")
    public ResponseEntity<ProfileDTO> getPublicProfile(@PathVariable long userId) {
        try {
            ProfileDTO profile = profileService.getPublicProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    /**
     * Endpoint to retrieve the authenticated user's own profile
     * @return ProfileDTO with the authenticated user's details
     */
    @GetMapping("/me")
    public ResponseEntity<ProfileDTO> getOwnProfile() {
        try {
            ProfileDTO profileDTO = profileService.getOwnProfile();
            return ResponseEntity.ok(profileDTO);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Get detailed profile information for a specific user (admin only)
     */
    @GetMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileDTO> getDetailedProfileAsAdmin(@PathVariable long userId) {
        try {
            // For admin users, we can provide the detailed profile of any user
            UserService userService = null;
            UserEntity user = userService.findById(userId);
            if (user == null) {
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

            return ResponseEntity.ok(profileDTO);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleSecurityException(SecurityException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}