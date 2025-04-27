package cit.edu.mmr.controller;

import cit.edu.mmr.dto.ErrorResponse;
import cit.edu.mmr.dto.FriendshipRequest;
import cit.edu.mmr.entity.FriendShipEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.FriendShipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.ResponseEntity.*;

@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    private static final Logger logger = LoggerFactory.getLogger(FriendshipController.class);

    private final FriendShipService friendShipService;
    private final UserRepository userRepository;

    public FriendshipController(FriendShipService friendShipService, UserRepository userRepository) {
        this.friendShipService = friendShipService;
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createFriendship(@RequestBody FriendshipRequest request, Authentication auth) {
        try {
            logger.info("Received request to create friendship");
            FriendShipEntity friendship = friendShipService.createFriendship(request, auth);
            return new ResponseEntity<>(friendship, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating friendship: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/areFriends/{friendId}")
    public ResponseEntity<?> areFriends(@PathVariable long friendId, Authentication auth) {
        try {
            boolean isFriend = friendShipService.areFriends(friendId, auth);
            return new ResponseEntity<>(isFriend, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error checking friendship status: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/friends")
    public ResponseEntity<?> getFriendsList(Authentication auth) {
        try {
            List<UserEntity> friends = friendShipService.getFriendsList(auth);
            return new ResponseEntity<>(friends, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving friends list: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFriendshipById(@PathVariable Long id, Authentication auth) {
        return friendShipService.getFriendshipById(id)
                .<ResponseEntity<?>>map(friendship -> new ResponseEntity<>(friendship, HttpStatus.OK))
                .orElseGet(() -> {
                    logger.warn("Friendship not found with ID: {}", id);
                    return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Friendship not found"), HttpStatus.NOT_FOUND);
                });
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptFriendship(@PathVariable Long id, Authentication auth) {
        try {
            return friendShipService.acceptFriendship(id, auth)
                    .<ResponseEntity<?>>map(friendship -> new ResponseEntity<>(friendship, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Friendship not found"), HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            logger.error("Error accepting friendship: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFriendship(@PathVariable Long id, Authentication auth) {
        try {
            friendShipService.deleteFriendship(id, auth);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NoSuchElementException e) {
            logger.warn("Delete failed: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            logger.warn("Access denied while deleting friendship: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Unexpected error during deletion: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal error occurred"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/hasPendingRequest/{friendId}")
    public ResponseEntity<?> hasPendingRequest(@PathVariable long friendId, Authentication auth) {
        try {
            boolean hasPending = friendShipService.hasPendingRequest(friendId, auth);
            return new ResponseEntity<>(hasPending, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error checking pending request status: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/cancel/{friendId}")
    public ResponseEntity<?> cancelRequest(@PathVariable long friendId, Authentication auth) {
        try {
            friendShipService.cancelRequest(friendId, auth);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NoSuchElementException e) {
            logger.warn("Cancel request failed: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(NOT_FOUND.value(), e.getMessage()), NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error canceling friend request: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/isReceiver/{friendId}")
    public ResponseEntity<?> isReceiver(@PathVariable long friendId, Authentication auth) {
        try {
            boolean isReceiver = friendShipService.isReceiver(friendId, auth);
            return new ResponseEntity<>(isReceiver, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error checking receiver status: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/requests/received")
    public ResponseEntity<?> getReceivedFriendRequests(Authentication auth) {
        try {
            List<FriendShipEntity> pendingRequests = friendShipService.getReceivedFriendRequests(auth);
            return new ResponseEntity<>(pendingRequests, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving received friend requests: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/findByUsers/{friendId}")
    public ResponseEntity<?> findByUsers(@PathVariable long friendId, Authentication auth) {
        try {
            return friendShipService.findByUsers(friendId, auth)
                    .<ResponseEntity<?>>map(friendship -> new ResponseEntity<>(friendship, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Friendship not found"), HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            logger.error("Error finding friendship between users: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
