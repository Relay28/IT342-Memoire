package cit.edu.mmr.controller;

import cit.edu.mmr.dto.NotificationRequest;
import cit.edu.mmr.entity.NotificationEntity;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Autowired
    public NotificationController(NotificationService notificationService,
                                  UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    // Get all notifications for authenticated user
    @GetMapping
    public ResponseEntity<List<NotificationEntity>> getAllNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            Authentication authentication) {
        try {
            List<NotificationEntity> notifications;
            if (unreadOnly) {
                notifications = notificationService.getUnreadNotifications(authentication);
            } else {
                notifications = notificationService.getUserNotifications(authentication);
            }
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error getting notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get a specific notification
    @GetMapping("/{id}")
    public ResponseEntity<NotificationEntity> getNotificationById(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            NotificationEntity notification = notificationService.getNotification(id, authentication);
            return ResponseEntity.ok(notification);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Mark a notification as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            notificationService.markNotificationAsRead(id, authentication);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error marking notification as read", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Mark all notifications as read
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        try {
            notificationService.markAllNotificationsAsRead(authentication);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error marking all notifications as read", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get count of unread notifications
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        try {
            long count = notificationService.getUnreadNotificationCount(authentication);
            return ResponseEntity.ok(Collections.singletonMap("count", count));
        } catch (Exception e) {
            logger.error("Error getting unread notification count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Register/update FCM token
    @PostMapping("/register-token")
    public ResponseEntity<Void> registerFcmToken(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String token = request.get("token");
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            notificationService.updateFcmToken(authentication, token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error registering FCM token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}