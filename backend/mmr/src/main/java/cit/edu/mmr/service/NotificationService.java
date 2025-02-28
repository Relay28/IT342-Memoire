package cit.edu.mmr.service;

import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import com.google.firebase.messaging.*;
import cit.edu.mmr.entity.NotificationEntity;
import cit.edu.mmr.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationEntity createNotification(Long userId, String type, String text, Long relatedItemId, String itemType) {
        NotificationEntity notification = new NotificationEntity();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        notification.setUser(user);
        notification.setType(type);
        notification.setText(text);
        notification.setRelatedItemId(relatedItemId);
        notification.setItemType(itemType);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        // Save notification in the database
        NotificationEntity savedNotification = notificationRepository.save(notification);

        // Send FCM push notification
        sendFCMNotification(userId, type, text);

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



    private void sendFCMNotification(Long userId, String title, String message) {
        try {
            // Fetch user's FCM token from database (You need to implement `getUserFcmToken`)
            String fcmToken = getUserFcmToken(userId);
            if (fcmToken == null) {
                return;
            }

            Message firebaseMessage = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(message)
                            .build())
                    .build();

            FirebaseMessaging.getInstance().send(firebaseMessage);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
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

    private String getUserFcmToken(Long userId) {
        // TODO: Fetch user's FCM token from the database (Implement in User entity)
        return "user_fcm_token"; // Replace with actual logic
    }
}
