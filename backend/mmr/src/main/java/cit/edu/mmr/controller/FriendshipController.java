package cit.edu.mmr.controller;

import cit.edu.mmr.dto.FriendshipRequest;
import cit.edu.mmr.entity.FriendShipEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.FriendShipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.ResponseEntity.*;
@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    private final FriendShipService friendShipService;
    private final UserRepository userRepository;

    public FriendshipController(FriendShipService friendShipService, UserRepository userRepository) {
        this.friendShipService = friendShipService;
        this.userRepository = userRepository;
    }

    // Create a new friendship
    @PostMapping("/create")
    public ResponseEntity<?> createFriendship(@RequestBody FriendshipRequest request) {
        UserEntity user = userRepository.findById(request.getUserId()).orElse(null);
        UserEntity friend = userRepository.findById(request.getFriendId()).orElse(null);
        if (user == null || friend == null) {
            return new ResponseEntity<>("User or Friend not found", NOT_FOUND);
        }
        FriendShipEntity friendship = friendShipService.createFriendship(user, friend, request.getStatus());
        return new ResponseEntity<>(friendship, HttpStatus.CREATED);
    }

    // Check if two users are friends
    @GetMapping("/areFriends")
    public ResponseEntity<?> areFriends(@RequestParam long userId, @RequestParam long friendId) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        UserEntity friend = userRepository.findById(friendId).orElse(null);
        if (user == null || friend == null) {
            return new ResponseEntity<>("User or Friend not found", NOT_FOUND);
        }
        boolean isFriend = friendShipService.areFriends(user, friend);
        return new ResponseEntity<>(isFriend, HttpStatus.OK);
    }

    // Retrieve a friendship by its id
    @GetMapping("/{id}")
    public ResponseEntity<?> getFriendshipById(@PathVariable Long id) {
        return friendShipService.getFriendshipById(id)
                .<ResponseEntity<?>>map(friendship -> new ResponseEntity<>(friendship, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>("Friendship not found", HttpStatus.NOT_FOUND));
    }

    // Endpoint to accept a friend request
    @PutMapping("/{id}/accept")
    public ResponseEntity<FriendShipEntity> acceptFriendship(@PathVariable Long id) {
        return friendShipService.acceptFriendship(id)
                .map(friendship -> new ResponseEntity<>(friendship, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }


    // Delete a friendship
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFriendship(@PathVariable Long id) {
        if (!friendShipService.getFriendshipById(id).isPresent()) {
            return new ResponseEntity<>("Friendship not found", NOT_FOUND);
        }
        friendShipService.deleteFriendship(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
