package cit.edu.mmr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean // Para ni maka Test ta sa Postman
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Disable CSRF protection
        http.csrf(csrf -> csrf.disable())

                // Disable form login and HTTP Basic authentication
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())

                // Allow all requests without authentication
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll()
                );

        return http.build();
    }
}
