package cit.edu.mmr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
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

    @Bean
    public TaskScheduler messageBrokerTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("wss-heartbeat-thread-");
        scheduler.setDaemon(true);
        return scheduler;
    }

    @Bean
    public WebSocketTransportRegistration webSocketTransportRegistration() {
        return new WebSocketTransportRegistration()
                .setMessageSizeLimit(128 * 1024)
                .setSendTimeLimit(20 * 1000)
                .setSendBufferSizeLimit(512 * 1024)
                .setTimeToFirstMessage(60000);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{25000, 25000})
                .setTaskScheduler(messageBrokerTaskScheduler()); // Add the task scheduler here
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // General notifications endpoint
        registry.addEndpoint("/ws-notifications")
                .setAllowedOriginPatterns("*")
                .addInterceptors(authTokenHandshakeInterceptor)
                .withSockJS()
                .setWebSocketEnabled(true)
                .setSessionCookieNeeded(false)
                .setDisconnectDelay(30000);

        // Specific endpoint for capsule content real-time updates
        registry.addEndpoint("/ws-capsule-content")
                .setAllowedOriginPatterns("*")
                .addInterceptors(authTokenHandshakeInterceptor)
                .withSockJS()
                .setWebSocketEnabled(true)
                .setSessionCookieNeeded(false)
                .setHeartbeatTime(25000)
                .setDisconnectDelay(30000)
                .setStreamBytesLimit(512 * 1024)
                .setHttpMessageCacheSize(1000);

        // Keep existing comments endpoint
        registry.addEndpoint("/ws-comments")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setWebSocketEnabled(true);

        // Add raw WebSocket endpoints (no SockJS) as fallback
        registry.addEndpoint("/ws-capsule-content")
                .setAllowedOriginPatterns("*")
                .addInterceptors(authTokenHandshakeInterceptor);

        registry.addEndpoint("/ws-notifications")
                .setAllowedOriginPatterns("*")
                .addInterceptors(authTokenHandshakeInterceptor);

        registry.addEndpoint("/ws-comments")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                try {
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
                                // Don't throw exception in GAE - just log it
                            }
                        } else {
                            System.out.println("No token provided at CONNECT");
                            // Don't throw exception in GAE - just log it
                        }
                    }
                    else if (StompCommand.SEND.equals(accessor.getCommand()) ||
                            StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                        if (accessor.getUser() == null) {
                            Object userAttr = accessor.getSessionAttributes() != null ?
                                    accessor.getSessionAttributes().get("user") : null;
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
                } catch (Exception e) {
                    System.out.println("Error in STOMP interceptor: " + e.getMessage());
                    e.printStackTrace();
                }

                return message;
            }
        });
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(128 * 1024)
                .setSendTimeLimit(20000)
                .setSendBufferSizeLimit(512 * 1024)
                .setTimeToFirstMessage(60000);
    }
}