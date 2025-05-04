package cit.edu.mmr.service;

import cit.edu.mmr.dto.FriendshipDTO;
import cit.edu.mmr.dto.FriendshipRequest;
import cit.edu.mmr.dto.UserDTO;
import cit.edu.mmr.entity.FriendShipEntity;
import cit.edu.mmr.entity.NotificationEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.FriendShipRepository;
import cit.edu.mmr.repository.UserRepository;
import jakarta.transaction.Transactional;
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

import java.util.*;
import java.util.stream.Collectors;

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
    @Transactional
    public FriendshipDTO createFriendship(FriendshipRequest request, Authentication auth) {
        logger.info("Creating friendship for friendId: {}", request.getFriendId());
        UserEntity user = getAuthenticatedUser(auth);
        UserEntity friend = userRepository.findById(request.getFriendId())
                .orElseThrow(() -> {
                    logger.warn("Friend user not found with ID: {}", request.getFriendId());
                    return new UsernameNotFoundException("User not found");
                });

        FriendShipEntity friendship = new FriendShipEntity();
        friendship.setUser(user);
        friendship.setFriend(friend);
        friendship.setStatus("Pending");
        friendship.setCreatedAt(new Date());

        // We don't need to explicitly manage the collections here
        // Just save the friendship entity
        logger.info("Saving new friendship between {} and {}", user.getUsername(), friend.getUsername());
        FriendShipEntity savedFriendship = friendShipRepository.save(friendship);

        NotificationEntity notification = new NotificationEntity();
        notification.setType("FRIEND_REQUEST");
        notification.setText(user.getUsername() + " sent you a friend request");
        notification.setRelatedItemId(savedFriendship.getId());
        notification.setItemType("FRIENDSHIP");

        notificationService.sendNotificationToUser(friend.getId(), notification);

        // Convert to DTO before returning
        return convertToDTO(savedFriendship);
    }

    @Transactional
    public List<FriendshipDTO> getReceivedFriendRequests(Authentication auth) {
        logger.info("Retrieving received friend requests for user: {}", auth.getName());
        UserEntity user = getAuthenticatedUser(auth);

        // Get friendships where the current user is the receiver
        // and the status is "Pending"
        List<FriendShipEntity> receivedRequests = friendShipRepository.findByFriendAndStatusCustom(user, "Pending");

        logger.debug("Found {} received friend requests for user {}", receivedRequests.size(), user.getUsername());
        return receivedRequests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getFriendsList(Authentication auth) {
        logger.info("Retrieving friends list for user: {}", auth.getName());
        UserEntity user = getAuthenticatedUser(auth);

        List<FriendShipEntity> friendshipsAsUser = friendShipRepository.findByUserAndStatusCustom(user, "Friends");
        List<FriendShipEntity> friendshipsAsFriend = friendShipRepository.findByFriendAndStatusCustom(user, "Friends");

        List<UserDTO> friends = new ArrayList<>();

        // Add friends where the current user is the sender
        for (FriendShipEntity friendship : friendshipsAsUser) {
            friends.add(convertToUserDTO(friendship.getFriend()));
        }

        // Add friends where the current user is the receiver
        for (FriendShipEntity friendship : friendshipsAsFriend) {
            friends.add(convertToUserDTO(friendship.getUser()));
        }

        logger.debug("Found {} friends for user {}", friends.size(), user.getUsername());
        return friends;
    }

    private UserDTO convertToUserDTO(UserEntity user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfilePictureData(),
                user.getRole(),
                user.getBiography(),
                user.isActive(),
                user.isOauthUser(),
                user.getCreatedAt()
        );
    }

    @Cacheable(value = "contentMetadata", key = "'friendship_' + #id")
    @Transactional
    public Optional<FriendshipDTO> getFriendshipById(Long id) {
        logger.debug("Retrieving friendship by ID: {}", id);
        return friendShipRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Cacheable(value = "userAuthentication", key = "'friendOf_' + #friend.id")
    @Transactional
    public List<FriendshipDTO> getFriendshipsByUser(UserEntity user) {
        logger.debug("Fetching friendships initiated by user: {}", user.getUsername());
        return friendShipRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "userAuthentication", key = "'friendOf_' + #friend.id")
    @Transactional
    public List<FriendshipDTO> getFriendshipsByFriend(UserEntity friend) {
        logger.debug("Fetching friendships where user is friend: {}", friend.getUsername());
        return friendShipRepository.findByFriend(friend).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "userAuthentication",
            key = "'areFriends_' + #auth.name + '_' + #friendId",
            condition = "#friendId != null")
    @Transactional
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
            @CacheEvict(value = "contentMetadata", key = "'friendship_' + #friendshipId"),
            @CacheEvict(value = "userAuthentication", key = "'areFriends_' + #auth.name + '_' + #optionalFriendship.get().friend.id"),
            @CacheEvict(value = "userAuthentication", key = "'areFriends_' + #optionalFriendship.get().friend.username + '_' + #auth.name")
    })
    @Transactional
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

        // Get friend details before deletion
        UserEntity friend = friendship.getUser().equals(user) ? friendship.getFriend() : friendship.getUser();

        friendShipRepository.deleteById(id);
        logger.info("Deleted friendship with ID: {}", id);

    }

    @Caching(evict = {
            @CacheEvict(value = "userAuthentication", key = "'friends_' + #auth.name"),
            @CacheEvict(value = "userAuthentication", key = "'friends_' + #optionalFriendship.get().friend.id"),
            @CacheEvict(value = "contentMetadata", key = "'friendship_' + #friendshipId"),
            @CacheEvict(value = "userAuthentication", key = "'areFriends_' + #auth.name + '_' + #optionalFriendship.get().friend.id"),
            @CacheEvict(value = "userAuthentication", key = "'areFriends_' + #optionalFriendship.get().friend.username + '_' + #auth.name")
    })
    @Transactional
    public Optional<FriendshipDTO> acceptFriendship(Long friendshipId, Authentication auth) {
        logger.info("Accepting friendship request with ID: {}", friendshipId);
        UserEntity user = getAuthenticatedUser(auth);
        Optional<FriendShipEntity> optionalFriendship = friendShipRepository.findById(friendshipId);

        if (optionalFriendship.isPresent()) {
            FriendShipEntity friendship = optionalFriendship.get();

            // Check if the current user is the receiver of the request
            if (!friendship.getFriend().equals(user)) {
                throw new AccessDeniedException("You can only accept friend requests sent to you");
            }

            friendship.setStatus("Friends");
            friendShipRepository.save(friendship);

            NotificationEntity notification = new NotificationEntity();
            notification.setType("FRIEND_REQUEST_ACCEPTED");
            notification.setText(user.getUsername() + " accepted your friend request");
            notification.setRelatedItemId(friendship.getId());
            notification.setItemType("FRIENDSHIP");

            notificationService.sendNotificationToUser(friendship.getUser().getId(), notification);

            logger.info("Friendship with ID {} accepted", friendshipId);
            return Optional.of(convertToDTO(friendship));
        } else {
            logger.warn("Friendship with ID {} not found for acceptance", friendshipId);
            return Optional.empty();
        }
    }

    @Transactional
    public boolean hasPendingRequest(long friendId, Authentication auth) {
        logger.info("Checking pending request status with friendId: {}", friendId);
        UserEntity user = getAuthenticatedUser(auth);
        UserEntity friend = userRepository.findById(friendId).orElse(null);

        if (friend == null) {
            logger.warn("Friend user not found with ID: {}", friendId);
            throw new UsernameNotFoundException("User not found");
        }

        // Check if user has sent a request to friend
        Optional<FriendShipEntity> friendshipOpt = friendShipRepository.findByUserAndFriend(user, friend);
        if (friendshipOpt.isPresent() && "Pending".equalsIgnoreCase(friendshipOpt.get().getStatus())) {
            return true;
        }

        // Check if friend has sent a request to user
        friendshipOpt = friendShipRepository.findByUserAndFriend(friend, user);
        return friendshipOpt.isPresent() && "Pending".equalsIgnoreCase(friendshipOpt.get().getStatus());
    }

    @Transactional
    public void cancelRequest(long friendId, Authentication auth) {
        UserEntity user = getAuthenticatedUser(auth);
        UserEntity friend = userRepository.findById(friendId)
                .orElseThrow(() -> new UsernameNotFoundException("Friend not found"));

        List<FriendShipEntity> pendingRequests = friendShipRepository
                .findPendingRequestsBetweenUsers(user, friend);

        if (pendingRequests.isEmpty()) {
            throw new NoSuchElementException("No pending request exists between these users");
        }

        friendShipRepository.deleteAll(pendingRequests);
    }

    @Transactional
    public boolean isReceiver(Long friendId, Authentication auth) {
        logger.info("Checking if user is receiver of friend request from: {}", friendId);
        UserEntity user = getAuthenticatedUser(auth);
        UserEntity friend = userRepository.findById(friendId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return friendShipRepository.isReceiverOfPendingRequest(user.getId(), friend.getId());
    }

    @Transactional
    public Optional<FriendshipDTO> findByUsers(Long friendId, Authentication auth) {
        logger.info("Finding friendship between users: {}", friendId);
        UserEntity user = getAuthenticatedUser(auth);
        UserEntity friend = userRepository.findById(friendId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return friendShipRepository.findBetweenUsers(user.getId(), friend.getId())
                .map(this::convertToDTO);
    }

    // Helper method to convert entity to DTO
    private FriendshipDTO convertToDTO(FriendShipEntity entity) {
        FriendshipDTO dto = new FriendshipDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId());
        dto.setUsername(entity.getUser().getUsername());
        dto.setFriendId(entity.getFriend().getId());
        dto.setFriendUsername(entity.getFriend().getUsername());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}