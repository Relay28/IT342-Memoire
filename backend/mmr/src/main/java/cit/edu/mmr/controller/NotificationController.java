package cit.edu.mmr.controller;

import cit.edu.mmr.dto.NotificationRequest;
import cit.edu.mmr.entity.NotificationEntity;
import cit.edu.mmr.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Create a new notification
    @PostMapping("/user/{userId}")
    public ResponseEntity<NotificationEntity> createNotification(
            @PathVariable Long userId,
            @RequestBody NotificationRequest notificationRequest) {
        try {
            NotificationEntity notification = notificationService.createNotification(
                    userId,
                    notificationRequest.getType(),
                    notificationRequest.getText(),
                    notificationRequest.getRelatedItemId(),
                    notificationRequest.getItemType()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(notification);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Mark a notification as read
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationEntity> markAsRead(@PathVariable Long notificationId) {
        try {
            NotificationEntity updated = notificationService.markNotificationAsRead(notificationId);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Get a single notification by its ID
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationEntity> getNotificationById(@PathVariable Long notificationId) {
        return notificationService.getNotificationById(notificationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Get all notifications for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationEntity>> getNotificationsForUser(@PathVariable Long userId) {
        List<NotificationEntity> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    // Delete a notification
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
