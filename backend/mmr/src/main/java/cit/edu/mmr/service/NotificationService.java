package cit.edu.mmr.service;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final FirebaseMessaging firebaseMessaging;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationService(FirebaseMessaging firebaseMessaging,
                               NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.firebaseMessaging = firebaseMessaging;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @CacheEvict(value = "notifications", key = "'userNotifications_' + #userId")
    public void sendNotificationToUser(Long userId, NotificationEntity notification) {
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            // Save notification to database
            notification.setUser(user);
            notificationRepository.save(notification);

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
                        .putData("itemId", String.valueOf(notification.getRelatedItemId()))
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