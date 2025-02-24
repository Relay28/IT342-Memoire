package cit.edu.mmr.repository;

import cit.edu.mmr.entity.FriendShipEntity;
import cit.edu.mmr.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendShipRepository extends JpaRepository<FriendShipEntity, Long> {
    List<FriendShipEntity> findByUser(UserEntity user);
    List<FriendShipEntity> findByFriend(UserEntity friend);
    boolean existsByUserAndFriend(UserEntity user, UserEntity friend);
    Optional<FriendShipEntity> findByUserAndFriend(UserEntity user, UserEntity friend);
}
