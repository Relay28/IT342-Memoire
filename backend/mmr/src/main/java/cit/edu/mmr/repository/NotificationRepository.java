package cit.edu.mmr.repository;

import cit.edu.mmr.entity.NotificationEntity;
import cit.edu.mmr.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    // Get all notifications for a specific user
    List<NotificationEntity> findByUser(UserEntity user);
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<NotificationEntity> findByUserId(long id);


    Optional<NotificationEntity> findByIdAndUserId(Long notificationId, Long userId);

    // Get unread notifications for a user
    List<NotificationEntity> findByUserAndIsReadFalse(UserEntity user);
    List<NotificationEntity> findByUserIdAndIsReadFalse(Long userId);
    List<NotificationEntity> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.user.id = :userId")
    void markAllAsReadForUser(@Param("userId") Long userId);
    // Get notifications by their type
    List<NotificationEntity> findByType(String type);
    
    // Get notifications related to a specific item
    List<NotificationEntity> findByRelatedItemIdAndItemType(Long itemId, String itemType);
}