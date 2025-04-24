package cit.edu.mmr.service;

import cit.edu.mmr.dto.websocket.NotificationDTO;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import com.google.firebase.messaging.*;
import cit.edu.mmr.entity.NotificationEntity;
import lombok.extern.slf4j.Slf4j;
import cit.edu.mmr.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final FirebaseMessaging firebaseMessaging;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate; // For WebSocket communication

    @Autowired
    public NotificationService(FirebaseMessaging firebaseMessaging,
                               NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.firebaseMessaging = firebaseMessaging;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @CacheEvict(value = "notifications", key = "'userNotifications_' + #userId")
    public void sendNotificationToUser(Long userId, NotificationEntity notification) {
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            // Save notification to database
            notification.setUser(user);
            NotificationEntity savedNotification = notificationRepository.save(notification);

            // Send WebSocket notification
            sendNotificationViaWebSocket(user.getUsername(), savedNotification);

            // Check if user has FCM token
            if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                Message message = Message.builder()
                        .setToken(user.getFcmToken())
                        .setNotification(Notification.builder()
                                .setTitle(getNotificationTitle(notification.getType()))
                                .setBody(notification.getText())
                                .build())
                        .putData("type", notification.getType())
                        .putData("itemType", notification.getItemType())
                        .putData("itemId", String.valueOf((notification.getRelatedItemId())))
                        .build();

                String response = firebaseMessaging.send(message);
                logger.info("Successfully sent notification to user {}: {}", userId, response);
            } else {
                logger.warn("User {} has no FCM token registered", userId);
            }
        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send FCM notification to user {}: {}", userId, e.getMessage());
        } catch (Exception e) {
            logger.error("Error in notification processing for user {}: {}", userId, e.getMessage());
        }
    }


    public void sendPushNotification(Long userId, NotificationEntity notification) {
        // Get FCM token from your database
        String fcmToken = userRepository.findFcmTokenByUserId(userId);

        if (fcmToken != null && !fcmToken.isEmpty()) {
            try {
                Message message = Message.builder()
                        .setToken(fcmToken)
                        .setNotification(Notification.builder()
                                .setTitle(getNotificationTitle(notification.getType()))
                                .setBody(notification.getText())
                                .build())
                        .putData("notificationId", String.valueOf(notification.getId()))
                        .putData("type", notification.getType())
                        .putData("itemType", notification.getItemType())
                        .putData("itemId", String.valueOf(notification.getRelatedItemId()))
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                logger.info("Successfully sent FCM notification: " + response);
            } catch (FirebaseMessagingException e) {
                logger.error("Failed to send FCM notification", e);
            }
        }
    }
    // New method to send notification via WebSocket
    private void sendNotificationViaWebSocket(String username, NotificationEntity notification) {
        try {
            // Convert notification to DTO if needed
            NotificationDTO notificationDTO = convertToDTO(notification);

            // Send to user-specific topic
            messagingTemplate.convertAndSend("/topic/notifications/" + username, notificationDTO);
            logger.info("WebSocket notification sent to user: {}", username);
        } catch (Exception e) {
            logger.error("Failed to send WebSocket notification: {}", e.getMessage());
        }
    }

    // Helper method to convert Entity to DTO
    private NotificationDTO convertToDTO(NotificationEntity notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setText(notification.getText());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setItemType(notification.getItemType());
        dto.setRelatedItemId(notification.getRelatedItemId());
        return dto;
    }

    // Existing methods remain unchanged...
    @Cacheable(value = "notifications", key = "'notification_' + #id + '_' + #auth.name")
    public NotificationEntity getNotification(Long id, Authentication auth) {
        UserEntity currentUser = getAuthenticatedUser(auth);
        return notificationRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
    }

    @Cacheable(value = "notifications", key = "'unreadNotifications_' + #auth.name")
    public List<NotificationEntity> getUnreadNotifications(Authentication auth) {
        UserEntity currentUser = getAuthenticatedUser(auth);
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(currentUser.getId());
    }

    @Caching(evict = {
            @CacheEvict(value = "notifications", key = "'unreadNotifications_' + #auth.name"),
            @CacheEvict(value = "notifications", key = "'unreadCount_' + #auth.name"),
            @CacheEvict(value = "notifications", key = "'userNotifications_' + #auth.name")
    })
    @Transactional
    public void markAllNotificationsAsRead(Authentication auth) {
        UserEntity currentUser = getAuthenticatedUser(auth);
        notificationRepository.markAllAsReadForUser(currentUser.getId());

        // Send WebSocket update for notification count
        sendNotificationCountUpdate(currentUser.getUsername(), 0);
    }

    @Cacheable(value = "notifications", key = "'unreadCount_' + #auth.name")
    public long getUnreadNotificationCount(Authentication auth) {
        UserEntity currentUser = getAuthenticatedUser(auth);
        return notificationRepository.countByUserIdAndIsReadFalse(currentUser.getId());
    }

    @Cacheable(value = "notifications", key = "'userNotifications_' + #auth.name")
    public List<NotificationEntity> getUserNotifications(Authentication auth) {
        UserEntity currentUser = getAuthenticatedUser(auth);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
    }

    @Caching(evict = {
            @CacheEvict(value = "notifications", key = "'notification_' + #notificationId + '_' + #auth.name"),
            @CacheEvict(value = "notifications", key = "'unreadNotifications_' + #auth.name"),
            @CacheEvict(value = "notifications", key = "'unreadCount_' + #auth.name"),
            @CacheEvict(value = "notifications", key = "'userNotifications_' + #auth.name")
    })
    public void markNotificationAsRead(Long notificationId, Authentication auth) {
        UserEntity currentUser = getAuthenticatedUser(auth);
        NotificationEntity notification = notificationRepository.findByIdAndUserId(notificationId, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        notification.setRead(true);
        notificationRepository.save(notification);

        // After marking as read, send updated count via WebSocket
        long newCount = notificationRepository.countByUserIdAndIsReadFalse(currentUser.getId());
        sendNotificationCountUpdate(currentUser.getUsername(), newCount);
    }

    // Method to send notification count updates via WebSocket
    private void sendNotificationCountUpdate(String username, long count) {
        Map<String, Long> countUpdate = Collections.singletonMap("count", count);
        messagingTemplate.convertAndSend("/topic/notifications/count/" + username, countUpdate);
    }

    @CacheEvict(value = "notifications", allEntries = true)
    @Transactional
    public void updateFcmToken(Authentication auth, String fcmToken) {
        UserEntity currentUser = getAuthenticatedUser(auth);
        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }

    private String getNotificationTitle(String type) {
        switch (type) {
            case "FRIEND_REQUEST":
                return "New Friend Request";
            case "FRIEND_REQUEST_ACCEPTED":
                return "Friend Request Accepted";
            case "TIME_CAPSULE_OPEN":
                return "Time Capsule Opened";
            case "COMMENT":
                return "New Comment on Your Time Capsule";
            default:
                return "New Notification";
        }
    }

    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));
    }
}