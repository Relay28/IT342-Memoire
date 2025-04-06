package cit.edu.mmr.service;

import cit.edu.mmr.dto.FriendshipRequest;
import cit.edu.mmr.entity.FriendShipEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.FriendShipRepository;
import cit.edu.mmr.repository.UserRepository;
import org.modelmapper.internal.bytebuddy.implementation.bytecode.Throw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(FriendShipService.class);

    private final FriendShipRepository friendShipRepository;
    private final UserRepository userRepository;

    @Autowired
    public FriendShipService(FriendShipRepository friendShipRepository, UserRepository userRepository) {
        this.friendShipRepository = friendShipRepository;
        this.userRepository = userRepository;
    }

    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        logger.debug("Fetching authenticated user: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public FriendShipEntity createFriendship(FriendshipRequest request, Authentication auth) {
        logger.info("Creating friendship for friendId: {}", request.getFriendId());
        UserEntity user = getAuthenticatedUser(auth);
        UserEntity friend = userRepository.findById(request.getFriendId()).orElse(null);

        if (friend == null) {
            logger.warn("Friend user not found with ID: {}", request.getFriendId());
            throw new UsernameNotFoundException("User not found");
        }

        FriendShipEntity friendship = new FriendShipEntity();
        friendship.setUser(user);
        friendship.setFriend(friend);
        friendship.setStatus("Pending");
        friendship.setCreatedAt(new Date());

        user.getFriendshipsAsUser().add(friendship);
        friend.getFriendshipsAsFriend().add(friendship);

        logger.info("Saving new friendship between {} and {}", user.getUsername(), friend.getUsername());
        return friendShipRepository.save(friendship);
    }

    public Optional<FriendShipEntity> getFriendshipById(Long id) {
        logger.debug("Retrieving friendship by ID: {}", id);
        return friendShipRepository.findById(id);
    }

    public List<FriendShipEntity> getFriendshipsByUser(UserEntity user) {
        logger.debug("Fetching friendships initiated by user: {}", user.getUsername());
        return friendShipRepository.findByUser(user);
    }

    public List<FriendShipEntity> getFriendshipsByFriend(UserEntity friend) {
        logger.debug("Fetching friendships where user is friend: {}", friend.getUsername());
        return friendShipRepository.findByFriend(friend);
    }

    public boolean areFriends(long friendId, Authentication auth) {
        logger.info("Checking friendship status with friendId: {}", friendId);
        UserEntity user = getAuthenticatedUser(auth);
        UserEntity friend = userRepository.findById(friendId).orElse(null);

        if (friend == null) {
            logger.warn("Friend user not found with ID: {}", friendId);
            throw new UsernameNotFoundException("User not found");
        }

        Optional<FriendShipEntity> friendshipOpt = friendShipRepository.findByUserAndFriend(user, friend);
        if (!friendshipOpt.isPresent()) {
            friendshipOpt = friendShipRepository.findByUserAndFriend(friend, user);
        }

        return friendshipOpt.isPresent() && "Friends".equalsIgnoreCase(friendshipOpt.get().getStatus());
    }

    public void deleteFriendship(Long id, Authentication auth) {
        logger.info("Attempting to delete friendship with ID: {}", id);
        UserEntity user = getAuthenticatedUser(auth);

        Optional<FriendShipEntity> optionalFriendship = friendShipRepository.findById(id);
        if (!optionalFriendship.isPresent()) {
            logger.warn("Friendship with ID {} not found", id);
            throw new NoSuchElementException("Friendship not found");
        }

        FriendShipEntity friendship = optionalFriendship.get();
        if (!(friendship.getUser().equals(user) || friendship.getFriend().equals(user))) {
            logger.warn("Unauthorized attempt to delete friendship with ID: {}", id);
            throw new AccessDeniedException("You do not have access to delete this friendship");
        }

        friendShipRepository.deleteById(id);
        logger.info("Deleted friendship with ID: {}", id);
    }

    public Optional<FriendShipEntity> acceptFriendship(Long friendshipId, Authentication auth) {
        logger.info("Accepting friendship request with ID: {}", friendshipId);
        UserEntity user = getAuthenticatedUser(auth);
        Optional<FriendShipEntity> optionalFriendship = friendShipRepository.findById(friendshipId);

        if (optionalFriendship.isPresent()) {
            FriendShipEntity friendship = optionalFriendship.get();
            friendship.setStatus("Friends");
            friendShipRepository.save(friendship);
            logger.info("Friendship with ID {} accepted", friendshipId);
            return Optional.of(friendship);
        } else {
            logger.warn("Friendship with ID {} not found for acceptance", friendshipId);
            return Optional.empty();
        }
    }
}
