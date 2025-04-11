package cit.edu.mmr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.*;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic","/queue"); // Enable a simple memory-based message broker
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // General notifications endpoint
        registry.addEndpoint("/ws-notifications")
                .setAllowedOriginPatterns("http://localhost:5173")
                .withSockJS();

        // Specific endpoint for capsule content real-time updates
        registry.addEndpoint("/ws-capsule-content")
                .setAllowedOriginPatterns("http://localhost:5173")
                .withSockJS()
                .setSessionCookieNeeded(false)// K
                 .setHeartbeatTime(25000); //

        // Keep existing comments endpoint
        registry.addEndpoint("/ws-comments")
                .setAllowedOriginPatterns("http://localhost:5173")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                // For CONNECT commands, authenticate the user
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Your existing authentication code...
                }
                // For other commands, try to retrieve the user from the session attributes
                else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) ||
                        StompCommand.SEND.equals(accessor.getCommand())) {

                    // Check if accessor already has a user
                    if (accessor.getUser() == null) {
                        // Try to get session attributes
                        Object sessionId = accessor.getSessionAttributes().get("sessionId");
                        Object user = accessor.getSessionAttributes().get("user");

                        // If no session user found, try JWT token again
                        if (user == null) {
                            String token = accessor.getFirstNativeHeader("Authorization");
                            if (token != null && token.startsWith("Bearer ")) {
                                try {
                                    token = token.substring(7).trim();
                                    Authentication auth = jwtTokenProvider.getAuthentication(token);
                                    SecurityContextHolder.getContext().setAuthentication(auth);
                                    accessor.setUser(auth);
                                    System.out.println("Re-authenticated user for " + accessor.getCommand() + ": " + auth.getName());
                                } catch (Exception e) {
                                    System.out.println("Failed to re-authenticate: " + e.getMessage());
                                }
                            }
                        }
                    }

                    // Log the result
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    if (auth == null) {
                        System.out.println("No authentication found for " + accessor.getCommand() + " command");
                    } else {
                        System.out.println("Authentication found for " + accessor.getCommand() + " command: " + auth.getName());
                    }
                }
                return message;
            }
        });

    }



}