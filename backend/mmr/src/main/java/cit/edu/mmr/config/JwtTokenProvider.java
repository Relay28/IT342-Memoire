package cit.edu.mmr.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtTokenProvider {

    private final String secretKey = "bXlTZWNyZXRLZXkxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXo=";  // Store this securely

    public Authentication getAuthentication(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            String username = claims.getSubject();

            // Ensure username is not null
            if (username == null || username.isEmpty()) {
                System.err.println("Username is null or empty in token claims");
                return null;
            }

            // Create an authentication object
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    username,
                    "",
                    List.of(new SimpleGrantedAuthority("USER"))
            );

            System.out.println("Created authentication for user: " + username);
            return auth;
        } catch (Exception e) {
            System.err.println("Error creating authentication from token: " + e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new AccessDeniedException("Invalid token");
        }
    }
}
