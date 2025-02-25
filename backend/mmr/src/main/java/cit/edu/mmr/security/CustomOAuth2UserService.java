package cit.edu.mmr.security;

import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

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

        // Depending on the provider, the keys might differ. For example, Google uses "email", "name", etc.
        String email = (String) attributes.get("email");
        String username = (String) attributes.get("name"); // adjust if needed

        // Check if user already exists in your database
        Optional<UserEntity> userOptional = userRepository.findByEmail(email);
        UserEntity userEntity;
        if (userOptional.isPresent()) {
            // Update user details if necessary
            userEntity = userOptional.get();
            userEntity.setUsername(username);
            // Update other fields if needed
        } else {
            // Create a new user
            userEntity = new UserEntity();
            userEntity.setEmail(email);
            userEntity.setUsername(username);
            userEntity.setRole("USER    "); // Set a default role (or map based on provider scopes)
            userEntity.setActive(true);
            // Set a default or generated password if necessary. Note: for OAuth2 users, password is typically not used.
            userEntity.setPassword("N/A");
            userEntity.setCreatedAt(new Date());
            userEntity.setOauthUser(true);
        }

        // Save the user entity

        userRepository.save(userEntity);

        // You may want to wrap the user details in your own implementation of OAuth2User if you need more control.
        // Here, we create a DefaultOAuth2User with the authorities we want.
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(userEntity.getRole())),
                attributes,
                "email" // Set the key that will be used for the username (this may vary by provider)
        );
    }
}
