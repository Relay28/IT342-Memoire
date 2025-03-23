package cit.edu.mmr.controller;

import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    // Get user by id
    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable long id) {
        Optional<UserEntity> userOptional = Optional.ofNullable(userService.findById(id));
        if (userOptional.isPresent()) {
            return new ResponseEntity<>(userOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("getSub/{sub}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable String sub) {
        Optional<UserEntity> userOptional = Optional.ofNullable(userService.findbyGoogleSub(sub));
        if (userOptional.isPresent()) {
            return new ResponseEntity<>(userOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    // Create a new user
    @PostMapping("/createUser")
    public ResponseEntity<UserEntity> createUser(@RequestBody UserEntity user) {
        try {
            UserEntity createdUser = userService.insertUserRecord(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Update user details including optional profile image
    @PutMapping("/updateUser")
    public ResponseEntity<UserEntity> updateUser(
            @RequestBody UserEntity newUserDetails,
            @RequestParam(value = "profileImg", required = false) MultipartFile profileImg) {
        try {
            // Get the authenticated user's details
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }

            // Fetch the current user from the repository using the authenticated username
            String username = auth.getName();
            UserEntity currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

//            // Ensure the authenticated user is a USER and can only update their own account
//            if (!currentUser.getRole().equals("USER")) {
//                return new ResponseEntity<>(null, HttpStatus.FORBIDDEN); // Only USER role can update their own account
//            }
            long id = currentUser.getId();

            // Update the authenticated user's details
            UserEntity updatedUser = userService.updateUserDetails(currentUser.getId(), newUserDetails, profileImg);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // Disable user account
    @PatchMapping("/{id}/disable")
    public ResponseEntity<String> disableUser(@PathVariable long id) {
        try {
            String response = userService.disableUser(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }

         @GetMapping("/user-info")
        public String getAccessToken(Authentication authentication) {
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                        oauthToken.getAuthorizedClientRegistrationId(),
                        oauthToken.getName()
                );

                if (client != null) {
                    return client.getAccessToken().getTokenValue(); // This is the Google access token
                }
            }
            return "No token found";

    }
}
