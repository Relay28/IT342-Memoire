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
    @Autowired
    private AuthTokenHandshakeInterceptor authTokenHandshakeInterceptor;

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
                .addInterceptors(authTokenHandshakeInterceptor)
                .withSockJS();

        // Specific endpoint for capsule content real-time updates
        registry.addEndpoint("/ws-capsule-content")
                .setAllowedOriginPatterns("http://localhost:5173")
                .addInterceptors(authTokenHandshakeInterceptor)
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

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = accessor.getFirstNativeHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7).trim();
                        try {
                            Authentication auth = jwtTokenProvider.getAuthentication(token);
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            accessor.setUser(auth);
                            accessor.getSessionAttributes().put("user", auth);
                            accessor.getSessionAttributes().put("sessionId", accessor.getSessionId());
                            System.out.println("User authenticated at CONNECT: " + auth.getName());
                        } catch (Exception e) {
                            System.out.println("Authentication failed at CONNECT: " + e.getMessage());
                            throw new AccessDeniedException("Invalid token");
                        }
                    } else {
                        System.out.println("No token provided at CONNECT");
                        throw new AccessDeniedException("No Authorization header provided");
                    }
                }

                else if (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    if (accessor.getUser() == null) {
                        Object userAttr = accessor.getSessionAttributes().get("user");
                        if (userAttr instanceof Authentication) {
                            accessor.setUser((Authentication) userAttr);
                            SecurityContextHolder.getContext().setAuthentication((Authentication) userAttr);
                            System.out.println("Reattached user from session for " + accessor.getCommand());
                        } else {
                            String token = accessor.getFirstNativeHeader("Authorization");
                            if (token != null && token.startsWith("Bearer ")) {
                                try {
                                    token = token.substring(7).trim();
                                    Authentication auth = jwtTokenProvider.getAuthentication(token);
                                    SecurityContextHolder.getContext().setAuthentication(auth);
                                    accessor.setUser(auth);
                                    System.out.println("Re-authenticated user for " + accessor.getCommand() + ": " + auth.getName());
                                } catch (Exception e) {
                                    System.out.println("Failed to re-authenticate for " + accessor.getCommand() + ": " + e.getMessage());
                                }
                            }
                        }
                    }

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