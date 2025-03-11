package cit.edu.mmr.service;


import cit.edu.mmr.dto.ProfileDTO;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    /**
     *
     * Get public profile information for a user
     * @param userId the ID of the user whose profile to retrieve
     * @return ProfileDTO with public user information
     */
    public ProfileDTO getPublicProfile(long userId) {
        UserEntity user = userService.findById(userId);
        if (user == null || !user.isEnabled()) {
            throw new NoSuchElementException("User not found or account is disabled");
        }

        return convertToProfileDTO(user, false);
    }

    /**
     * Get detailed profile information for the currently authenticated user
     * @param userId the ID of the user whose profile to retrieve
     * @return ProfileDTO with detailed user information
     */
    public ProfileDTO getOwnProfile(long userId) {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity currentUser = (UserEntity) auth.getPrincipal();

        // Verify the requested profile belongs to the authenticated user
        if (currentUser.getId() != userId) {
            throw new SecurityException("You can only access your own detailed profile");
        }

        return convertToProfileDTO(currentUser, true);
    }

    /**
     * Convert a UserEntity to a ProfileDTO
     * @param user the user entity to convert
     * @param includePrivateInfo whether to include private information
     * @return ProfileDTO with appropriate user information
     */
    private ProfileDTO convertToProfileDTO(UserEntity user, boolean includePrivateInfo) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setUserId(user.getId());
        profileDTO.setUsername(user.getUsername());
        profileDTO.setProfilePicture(user.getProfilePicture());
        profileDTO.setCreatedAt(user.getCreatedAt());

        // Include private information only for the user's own profile
        if (includePrivateInfo) {
            profileDTO.setEmail(user.getEmail());
            profileDTO.setRole(user.getRole());
            profileDTO.setOauthUser(user.isOauthUser());
        }

        return profileDTO;
    }
}
