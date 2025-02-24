package cit.edu.mmr.repository;

import cit.edu.mmr.entity.NotificationEntity;
import cit.edu.mmr.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    // Get all notifications for a specific user
    List<NotificationEntity> findByUser(UserEntity user);
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Get unread notifications for a user
    List<NotificationEntity> findByUserAndIsReadFalse(UserEntity user);
    
    // Get notifications by their type
    List<NotificationEntity> findByType(String type);
    
    // Get notifications related to a specific item
    List<NotificationEntity> findByRelatedItemIdAndItemType(Long itemId, String itemType);
}