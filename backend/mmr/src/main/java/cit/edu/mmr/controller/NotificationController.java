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
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationEntity> markAsRead(@PathVariable Long notificationId) {
        try {
            NotificationEntity updated = notificationService.markNotificationAsRead(notificationId);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    // Get unread notifications for a user
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationEntity>> getUnreadNotifications(@PathVariable Long userId) {
        List<NotificationEntity> unreadNotifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(unreadNotifications);
    }


    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllNotificationsAsRead(userId);
        return ResponseEntity.noContent().build();
    }


}
