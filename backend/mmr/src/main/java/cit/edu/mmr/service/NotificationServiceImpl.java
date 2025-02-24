package cit.edu.mmr.service;

import cit.edu.mmr.entity.NotificationEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.NotificationRepository;
import cit.edu.mmr.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public NotificationEntity createNotification(Long userId, String type, String text, long relatedItemId, String itemType) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + userId));

        // If no text is provided, generate a default message based on the type.
        if (text == null || text.trim().isEmpty()) {
            text = generateMessageForType(type, relatedItemId, itemType);
        }

        NotificationEntity notification = new NotificationEntity();
        notification.setUser(user);
        notification.setType(type);
        notification.setText(text);
        notification.setRelatedItemId(relatedItemId);
        notification.setItemType(itemType);
        notification.setRead(false);
        notification.setCreatedAt(new Date());

        return notificationRepository.save(notification);
    }

    private String generateMessageForType(String type, long relatedItemId, String itemType) {
        switch (type.toUpperCase()) {
            case "COMMENT":
                return "You have a new comment on your " + itemType + " (ID: " + relatedItemId + ").";
            case "LIKE":
                return "Your " + itemType + " (ID: " + relatedItemId + ") was liked.";
            case "FRIEND_REQUEST":
                return "You received a friend request.";
            default:
                return "You have a new notification.";
        }
    }


    @Override
    public NotificationEntity markNotificationAsRead(Long notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with id " + notificationId));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Override
    public Optional<NotificationEntity> getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId);
    }

    @Override
    public List<NotificationEntity> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new EntityNotFoundException("Notification not found with id " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }
}
