package cit.edu.mmr.controller;

import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.UserService;
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

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private final OAuth2AuthorizedClientService authorizedClientService;

    public UserController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    // Get user by JWT token
    @GetMapping
    public ResponseEntity<UserEntity> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            UserEntity currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            return new ResponseEntity<>(currentUser, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    // Create a new user (assuming this endpoint is used for account creation for admins)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserEntity> createUser(@RequestBody UserEntity user) {
        try {
            UserEntity createdUser = userService.insertUserRecord(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Update user details including optional profile image
    @PutMapping
    public ResponseEntity<UserEntity> updateUser(
            @RequestBody UserEntity newUserDetails,
            @RequestParam(value = "profileImg", required = false) MultipartFile profileImg,
            Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                UserEntity currentUser = userRepository.findByUsername(username)
                        .orElseThrow(() -> new NoSuchElementException("User not found"));

                UserEntity updatedUser = userService.updateUserDetails(currentUser.getId(), newUserDetails, profileImg);
                return new ResponseEntity<>(updatedUser, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Disable user account
    @PatchMapping("/disable")
    public ResponseEntity<String> disableUser(Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                UserEntity currentUser = userRepository.findByUsername(username)
                        .orElseThrow(() -> new NoSuchElementException("User not found"));

                String response = userService.disableUser(currentUser.getId());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }
}
