package cit.edu.mmr.security;

import cit.edu.mmr.config.JwtService;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private JwtService jwtService;
    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegate to the default implementation for loading the user
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // Extract attributes from OAuth2User
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Extract required fields from OAuth provider
        String email = (String) attributes.get("email");
        String username = (String) attributes.get("name"); // Adjust if needed
       // String googleSub = oAuth2User.getAttribute("sub");

        // Check if user already exists
        Optional<UserEntity> userOptional = userRepository.findByEmail(email);
        UserEntity userEntity;
        if (userOptional.isPresent()) {
            // Update user details if necessary
            userEntity = userOptional.get();
            userEntity.setUsername(username);
        } else {
            // Create a new user
            userEntity = new UserEntity();
            //if(!googleSub.isEmpty())

            userEntity.setEmail(email);
            userEntity.setUsername(username);
            userEntity.setRole("USER"); // Set a default role
            userEntity.setActive(true);
            userEntity.setPassword("N/A"); // Not needed for OAuth2 users
            userEntity.setCreatedAt(new Date());
            userEntity.setOauthUser(true);
        }

        // Save the user entity before generating the JWT token
        userRepository.save(userEntity);

        // Generate JWT token
        String jwtToken = jwtService.generateToken(userEntity);

        // Add token to attributes for retrieval
        Map<String, Object> updatedAttributes = new HashMap<>(oAuth2User.getAttributes());
        updatedAttributes.put("jwtToken", jwtToken);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(userEntity.getRole())),
                updatedAttributes,
                "email"
        );
    }
}
