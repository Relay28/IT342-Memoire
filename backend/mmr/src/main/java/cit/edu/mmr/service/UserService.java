package cit.edu.mmr.service;


import cit.edu.mmr.dto.UserDTO;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import org.apache.catalina.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository urepo;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private PasswordEncoder passwordEncoder; //
//    @Autowired
//    private FileStorageService fileStorageService;

    public UserService(){
        super();

    }

    public byte[] getProfileImage(String filename) {
        try {
            String folder = "uploads/profileImages/";
            Path path = Paths.get(folder + filename);
            if (!Files.exists(path)) {
                throw new FileNotFoundException("Profile image not found: " + filename);
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read profile image: " + e.getMessage(), e);
        }
    }

    public boolean isUsernameTaken(String username) {
        return urepo.existsByUsername(username);
    }

    public boolean isEmailTaken(String email) {
        return urepo.existsByEmail(email);
    }
    private void saveProfileImage(MultipartFile profileImg, UserEntity user) {
        try {
            user.setProfilePictureData(profileImg.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile image to database: " + e.getMessage(), e);
        }
    }

    public byte[] getProfileImageFromDatabase(long userId) {
        UserEntity user = urepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return user.getProfilePictureData();
    }


    public UserEntity findById(long userid){
        Optional<UserEntity> user = urepo.findById(userid);
        return user.orElse(null);

    }

    public UserEntity insertUserRecord(UserEntity user){

        if(user.getUsername()==null|| user.getUsername().isEmpty()){
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if(user.getEmail()==null||user.getEmail().isEmpty()){
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        if(isUsernameTaken(user.getUsername())){
            throw new IllegalArgumentException("Username is Already taken");
        }

        if(isEmailTaken(user.getEmail())){
            throw new IllegalArgumentException("Email is Already Registered");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return urepo.save(user);
    }


    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return urepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    public List<UserEntity> getAllUsers(Authentication auth){
        UserEntity user = getAuthenticatedUser(auth);

            List<UserEntity> list=null;
            try {
                if(!user.getRole().equals("ROLE_ADMIN"))
                  throw new AccessDeniedException("CANNOT ACCESS ENDPOINT");
                else
                   list= urepo.findAll();
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
            return list;
    }

    @CacheEvict(value = {"publicProfiles", "ownProfiles"}, key = "#userId")
    public UserDTO updateUserDetails(long userId, UserEntity newUserDetails, MultipartFile profileImg) throws IOException {
        UserEntity existingUser = urepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // Update only the fields that are provided in the request
        if (newUserDetails.getEmail() != null) {
            existingUser.setEmail(newUserDetails.getEmail());
        }
        if (newUserDetails.getBiography() != null) {
            existingUser.setBiography(newUserDetails.getBiography());
        }
        if (newUserDetails.getName() != null) {
            existingUser.setName(newUserDetails.getName());
        }

        // Handle profile image update
        if (profileImg != null && !profileImg.isEmpty()) {
            saveProfileImage(profileImg, existingUser);
        }

        // Save the updated user
        UserEntity updatedUser = urepo.save(existingUser);

        // Convert to DTO and return
        return convertToDTO(updatedUser);
    }

    /**
     * Updates only the profile picture of a user
     * @param userId ID of the user
     * @param profileImg new profile image
     * @return updated user entity
     */
    @CacheEvict(value = {"publicProfiles", "ownProfiles", "userProfiles"}, key = "#currentUser.id")
    public UserEntity updateProfilePicture(MultipartFile profileImg, UserEntity currentUser) {
        long userId = currentUser.getId();
        logger.info("Updating profile picture for user ID: {}", userId);

        try {
            // Validate image
            if (profileImg == null || profileImg.isEmpty()) {
                logger.warn("Empty profile image provided for user ID: {}", userId);
                // throw new IllegalArgumentException("Profile image cannot be empty");
            }else {

                // Save image
                saveProfileImage(profileImg, currentUser);
                logger.info("Profile picture successfully updated for user ID: {}", userId);

                // Save and return updated user
            }
            return urepo.save(currentUser);
        } catch (Exception e) {
            logger.error("Failed to update profile picture for user ID: {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update profile picture: " + e.getMessage(), e);
        }
    }
    public String disableUser(long id ){
        String msg="";
        UserEntity user = urepo.findById(id).orElseThrow(() -> new NoSuchElementException("User " + id + " not found"));
        user.setActive(false);
        urepo.save(user);
        return "User Account has been Deactivated";

    }

    public void changePassword(String username, String currentPassword, String newPassword) {
        logger.info("Attempting to change password for user: {}", username);

        // Find user
        UserEntity user = urepo.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found with username: {}", username);
                    return new NoSuchElementException("User not found");
                });

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            logger.warn("Current password verification failed for user: {}", username);
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < 8) {
            logger.warn("New password validation failed for user: {}", username);
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        // Check if new password is same as current
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            logger.warn("New password same as current password for user: {}", username);
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));

        // THIS IS CRUCIAL - Make sure you're saving the updated user
        urepo.save(user); // This line must be present
        logger.info("Password successfully changed for user: {}", username);
    }

    private UserDTO convertToDTO(UserEntity user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getProfilePictureData(),
                user.getRole(),
                user.getBiography(),
                user.isActive(),
                user.isOauthUser(),
                user.getCreatedAt()
        );
    }
}
