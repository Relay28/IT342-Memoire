package cit.edu.mmr.service;


import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
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

    public UserEntity putUserDetails(long id, UserEntity newUserDetails, MultipartFile profileImg) {
        // Find the existing user or throw an exception if not found
        UserEntity user = urepo.findById(id).orElseThrow(() -> new NoSuchElementException("User " + id + " not found"));

        // Update user details from newUserDetails


        if (newUserDetails.getUsername() != null) {
            user.setUsername(newUserDetails.getUsername());
        }
        if (newUserDetails.getPassword() != null) {
            user.setPassword(newUserDetails.getPassword());
        }
        if (newUserDetails.getEmail() != null) {
            user.setEmail(newUserDetails.getEmail());
        }

        // Save profile image if provided
        if (profileImg != null && !profileImg.isEmpty()) {
            saveProfileImage(profileImg, user);
        }

        // Save the updated user entity to the repository
        return urepo.save(user);
    }


    public String disableUser(long id ){
        String msg="";
        UserEntity user = urepo.findById(id).orElseThrow(() -> new NoSuchElementException("User " + id + " not found"));
        user.setActive(false);
        urepo.save(user);
        return "User Account has been Deactivated";

    }
}
