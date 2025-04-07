package cit.edu.mmr.service;

import cit.edu.mmr.dto.FriendshipRequest;
import cit.edu.mmr.entity.FriendShipEntity;
import cit.edu.mmr.entity.NotificationEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.FriendShipRepository;
import cit.edu.mmr.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class FriendShipService {

    private static final Logger logger = LoggerFactory.getLogger(FriendShipService.class);

    private final FriendShipRepository friendShipRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Autowired
    public FriendShipService(FriendShipRepository friendShipRepository, NotificationService notificationService, UserRepository userRepository) {
        this.friendShipRepository = friendShipRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        logger.debug("Fetching authenticated user: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Caching(evict = {
            @CacheEvict(value = "userAuthentication", key = "'friends_' + #auth.name"),
            @CacheEvict(value = "userAuthentication", key = "'friends_' + #request.friendId"),
            @CacheEvict(value = "contentMetadata", key = "'friendship_' + #result.id", condition = "#result != null")
    })
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
        FriendShipEntity savedFriendship = friendShipRepository.save(friendship);

        NotificationEntity notification = new NotificationEntity();
        notification.setType("FRIEND_REQUEST");
        notification.setText(user.getUsername() + " sent you a friend request");
        notification.setRelatedItemId(savedFriendship.getId());
        notification.setItemType("FRIENDSHIP");

        notificationService.sendNotificationToUser(friend.getId(), notification);

        return friendship;

    }

    @Cacheable(value = "contentMetadata", key = "'friendship_' + #id")
    public Optional<FriendShipEntity> getFriendshipById(Long id) {
        logger.debug("Retrieving friendship by ID: {}", id);
        return friendShipRepository.findById(id);
    }

    @Cacheable(value = "userAuthentication", key = "'friendOf_' + #friend.id")
    public List<FriendShipEntity> getFriendshipsByUser(UserEntity user) {
        logger.debug("Fetching friendships initiated by user: {}", user.getUsername());
        return friendShipRepository.findByUser(user);
    }

    @Cacheable(value = "userAuthentication", key = "'friendOf_' + #friend.id")
    public List<FriendShipEntity> getFriendshipsByFriend(UserEntity friend) {
        logger.debug("Fetching friendships where user is friend: {}", friend.getUsername());
        return friendShipRepository.findByFriend(friend);
    }

    @Cacheable(value = "userAuthentication",
            key = "'areFriends_' + #auth.name + '_' + #friendId",
            condition = "#friendId != null")
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


    @Caching(evict = {
            @CacheEvict(value = "userAuthentication", key = "'friends_' + #auth.name"),
            @CacheEvict(value = "userAuthentication", key = "'friends_' + #optionalFriendship.get().friend.id"),
            @CacheEvict(value = "contentMetadata", key = "'friendship_' + #id"),
            @CacheEvict(value = "userAuthentication", key = "'areFriends_' + #auth.name + '_' + #optionalFriendship.get().friend.id"),
            @CacheEvict(value = "userAuthentication", key = "'areFriends_' + #optionalFriendship.get().friend.username + '_' + #auth.name")
    })
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

    @Caching(evict = {
            @CacheEvict(value = "userAuthentication", key = "'friends_' + #auth.name"),
            @CacheEvict(value = "userAuthentication", key = "'friends_' + #optionalFriendship.get().user.id"),
            @CacheEvict(value = "contentMetadata", key = "'friendship_' + #friendshipId"),
            @CacheEvict(value = "userAuthentication", key = "'areFriends_' + #auth.name + '_' + #optionalFriendship.get().user.id"),
            @CacheEvict(value = "userAuthentication", key = "'areFriends_' + #optionalFriendship.get().user.username + '_' + #auth.name")
    })
    public Optional<FriendShipEntity> acceptFriendship(Long friendshipId, Authentication auth) {
        logger.info("Accepting friendship request with ID: {}", friendshipId);
        UserEntity user = getAuthenticatedUser(auth);
        Optional<FriendShipEntity> optionalFriendship = friendShipRepository.findById(friendshipId);

        if (optionalFriendship.isPresent()) {
            FriendShipEntity friendship = optionalFriendship.get();
            friendship.setStatus("Friends");
            friendShipRepository.save(friendship);
            NotificationEntity notification = new NotificationEntity();
            notification.setType("FRIEND_REQUEST_ACCEPTED");
            notification.setText(friendship.getFriend().getUsername() + " accepted your friend request");
            notification.setRelatedItemId(friendship.getId());
            notification.setItemType("FRIENDSHIP");

            notificationService.sendNotificationToUser(friendship.getUser().getId(), notification);

            logger.info("Friendship with ID {} accepted", friendshipId);
            return Optional.of(friendship);
        } else {
            logger.warn("Friendship with ID {} not found for acceptance", friendshipId);
            return Optional.empty();
        }
    }
}
