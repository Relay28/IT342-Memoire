package cit.edu.mmr.service;


import cit.edu.mmr.dto.ProfileDTO;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.exception.exceptions.DisabledAccountException;
import cit.edu.mmr.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    /**
     * Get public profile information for a user
     * Cache results to improve performance for frequently accessed profiles
     */
    @Cacheable(value = "publicProfiles", key = "#userId", unless = "#result == null")
    public ProfileDTO getPublicProfile(long userId) {
        logger.info("Cache miss: Retrieving public profile for user ID: {}", userId);

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
            throw e;
        }
    }

    /**
     * Get detailed profile information for the currently authenticated user
     * Cache results based on username since this is a common operation
     */
    @Cacheable(value = "ownProfiles", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()",
            unless = "#result == null")
    public ProfileDTO getOwnProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        logger.info("Cache miss: Retrieving authenticated user's own profile for: {}", username);

        if (auth == null || !auth.isAuthenticated()) {
            logger.warn("Unauthorized attempt to access user profile");
            throw new SecurityException("User is not authenticated");
        }

        try {
            UserEntity currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("Authenticated user '{}' not found in database", username);
                        return new NoSuchElementException("User not found");
                    });

            logger.info("Successfully retrieved profile for user: {}", username);
            return convertToProfileDTO(currentUser, true);
        } catch (Exception e) {
            logger.error("Error retrieving own profile: {}", e.getMessage(), e);
            throw e;
        }
    }
    /**
     * Convert a UserEntity to a ProfileDTO
     * @param user the user entity to convert
     * @param includePrivateInfo whether to include private information
     * @return ProfileDTO with appropriate user information
     */


    // In ProfileService.java
    /**
     * Search for user profiles by username or name
     * @param query Search term (username or name)
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of matching ProfileDTOs
     */


    @Cacheable(value = "profileSearches", key = "#query.concat('-').concat(#page).concat('-').concat(#size)")
    public List<ProfileDTO> searchProfiles(String query, int page, int size) {
        logger.info("Searching profiles for query: '{}', page: {}, size: {}", query, page, size);

        // Normalize the query (trim and make lowercase for case-insensitive search)
        String normalizedQuery = query.trim().toLowerCase();

        // Search for users by username or name
        Page<UserEntity> users = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(
                normalizedQuery,
                normalizedQuery,
                PageRequest.of(page, size, Sort.by("username").ascending()));

        // Convert to DTOs
        return users.getContent().stream()
                .filter(UserEntity::isEnabled) // Only include enabled accounts
                .map(user -> convertToProfileDTO(user, false)) // Public profile only
                .collect(Collectors.toList());
    }
    private ProfileDTO convertToProfileDTO(UserEntity user, boolean includePrivateInfo) {
        logger.debug("Converting user entity to ProfileDTO, includePrivateInfo={}", includePrivateInfo);

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setUserId(user.getId());
        profileDTO.setUsername(user.getUsername());
        profileDTO.setProfilePicture(user.getProfilePictureData());
        profileDTO.setCreatedAt(user.getCreatedAt());
        profileDTO.setBiography(user.getBiography());
        profileDTO.setName(user.getName());

        // Include private information only for the user's own profile
        if (includePrivateInfo) {
            profileDTO.setEmail(user.getEmail());
            profileDTO.setRole(user.getRole());
            profileDTO.setOauthUser(user.isOauthUser());
        }

        return profileDTO;
    }
}