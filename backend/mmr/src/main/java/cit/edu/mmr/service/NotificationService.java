package cit.edu.mmr.service;

import cit.edu.mmr.entity.NotificationEntity;

import java.util.List;
import java.util.Optional;

public interface NotificationService {

    NotificationEntity createNotification(Long userId, String type, String text, long relatedItemId, String itemType);

    NotificationEntity markNotificationAsRead(Long notificationId);

    Optional<NotificationEntity> getNotificationById(Long notificationId);

    List<NotificationEntity> getNotificationsForUser(Long userId);

    void deleteNotification(Long notificationId);
}
