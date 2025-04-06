package cit.edu.mmr.service;

import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import com.google.firebase.messaging.*;
import cit.edu.mmr.entity.NotificationEntity;
import lombok.extern.slf4j.Slf4j;
import cit.edu.mmr.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private final FirebaseMessaging firebaseMessaging;
    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository, FirebaseMessaging firebaseMessaging) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.firebaseMessaging = firebaseMessaging;
    }

    public NotificationEntity createNotification(Long userId, String type, String text,
                                                 Long relatedItemId, String itemType) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        NotificationEntity notification = new NotificationEntity();
        notification.setUser(user);
        notification.setType(type);
        notification.setText(text);
        notification.setRelatedItemId(relatedItemId);
        notification.setItemType(itemType);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        NotificationEntity savedNotification = notificationRepository.save(notification);

        // Send FCM notification
        sendFCMNotification(user, type, text, savedNotification.getId());

        return savedNotification;
    }

    public List<NotificationEntity> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }
    public void markAllNotificationsAsRead(Long userId) {
        List<NotificationEntity> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }



    private void sendFCMNotification(UserEntity user, String title, String body, Long notificationId) {
        if (user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
            return; // No token to send to
        }

        try {
            // You can customize the notification payload here
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Add data payload for deep linking or additional processing
            Map<String, String> data = new HashMap<>();
            data.put("notificationId", notificationId.toString());
            data.put("type", "your_notification_type");
            data.put("click_action", "FLUTTER_NOTIFICATION_CLICK");

            Message message = Message.builder()
                    .setToken(user.getFcmToken())
                    .setNotification(notification)
                    .putAllData(data)
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("Successfully sent FCM notification: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM notification", e);

            // Handle invalid/expired tokens
            if (e.getErrorCode().equals("registration-token-not-registered")) {
                user.setFcmToken(null);
                userRepository.save(user);
            }
        }
    }



    public NotificationEntity markNotificationAsRead(Long notificationId) {
        Optional<NotificationEntity> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            NotificationEntity notification = optionalNotification.get();
            notification.setRead(true);
            return notificationRepository.save(notification);
        } else {
            throw new EntityNotFoundException("Notification not found");
        }
    }

    public Optional<NotificationEntity> getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId);
    }

    public List<NotificationEntity> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    public void deleteNotification(Long notificationId) {
        if (notificationRepository.existsById(notificationId)) {
            notificationRepository.deleteById(notificationId);
        } else {
            throw new EntityNotFoundException("Notification not found");
        }
    }

    @Transactional
    public void updateFcmToken(Long userId, String fcmToken) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }

    private String getUserFcmToken(Long userId) {
        // TODO: Fetch user's FCM token from the database (Implement in User entity)
        return "user_fcm_token"; // Replace with actual logic
    }
}
