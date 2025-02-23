package cit.edu.mmr.repository;

import cit.edu.mmr.entity.FriendShipEntity;
import cit.edu.mmr.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendShipRepository extends JpaRepository<FriendShipEntity, Long> {
    
	  // Find friendship between two users
    Optional<FriendShipEntity> findByUserIdAndFriendId(Long userId, Long friendId);

    // Find all friendships for a user (where user is either the user or friend)
    @Query("SELECT f FROM FriendShipEntity f WHERE f.user.id = :userId OR f.friend.id = :userId")
    List<FriendShipEntity> findAllFriendships(Long userId);

//    // Find all friendships by status
//    List<FriendShipEntity> findByUserIdAndStatus(Long userId, UserEntity Status);

    // Check if friendship exists
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
}