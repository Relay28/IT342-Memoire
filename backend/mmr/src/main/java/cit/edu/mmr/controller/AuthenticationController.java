package cit.edu.mmr.controller;

import cit.edu.mmr.config.JwtService;
import cit.edu.mmr.dto.AuthenticationRequest;
import cit.edu.mmr.dto.AuthenticationResponse;
import cit.edu.mmr.dto.RegisterRequest;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.exception.exceptions.AuthenticationException;
import cit.edu.mmr.exception.exceptions.RegistrationException;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.AuthenticationService;
import cit.edu.mmr.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.bind.annotation.CrossOrigin;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationService authService;
    private final JwtService jwtService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;


    public AuthenticationController(AuthenticationService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/verify-token")
    public ResponseEntity<AuthenticationResponse> verifyToken(@RequestParam String idToken) {
        logger.info("Attempting to verify Google OAuth token");

        try {
            // Step 1: Verify Google ID token
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            Map<String, Object> googleUserInfo = new RestTemplate().getForObject(url, Map.class);

            if (googleUserInfo == null || googleUserInfo.get("email") == null) {
                logger.warn("Invalid or expired Google token received");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String email = (String) googleUserInfo.get("email");
            String username = (String) googleUserInfo.get("name");
            String profilePicture = (String) googleUserInfo.get("picture");

            logger.info("Successfully verified Google token for user: {}", email);

            // Step 2: Check if user exists, else create one
            Optional<UserEntity> existingUser = userRepository.findByEmail(email);
            UserEntity userEntity;

            if (existingUser.isPresent()) {
                userEntity = existingUser.get();
                logger.info("Existing user found with email: {}", email);
            } else {
                // Register new user
                logger.info("Creating new user account for OAuth user: {}", email);
                userEntity = new UserEntity();
                userEntity.setEmail(email);
                userEntity.setUsername(username);
                userEntity.setRole("USER");
                userEntity.setActive(true);
                userEntity.setPassword("N/A"); // OAuth users don't use passwords
                userEntity.setCreatedAt(new Date());
                userEntity.setOauthUser(true);
                userRepository.save(userEntity);
                logger.info("Successfully created new OAuth user account with ID: {}", userEntity.getId());
            }

            // Step 3: Generate JWT token
            String jwtToken = jwtService.generateToken(userEntity);
            logger.debug("JWT token generated successfully");

            // Step 4: Construct response
            AuthenticationResponse response = new AuthenticationResponse(
                    jwtToken,
                    userEntity.getId(),
                    userEntity.getUsername(),
                    userEntity.getEmail(),
                    userEntity.getRole()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing OAuth verification: {}", e.getMessage(), e);
            throw new AuthenticationException("OAuth authentication failed", e);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        logger.info("Processing registration request for username: {}", request.getUsername());
        try {
            AuthenticationResponse response = authService.register(request);
            logger.info("User registered successfully: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Registration failed for username {}: {}", request.getUsername(), e.getMessage(), e);
            throw new RegistrationException("Registration failed: " + e.getMessage(), e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        logger.info("Processing authentication request for username: {}", request.getUsername());
        try {
            AuthenticationResponse response = authService.authenticate(request);
            logger.info("User authenticated successfully: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Authentication failed for username {}: {}", request.getUsername(), e.getMessage(), e);
            throw new AuthenticationException("Authentication failed", e);
        }
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AuthenticationResponse> authenticateAdmin(@RequestBody AuthenticationRequest request) {
        logger.info("Processing authentication request for username: {}", request.getUsername());
        try {
            AuthenticationResponse response = authService.authenticateAdmin(request);
            logger.info("User authenticated successfully: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Authentication failed for username {}: {}", request.getUsername(), e.getMessage(), e);
            throw new AuthenticationException("Authentication failed", e);
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(jwt);
        }
        return ResponseEntity.ok("Logout successful");
    }
}