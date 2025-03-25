package cit.edu.mmr.controller;

import cit.edu.mmr.config.JwtService;
import cit.edu.mmr.dto.AuthenticationRequest;
import cit.edu.mmr.dto.AuthenticationResponse;
import cit.edu.mmr.dto.RegisterRequest;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authService;

    private final JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    public AuthenticationController(AuthenticationService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/verify-token")
    public ResponseEntity<AuthenticationResponse> verifyToken(@RequestParam String idToken) {
        // Step 1: Verify Google ID token
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        Map<String, Object> googleUserInfo = new RestTemplate().getForObject(url, Map.class);

        if (googleUserInfo == null || googleUserInfo.get("email") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = (String) googleUserInfo.get("email");
        String username = (String) googleUserInfo.get("name");
        String googleSub = (String) googleUserInfo.get("sub"); // Google's unique user ID
        String profilePicture = (String) googleUserInfo.get("picture"); // User profile picture URL

        // Step 2: Check if user exists, else create one
        Optional<UserEntity> existingUser = userRepository.findByEmail(email);
        UserEntity userEntity;

        if (existingUser.isPresent()) {
            userEntity = existingUser.get();
        } else {
            // Register new user
            userEntity = new UserEntity();
            userEntity.setGoogleSub(googleSub);
            userEntity.setEmail(email);
            userEntity.setUsername(username);
            userEntity.setRole("USER");
            userEntity.setActive(true);
            userEntity.setPassword("N/A"); // OAuth users don't use passwords
            userEntity.setCreatedAt(new Date());
            userEntity.setOauthUser(true);
            userRepository.save(userEntity);
        }

        // Step 3: Generate JWT token
        String jwtToken = jwtService.generateToken(userEntity);

        // Step 4: Construct response
        AuthenticationResponse response = new AuthenticationResponse(
                jwtToken,
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getEmail()
        );

        return ResponseEntity.ok(response);
    }


    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }


    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}