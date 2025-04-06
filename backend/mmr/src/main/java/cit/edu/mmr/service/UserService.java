package cit.edu.mmr.service;


import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository urepo;

    @Autowired
    private PasswordEncoder passwordEncoder; //
//    @Autowired
//    private FileStorageService fileStorageService;

    public UserService(){
        super();

    }


    public boolean isUsernameTaken(String username) {
        return urepo.existsByUsername(username);
    }

    public boolean isEmailTaken(String email) {
        return urepo.existsByEmail(email);
    }
    private void saveProfileImage(MultipartFile profileImg, UserEntity user) {
        try {
            String folder = "uploads/profileImages/";
            String filename = System.currentTimeMillis() + "_" + profileImg.getOriginalFilename(); // Avoid duplicate names

            Path path = Paths.get(folder + filename);
            Files.createDirectories(path.getParent());
            Files.write(path, profileImg.getBytes());

            user.setProfilePicture(filename); // Store the filename in the user entity
        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile image: " + e.getMessage(), e);
        }
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


    @CacheEvict(value = {"publicProfiles", "ownProfiles"}, key = "#userId")
    public UserEntity updateUserDetails(long userId, UserEntity newUserDetails, MultipartFile profileImg) throws IOException {
        UserEntity existingUser = urepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // Update only the fields that are provided in the request
        if (newUserDetails.getUsername() != null) {
            existingUser.setUsername(newUserDetails.getUsername());
        }
        if (newUserDetails.getEmail() != null) {
            existingUser.setEmail(newUserDetails.getEmail());
        }
        if (newUserDetails.getBiography() != null) {
            existingUser.setBiography(newUserDetails.getBiography());
        }

        // Handle profile image update
        if (profileImg != null && !profileImg.isEmpty()) {
            saveProfileImage(profileImg,existingUser);

        }

        // Save the updated user
        return urepo.save(existingUser);
    }
    public String disableUser(long id ){
        String msg="";
        UserEntity user = urepo.findById(id).orElseThrow(() -> new NoSuchElementException("User " + id + " not found"));
        user.setActive(false);
        urepo.save(user);
        return "User Account has been Deactivated";

    }


}
