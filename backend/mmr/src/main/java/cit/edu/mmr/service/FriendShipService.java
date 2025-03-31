package cit.edu.mmr.service;

import cit.edu.mmr.dto.FriendshipRequest;
import cit.edu.mmr.entity.FriendShipEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.FriendShipRepository;
import cit.edu.mmr.repository.UserRepository;
import org.modelmapper.internal.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class FriendShipService {

    private final FriendShipRepository friendShipRepository;
    @Autowired
    private final UserRepository userRepository;

    public FriendShipService(FriendShipRepository friendShipRepository, UserRepository userRepository) {
        this.friendShipRepository = friendShipRepository;
        this.userRepository = userRepository;
    }
    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public FriendShipEntity createFriendship(FriendshipRequest request, Authentication auth) {
       UserEntity user = getAuthenticatedUser(auth);
        UserEntity friend = userRepository.findById(request.getFriendId()).orElse(null);
        if (friend == null) {
            throw new UsernameNotFoundException("User  not found");
        }
        FriendShipEntity friendship = new FriendShipEntity();
        friendship.setUser(user);
        friendship.setFriend(friend);

        friendship.setStatus("Pending");
        friendship.setCreatedAt(new Date());
        List<FriendShipEntity> usr = user.getFriendshipsAsUser();
        List<FriendShipEntity> fsr = friend.getFriendshipsAsFriend();
        fsr.add(friendship);
        usr.add(friendship);
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

    public boolean areFriends(long friendId , Authentication auth) {

        UserEntity user = getAuthenticatedUser(auth);
        UserEntity friend = userRepository.findById(friendId).orElse(null);
        if (friend == null) {
            throw new UsernameNotFoundException("User  not found");
        }
        Optional<FriendShipEntity> friendshipOpt = friendShipRepository.findByUserAndFriend(user, friend);
        if (!friendshipOpt.isPresent()) {
            friendshipOpt = friendShipRepository.findByUserAndFriend(friend, user);
        }
        return friendshipOpt.isPresent() && "Friends".equalsIgnoreCase(friendshipOpt.get().getStatus());
    }


    public void deleteFriendship(Long id, Authentication auth) {
        UserEntity user = getAuthenticatedUser(auth);

        Optional<FriendShipEntity> optionalFriendship = friendShipRepository.findById(id);

        if (!optionalFriendship.isPresent()) {
            throw new NoSuchElementException("Friendship not found");
        }

        FriendShipEntity friendship = optionalFriendship.get();

        // Corrected condition: The user must be part of the friendship to delete it
        if (!(friendship.getUser().equals(user) || friendship.getFriend().equals(user))) {
            throw new AccessDeniedException("You do not have access to delete this friendship");
        }

        friendShipRepository.deleteById(id);
    }

    /**
     * Accepts a friend request by updating the status from "Pending" to "Friends".
     *
     * @param friendshipId The ID of the friendship request.
     * @return An Optional containing the updated FriendShipEntity, or empty if not found.
     */
    public Optional<FriendShipEntity> acceptFriendship(Long friendshipId, Authentication auth) {
        UserEntity user = getAuthenticatedUser(auth);
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
