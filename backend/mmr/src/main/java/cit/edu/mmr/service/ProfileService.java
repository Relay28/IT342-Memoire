package cit.edu.mmr.service;


import cit.edu.mmr.dto.ProfileDTO;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.exception.exceptions.DisabledAccountException;
import cit.edu.mmr.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    /**
     * Get public profile information for a user
     * @param userId the ID of the user whose profile to retrieve
     * @return ProfileDTO with public user information
     */
    public ProfileDTO getPublicProfile(long userId) {
        logger.info("Retrieving public profile for user ID: {}", userId);

        try {
            UserEntity user = userService.findById(userId);
            if (user == null) {
                logger.warn("User not found with ID: {}", userId);
                throw new NoSuchElementException("User not found");
            }

            if (!user.isEnabled()) {
                logger.warn("Attempting to access disabled account with ID: {}", userId);
                throw new DisabledAccountException("User account is disabled");
            }

            logger.debug("Successfully retrieved public profile for user ID: {}", userId);
            return convertToProfileDTO(user, false);
        } catch (Exception e) {
            logger.error("Error retrieving public profile for user ID {}: {}", userId, e.getMessage(), e);
            throw e; // Re-throw the exception to be handled by the controller
        }
    }

    /**
     * Get detailed profile information for the currently authenticated user
     * @return ProfileDTO with detailed user information
     */
    public ProfileDTO getOwnProfile() {
        logger.info("Retrieving authenticated user's own profile");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            logger.warn("Unauthorized attempt to access user profile");
            throw new SecurityException("User is not authenticated");
        }

        try {
            // Assuming the principal is the username, fetch the user entity from the repository
            String username = auth.getName();
            logger.debug("Loading profile for authenticated user: {}", username);

            UserEntity currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("Authenticated user '{}' not found in database", username);
                        return new NoSuchElementException("User not found");
                    });

            logger.info("Successfully retrieved profile for user: {}", username);
            return convertToProfileDTO(currentUser, true);
        } catch (Exception e) {
            logger.error("Error retrieving own profile: {}", e.getMessage(), e);
            throw e; // Re-throw the exception to be handled by the controller
        }
    }

    /**
     * Convert a UserEntity to a ProfileDTO
     * @param user the user entity to convert
     * @param includePrivateInfo whether to include private information
     * @return ProfileDTO with appropriate user information
     */
    private ProfileDTO convertToProfileDTO(UserEntity user, boolean includePrivateInfo) {
        logger.debug("Converting user entity to ProfileDTO, includePrivateInfo={}", includePrivateInfo);

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setUserId(user.getId());
        profileDTO.setUsername(user.getUsername());
        profileDTO.setProfilePicture(user.getProfilePicture());
        profileDTO.setCreatedAt(user.getCreatedAt());
        profileDTO.setBiography(user.getBiography());

        // Include private information only for the user's own profile
        if (includePrivateInfo) {
            profileDTO.setEmail(user.getEmail());
            profileDTO.setRole(user.getRole());
            profileDTO.setOauthUser(user.isOauthUser());
        }

        return profileDTO;
    }
}