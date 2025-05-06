package cit.edu.mmr.service;

import cit.edu.mmr.config.JwtService;
import cit.edu.mmr.dto.AuthenticationRequest;
import cit.edu.mmr.dto.AuthenticationResponse;
import cit.edu.mmr.dto.RegisterRequest;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.exception.exceptions.DatabaseOperationException;
import cit.edu.mmr.exception.exceptions.EmailAlreadyExistsException;
import cit.edu.mmr.exception.exceptions.InvalidCredentialsException;
import cit.edu.mmr.exception.exceptions.UsernameAlreadyExistsException;
import cit.edu.mmr.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;

@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(RegisterRequest request) {
        logger.debug("Processing registration for username: {} and email: {}", request.getUsername(), request.getEmail());

        // Check if username or email is already taken
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.warn("Registration failed: Username '{}' is already taken", request.getUsername());
            throw new UsernameAlreadyExistsException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed: Email '{}' is already registered", request.getEmail());
            throw new EmailAlreadyExistsException("Email is already registered");
        }

            var user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");
        user.setName(request.getName());
        user.setActive(true);
        user.setName(request.getUsername());

        try {
            userRepository.save(user);
            logger.info("User successfully registered with ID: {}", user.getId());
        } catch (Exception e) {
            logger.error("Failed to save new user to database: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Failed to complete registration", e);
        }

        var jwtToken = jwtService.generateToken(
                User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities(user.getRole())
                        .build()
        );

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Cacheable(value = "userAuthentication", key = "#request.username", unless = "#result == null")
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        logger.debug("Cache miss: Authenticating user: {}", request.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed: Bad credentials for username: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.error("User not found after successful authentication: {}", request.getUsername());
                    return new UsernameNotFoundException("User not found");
                });

        var jwtToken = jwtService.generateToken(
                User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities("ROLE_" + user.getRole())
                        .build()
        );

        logger.info("User '{}' authenticated successfully", request.getUsername());

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }


    @Cacheable(value = "userAuthentication", key = "#request.username", unless = "#result == null")
    public AuthenticationResponse authenticateAdmin(AuthenticationRequest request) {
        logger.debug("Cache miss: Authenticating user: {}", request.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed: Bad credentials for username: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.error("User not found after successful authentication: {}", request.getUsername());
                    return new UsernameNotFoundException("User not found");
                });

            try {
                if(!user.getRole().equals("ROLE_ADMIN"))
                     throw new AccessDeniedException("Cannot access Data");
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        var jwtToken = jwtService.generateToken(
                User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities("ROLE_" + user.getRole())
                        .build()
        );

        logger.info("User '{}' authenticated successfully", request.getUsername());

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}