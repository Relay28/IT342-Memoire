package cit.edu.mmr.service;

import cit.edu.mmr.entity.FriendShipEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.FriendShipRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class FriendShipService {

    private final FriendShipRepository friendShipRepository;

    public FriendShipService(FriendShipRepository friendShipRepository) {
        this.friendShipRepository = friendShipRepository;
    }

    public FriendShipEntity createFriendship(UserEntity user, UserEntity friend, String status) {
        FriendShipEntity friendship = new FriendShipEntity();
        friendship.setUser(user);
        friendship.setFriend(friend);
        friendship.setStatus(status);
        friendship.setCreatedAt(new Date());
        return friendShipRepository.save(friendship);
    }

    public Optional<FriendShipEntity> getFriendshipById(Long id) {
        return friendShipRepository.findById(id);
    }

    public List<FriendShipEntity> getFriendshipsByUser(UserEntity user) {
        return friendShipRepository.findByUser(user);
    }

    public List<FriendShipEntity> getFriendshipsByFriend(UserEntity friend) {
        return friendShipRepository.findByFriend(friend);
    }

    public boolean areFriends(UserEntity user, UserEntity friend) {
        Optional<FriendShipEntity> friendshipOpt = friendShipRepository.findByUserAndFriend(user, friend);
        if (!friendshipOpt.isPresent()) {
            friendshipOpt = friendShipRepository.findByUserAndFriend(friend, user);
        }
        return friendshipOpt.isPresent() && "Friends".equalsIgnoreCase(friendshipOpt.get().getStatus());
    }


    public void deleteFriendship(Long id) {
        friendShipRepository.deleteById(id);
    }

    /**
     * Accepts a friend request by updating the status from "Pending" to "Friends".
     *
     * @param friendshipId The ID of the friendship request.
     * @return An Optional containing the updated FriendShipEntity, or empty if not found.
     */
    public Optional<FriendShipEntity> acceptFriendship(Long friendshipId) {
        Optional<FriendShipEntity> optionalFriendship = friendShipRepository.findById(friendshipId);
        if(optionalFriendship.isPresent()) {
            FriendShipEntity friendship = optionalFriendship.get();
            // You can check for a current status if needed
            friendship.setStatus("Friends");
            friendShipRepository.save(friendship);
            return Optional.of(friendship);
        }
        return Optional.empty();
    }
}
