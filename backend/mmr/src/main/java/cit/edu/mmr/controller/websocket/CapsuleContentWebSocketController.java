package cit.edu.mmr.controller.websocket;

import cit.edu.mmr.dto.websocket.capsulecontent.ContentDeleteEvent;
import cit.edu.mmr.dto.websocket.capsulecontent.ContentUpdateEvent;
import cit.edu.mmr.entity.CapsuleContentEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.serviceInterfaces.CapsuleContentService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

@Controller
public class CapsuleContentWebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(CapsuleContentWebSocketController.class);
    private final CapsuleContentService capsuleContentService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
     private final UserRepository userRepository;
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Autowired
    public CapsuleContentWebSocketController(CapsuleContentService capsuleContentService, SimpMessagingTemplate messagingTemplate, UserRepository userRepository) {
        this.capsuleContentService = capsuleContentService;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        logger.info("CapsuleContentWebSocketController initialized");
    }

    @MessageMapping("app/capsule-content/connect/{capsuleId}")
    public void handleConnectionRequest(
            @DestinationVariable Long capsuleId,
            SimpMessageHeaderAccessor headerAccessor, Principal authentication) {
        System.out.println("WebSocket connection attempt for capsule: {}"+capsuleId);
        logger.debug("HeaderAccessor user: {}", headerAccessor.getUser());
        logger.debug("Received connection request for capsule ID: {}", capsuleId);


        if (authentication == null ) {
            logger.warn("Unauthenticated connection attempt for capsule ID: {}", capsuleId);
            throw new AccessDeniedException("Authentication required");
        }

     String user = authentication.getName();
    Authentication auth =(Authentication) authentication;
        String sessionId = headerAccessor.getSessionId();
        logger.debug("Processing connection request - Capsule ID: {}, User: {}, Session ID: {}",
                capsuleId, user, sessionId);

        try {
            capsuleContentService.handleConnectionRequest(capsuleId, auth, sessionId);
            logger.info("Successfully processed connection request for capsule ID: {}, User: {}",
                    capsuleId, authentication.getName());
        } catch (Exception e) {
            logger.error("Error processing connection request for capsule ID: {}, User: {}. Error: {}",
                    capsuleId, authentication.getName(), e.getMessage(), e);
            throw e;
        }
    }
    @MessageMapping("/capsule/{capsuleId}")
    @SendTo("/topic/capsule/{capsuleId}")
    public List<CapsuleContentEntity> getCapsuleContents(@DestinationVariable String capsuleId,Authentication auth) {
        return capsuleContentService.getContentsByCapsuleId(Long.parseLong(capsuleId),auth);
    }

    @EventListener
    public void handleContentUpdate(ContentUpdateEvent event) {
        String eventId = event.getEventId();
        Long capsuleId = event.getCapsuleId();

        logger.debug("Processing content update event - Event ID: {}, Capsule ID: {}", eventId, capsuleId);

        if (processedEventIds.contains(eventId)) {
            logger.debug("Duplicate event detected, skipping processing - Event ID: {}", eventId);
            return;
        }

        processedEventIds.add(eventId);
        logger.trace("Added event to processed events set - Event ID: {}", eventId);

        try {
            Map<String, Object> payload = new HashMap<>(event.getContentMap());
            payload.put("eventId", eventId);
            payload.put("timestamp", System.currentTimeMillis());

            String destination = "/topic/capsule-content/updates/" + capsuleId;
            messagingTemplate.convertAndSend(destination, payload);

            logger.info("Successfully sent content update - Event ID: {}, Capsule ID: {}, Destination: {}",
                    eventId, capsuleId, destination);
        } catch (Exception e) {
            logger.error("Failed to send content update - Event ID: {}, Capsule ID: {}. Error: {}",
                    eventId, capsuleId, e.getMessage(), e);
        } finally {
            // Clean up after 5 minutes
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    processedEventIds.remove(eventId);
                    logger.trace("Removed event from processed events set - Event ID: {}", eventId);
                }
            }, 300000);
        }
    }

    public void sendInitialContents(String sessionId, Long capsuleId, List<CapsuleContentEntity> contents) {
        logger.debug("Preparing to send initial contents - Capsule ID: {}, Session ID: {}, Content count: {}",
                capsuleId, sessionId, contents.size());


        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("capsuleId", capsuleId);
            payload.put("contents", contents);

            String destination = "/queue/capsule-content/initial";
            messagingTemplate.convertAndSendToUser(sessionId, destination, payload);

            logger.info("Successfully sent initial contents - Capsule ID: {}, Session ID: {}, Content count: {}",
                    capsuleId, sessionId, contents.size());
        } catch (Exception e) {
            logger.error("Failed to send initial contents - Capsule ID: {}, Session ID: {}. Error: {}",
                    capsuleId, sessionId, e.getMessage(), e);
        }
    }

    @EventListener
    public void handleContentDeletion(ContentDeleteEvent event) {
        String eventId = event.getEventId();
        Long capsuleId = event.getCapsuleId();
        Long contentId = event.getContentId();

        logger.debug("Processing content deletion event - Event ID: {}, Capsule ID: {}, Content ID: {}",
                eventId, capsuleId, contentId);

        if (processedEventIds.contains(eventId)) {
            logger.debug("Duplicate deletion event detected, skipping processing - Event ID: {}", eventId);
            return;
        }

        processedEventIds.add(eventId);
        logger.trace("Added deletion event to processed events set - Event ID: {}", eventId);

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", "delete");
            payload.put("contentId", contentId);
            payload.put("eventId", eventId);
            payload.put("timestamp", System.currentTimeMillis());

            String destination = "/topic/capsule-content/updates/" + capsuleId;
            messagingTemplate.convertAndSend(destination, payload);

            logger.info("Successfully sent content deletion - Event ID: {}, Capsule ID: {}, Content ID: {}",
                    eventId, capsuleId, contentId);
        } catch (Exception e) {
            logger.error("Failed to send content deletion - Event ID: {}, Capsule ID: {}, Content ID: {}. Error: {}",
                    eventId, capsuleId, contentId, e.getMessage(), e);
        } finally {
            // Clean up after 5 minutes
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    processedEventIds.remove(eventId);
                    logger.trace("Removed deletion event from processed events set - Event ID: {}", eventId);
                }
            }, 300000);
        }
    }
}