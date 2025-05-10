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

    @Query("SELECT COUNT(f) FROM FriendShipEntity f WHERE f.user = :user AND f.Status = :Status")
    long countByUserAndStatus(@Param("user") UserEntity user, @Param("Status") String Status);

    @Query("SELECT COUNT(f) FROM FriendShipEntity f WHERE f.friend = :friend AND f.Status = :Status")
    long countByFriendAndStatus(@Param("friend") UserEntity friend, @Param("Status") String Status);
    @Query("SELECT f FROM FriendShipEntity f WHERE " +
            "(f.user.id = :userId AND f.friend.id = :friendId) OR " +
            "(f.user.id = :friendId AND f.friend.id = :userId)")
    Optional<FriendShipEntity> findBetweenUsers(@Param("userId") Long userId,
                                                @Param("friendId") Long friendId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
            "FROM FriendShipEntity f " +
            "WHERE f.friend.id = :userId AND f.user.id = :friendId AND f.Status = 'Pending'")
    boolean isReceiverOfPendingRequest(@Param("userId") Long userId,
                                       @Param("friendId") Long friendId);
    // Existing methods with proper case sensitivity
    boolean existsByUserAndFriend(UserEntity user, UserEntity friend);
    Optional<FriendShipEntity> findByUserAndFriend(UserEntity user, UserEntity friend);


    @Query("SELECT f FROM FriendShipEntity f WHERE f.user = :user AND f.Status = :Status")
    List<FriendShipEntity> findByUserAndStatusCustom(@Param("user") UserEntity user, @Param("Status") String Status);

    @Query("SELECT f FROM FriendShipEntity f WHERE f.friend = :friend AND f.Status = :Status")
    List<FriendShipEntity> findByFriendAndStatusCustom(@Param("friend") UserEntity friend, @Param("Status") String Status);
    // Additional useful methods
    List<FriendShipEntity> findByUser(UserEntity user);
    List<FriendShipEntity> findByFriend(UserEntity friend);

    @Query("SELECT f FROM FriendShipEntity f WHERE " +
            "(f.user = :user1 OR f.friend = :user2) AND f.Status = :statusParam")
    List<FriendShipEntity> findByUserOrFriendAndStatus(
            @Param("user1") UserEntity user,
            @Param("user2") UserEntity friend,
            @Param("statusParam") String Status);
}