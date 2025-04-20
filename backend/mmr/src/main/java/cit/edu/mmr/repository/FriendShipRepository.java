package cit.edu.mmr.repository;

import cit.edu.mmr.entity.FriendShipEntity;
import cit.edu.mmr.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;


public interface FriendShipRepository extends JpaRepository<FriendShipEntity, Long> {

    // Correct query method for finding pending requests
    @Query("SELECT f FROM FriendShipEntity f WHERE " +
            "((f.user = :user AND f.friend = :friend) OR " +
            "(f.friend = :user AND f.user = :friend)) " +
            "AND f.Status = 'Pending'")  // Note: Case matches your entity field
    List<FriendShipEntity> findPendingRequestsBetweenUsers(
            @Param("user") UserEntity user,
            @Param("friend") UserEntity friend
    );

    // Existing methods with proper case sensitivity
    boolean existsByUserAndFriend(UserEntity user, UserEntity friend);
    Optional<FriendShipEntity> findByUserAndFriend(UserEntity user, UserEntity friend);

    // Additional useful methods
    List<FriendShipEntity> findByUser(UserEntity user);
    List<FriendShipEntity> findByFriend(UserEntity friend);
}