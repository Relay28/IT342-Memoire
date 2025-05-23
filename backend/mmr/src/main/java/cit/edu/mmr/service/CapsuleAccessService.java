package cit.edu.mmr.service;

import cit.edu.mmr.entity.CapsuleAccessEntity;
import cit.edu.mmr.entity.FriendShipEntity;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.exception.exceptions.DatabaseOperationException;
import cit.edu.mmr.repository.CapsuleAccessRepository;
import cit.edu.mmr.repository.FriendShipRepository;
import cit.edu.mmr.repository.TimeCapsuleRepository;
import cit.edu.mmr.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Transactional
public class  CapsuleAccessService {
    private static final Logger logger = LoggerFactory.getLogger(CapsuleAccessService.class);

    private final CapsuleAccessRepository capsuleAccessRepository;
    private final TimeCapsuleRepository timeCapsuleRepository;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    private final FriendShipRepository friendShipRepository;

    @Autowired
    public CapsuleAccessService(CapsuleAccessRepository capsuleAccessRepository,
                                TimeCapsuleRepository timeCapsuleRepository,
                                UserRepository userRepository,
                                CacheManager cacheManager, FriendShipRepository friendShipRepository) {
        this.capsuleAccessRepository = capsuleAccessRepository;
        this.timeCapsuleRepository = timeCapsuleRepository;
        this.userRepository = userRepository;
        this.cacheManager = cacheManager;
        this.friendShipRepository = friendShipRepository;
    }


    public void restrictAccessToOwner(Long capsuleId, Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);
        TimeCapsuleEntity capsule = timeCapsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        if (!(capsule.getCreatedBy().getId() ==(user.getId()))) {
            throw new AccessDeniedException("You do not have permission to modify access for this capsule");
        }
        capsule.setLocked(false);

        capsuleAccessRepository.deleteByCapsuleId(capsuleId); // Delete all access entries
    }

    public void grantPublicAccess(Long capsuleId, Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);
        TimeCapsuleEntity capsule = timeCapsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        if (!(capsule.getCreatedBy().getId() ==(user.getId()))) {
            throw new AccessDeniedException("You do not have permission to modify access for this capsule");
        }

        // Ensure the capsule is published
//        if (!"PUBLISHED".equals(capsule.getStatus())) {
//            throw new IllegalStateException("Only published capsules can be made public");
//        }

        // Set the capsule as public
        capsule.setPublic(true);
        timeCapsuleRepository.save(capsule);
    }
    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        logger.debug("Getting authenticated user: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found during authentication: {}", username);
                    return new UsernameNotFoundException("User not found");
                });
    }

    /**
     * Grant access to a time capsule for a specific user
     *
     * @param capsuleId      ID of the time capsule
     * @param userId         ID of the user to grant access to
     * @param role           Role to be assigned
     * @param authentication Current authenticated user
     * @return Created CapsuleAccessEntity
     * @throws EntityNotFoundException  if capsule or users not found
     * @throws IllegalArgumentException if invalid parameters
     */
    @Caching(evict = {
            @CacheEvict(value = "contentMetadata", key = "'capsuleAccesses_' + #capsuleId"),
            @CacheEvict(value = "contentMetadata", key = "'userAccesses_' + #userId"),
            @CacheEvict(value = "contentMetadata", key = "'hasAccess_' + #capsuleId + '_*'", allEntries = true)
    })
    public CapsuleAccessEntity grantAccess(Long capsuleId, Long userId, String role, Authentication authentication) {
        logger.info("Granting {} access to capsule ID {} for user ID {}", role, capsuleId, userId);

        // Validate parameters
        if (capsuleId == null || userId == null || role == null) {
            logger.warn("Invalid parameters for grantAccess: capsuleId={}, userId={}, role={}", capsuleId, userId, role);
            throw new IllegalArgumentException("All parameters must be non-null");
        }

        // Validate role
        if (!"EDITOR".equals(role) && !"VIEWER".equals(role)) {
            logger.warn("Invalid role provided: {}", role);
            throw new IllegalArgumentException("Role must be either EDITOR or VIEWER");
        }

        UserEntity currentUser = getAuthenticatedUser(authentication);

        // Get capsule
        TimeCapsuleEntity capsule;
        try {
            capsule = timeCapsuleRepository.findById(capsuleId)
                    .orElseThrow(() -> new EntityNotFoundException("Time capsule not found with ID: " + capsuleId));
            logger.debug("Found time capsule: {}", capsule.getId());
        } catch (Exception e) {
            logger.error("Error retrieving time capsule with ID {}: {}", capsuleId, e.getMessage());
            throw new DatabaseOperationException("Error retrieving time capsule", e);
        }

        // Get target user
        UserEntity user;
        try {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
            logger.debug("Found target user: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Error retrieving user with ID {}: {}", userId, e.getMessage());
            throw new DatabaseOperationException("Error retrieving user", e);
        }

        // Check for existing access
        try {
            Optional<CapsuleAccessEntity> existingAccess = capsuleAccessRepository
                    .findByCapsuleAndUser(capsule, user);

            if (existingAccess.isPresent()) {
                logger.warn("Access already exists for user ID {} and capsule ID {}", userId, capsuleId);
                throw new IllegalStateException("Access already exists for this user and capsule");
            }
        } catch (IllegalStateException e) {
            // Re-throw the specific exception
            throw e;
        } catch (Exception e) {
            logger.error("Error checking existing access: {}", e.getMessage());
            throw new DatabaseOperationException("Error checking existing access", e);
        }

        if(!currentUser.equals(capsule.getCreatedBy())){
            throw new AccessDeniedException("Access Denied, User "+currentUser.getId()+" Has No Access Rights To Capsule "+capsule.getId());
        }

        // Create new access
        try {
            CapsuleAccessEntity access = new CapsuleAccessEntity();
            access.setCapsule(capsule);
            access.setUser(user);
            access.setUploadedBy(currentUser);
            access.setRole(role);

            List<CapsuleAccessEntity> caps = capsule.getCapsuleAccesses();
            caps.add(access);
            capsule.setCapsuleAccesses(caps);

            CapsuleAccessEntity savedAccess = capsuleAccessRepository.save(access);
            logger.info("Successfully granted {} access to capsule ID {} for user ID {}", role, capsuleId, userId);
            return savedAccess;
        } catch (Exception e) {
            logger.error("Error saving capsule access: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error saving capsule access", e);
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "contentMetadata", key = "'capsuleAccesses_' + #capsuleId"),
            @CacheEvict(value = "contentMetadata", key = "'userAccesses_*'", allEntries = true),
            @CacheEvict(value = "contentMetadata", key = "'hasAccess_' + #capsuleId + '_*'", allEntries = true)
    })
    @Transactional
    public List<CapsuleAccessEntity> grantAccessToAllFriends(Long capsuleId, String role, Authentication authentication) {
        logger.info("Granting {} access to capsule {} for all friends", role, capsuleId);

        // Validate parameters
        if (capsuleId == null || role == null) {
            logger.warn("Invalid parameters for grantAccessToAllFriends: capsuleId={}, role={}", capsuleId, role);
            throw new IllegalArgumentException("Capsule ID and role must be non-null");
        }

        // Validate role
        if (!"EDITOR".equals(role) && !"VIEWER".equals(role)) {
            logger.warn("Invalid role provided: {}", role);
            throw new IllegalArgumentException("Role must be either EDITOR or VIEWER");
        }

        UserEntity currentUser = getAuthenticatedUser(authentication);

        // Get capsule
        TimeCapsuleEntity capsule = timeCapsuleRepository.findById(capsuleId)
                .orElseThrow(() -> {
                    logger.error("Time capsule not found with ID: {}", capsuleId);
                    return new EntityNotFoundException("Time capsule not found with ID: " + capsuleId);
                });

        // Verify current user is the owner of the capsule
        if (!capsule.getCreatedBy().equals(currentUser)) {
            logger.warn("User {} attempted to grant access to capsule {} they don't own",
                    currentUser.getUsername(), capsuleId);
            throw new AccessDeniedException("Only the capsule owner can grant access");
        }

        // Get all friends with status "Friends"
        List<FriendShipEntity> friendships = friendShipRepository.findByUserOrFriendAndStatus(
                currentUser, currentUser, "Friends");

        Set<UserEntity> friends = new HashSet<>();
        for (FriendShipEntity friendship : friendships) {
            if (friendship.getUser().equals(currentUser)) {
                friends.add(friendship.getFriend());
            } else {
                friends.add(friendship.getUser());
            }
        }

        logger.debug("Found {} friends to grant access to", friends.size());

        // Filter out friends who already have access
        List<Long> existingUserIds = capsuleAccessRepository.findUserIdsWithAccessToCapsule(capsuleId);
        friends.removeIf(friend -> existingUserIds.contains(friend.getId()));

        // Grant access to each friend
        List<CapsuleAccessEntity> createdAccesses = new ArrayList<>();
        for (UserEntity friend : friends) {
            try {
                CapsuleAccessEntity access = new CapsuleAccessEntity();
                access.setCapsule(capsule);
                access.setUser(friend);
                access.setUploadedBy(currentUser);
                access.setRole("VIEWER");

                CapsuleAccessEntity savedAccess = capsuleAccessRepository.save(access);
                createdAccesses.add(savedAccess);

                // Add to capsule's access list
                capsule.getCapsuleAccesses().add(savedAccess);

                logger.debug("Granted {} access to friend {}", role, friend.getUsername());
            } catch (Exception e) {
                logger.error("Error granting access to friend {}: {}", friend.getUsername(), e.getMessage());
                // Continue with other friends even if one fails
            }
        }

        // Save the capsule with updated accesses
        timeCapsuleRepository.save(capsule);

        logger.info("Successfully granted access to {} friends for capsule {}", createdAccesses.size(), capsuleId);
        return createdAccesses;
    }

    /**
     * Update access role for a specific capsule access
     *
     * @param accessId ID of the capsule access to update
     * @param newRole New role to be assigned
     * @param authentication Current authenticated user
     * @return Updated CapsuleAccessEntity
     * @throws EntityNotFoundException if access not found
     * @throws AccessDeniedException if current user is not the uploader
     */
    @Caching(evict = {
            @CacheEvict(value = "contentMetadata", key = "'capsuleAccesses_' + #access.capsule.id"),
            @CacheEvict(value = "contentMetadata", key = "'userAccesses_' + #access.user.id"),
            @CacheEvict(value = "contentMetadata", key = "'hasAccess_' + #access.capsule.id + '_*'", allEntries = true)
    })
    public CapsuleAccessEntity updateAccessRole(Long accessId, String newRole, Authentication authentication) {
        logger.info("Updating access role for access ID {} to {}", accessId, newRole);

        if (accessId == null || newRole == null) {
            logger.warn("Invalid parameters for updateAccessRole: accessId={}, newRole={}", accessId, newRole);
            throw new IllegalArgumentException("Access ID and new role must not be null");
        }

        UserEntity currentUser = getAuthenticatedUser(authentication);

        // Get access entity
        CapsuleAccessEntity access;
        try {
            access = capsuleAccessRepository.findById(accessId)
                    .orElseThrow(() -> new EntityNotFoundException("Capsule access not found with ID: " + accessId));
            logger.debug("Found capsule access entry: {}", access.getId());
        } catch (Exception e) {
            logger.error("Error retrieving capsule access with ID {}: {}", accessId, e.getMessage());
            throw new DatabaseOperationException("Error retrieving capsule access", e);
        }

        // Check authorization
        if (!access.getUploadedBy().equals(currentUser)) {
            logger.warn("User {} attempted to modify access {} created by user {}",
                    currentUser.getUsername(), accessId, access.getUploadedBy().getUsername());
            throw new AccessDeniedException("Only the user who granted this access can modify it");
        }

        // Validate the role
        if (!"EDITOR".equals(newRole) && !"VIEWER".equals(newRole)) {
            logger.warn("Invalid role provided: {}", newRole);
            throw new IllegalArgumentException("Role must be either EDITOR or VIEWER");
        }

        // Update role
        try {
            access.setRole(newRole);
            CapsuleAccessEntity updatedAccess = capsuleAccessRepository.save(access);
            logger.info("Successfully updated access role to {} for access ID {}", newRole, accessId);
            return updatedAccess;
        } catch (Exception e) {
            logger.error("Error updating capsule access role: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error updating capsule access role", e);
        }
    }

    /**
     * Remove access for a specific capsule access entry
     *
     * @param accessId ID of the capsule access to remove
     * @param authentication Current authenticated user
     * @throws EntityNotFoundException if access not found
     * @throws AccessDeniedException if current user is not authorized
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "contentMetadata", key = "'capsuleAccesses_' + #capsule.id"),
            @CacheEvict(value = "contentMetadata", key = "'userAccesses_' + #access.user.id"),
            @CacheEvict(value = "contentMetadata", key = "'hasAccess_' + #capsule.id + '_*'", allEntries = true)
    })
    public void removeAccess(Long accessId, Authentication authentication) {
        logger.info("Removing access for access ID {}", accessId);

        if (accessId == null) {
            throw new IllegalArgumentException("Access ID must not be null");
        }

        try {
            UserEntity currentUser = getAuthenticatedUser(authentication);

            // Fetch access entity
            CapsuleAccessEntity access = capsuleAccessRepository.findById(accessId)
                    .orElseThrow(() -> new EntityNotFoundException("Capsule access not found with ID: " + accessId));

            TimeCapsuleEntity capsule = timeCapsuleRepository.findById(access.getCapsule().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Time capsule not found with ID: " + access.getCapsule().getId()));

            // Check authorization
            if (!access.getUploadedBy().equals(currentUser) && !currentUser.equals(capsule.getCreatedBy())) {
                throw new AccessDeniedException("Only the user who granted this access can remove it");
            }

            // Delete access
            capsuleAccessRepository.deleteById(accessId);

            // Evict caches
            cacheManager.getCache("contentMetadata").evict("capsuleAccesses_" + capsule.getId());
            cacheManager.getCache("contentMetadata").evict("userAccesses_" + access.getUser().getId());
            cacheManager.getCache("contentMetadata").evict("hasAccess_" + capsule.getId() + "_*");

            logger.info("Successfully removed access ID {}", accessId);
        } catch (EntityNotFoundException e) {
            logger.warn("Access ID {} not found during deletion, possibly already deleted", accessId);
        } catch (Exception e) {
            logger.error("Error removing capsule access: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Error removing capsule access", e);
        }
    }

    /**
     * Get all access entries for a specific capsule
     *
     * @param capsuleId ID of the time capsule
     * @param authentication Current authenticated user
     * @return List of CapsuleAccessEntity
     * @throws EntityNotFoundException if capsule not found
     */
    @Cacheable(value = "contentMetadata", key = "'capsuleAccesses_' + #capsuleId")
    public List<CapsuleAccessEntity> getAccessesByCapsule(Long capsuleId, Authentication authentication) {
        logger.info("Getting accesses for capsule ID {}", capsuleId);

        if (capsuleId == null) {
            logger.warn("Invalid parameter for getAccessesByCapsule: capsuleId is null");
            throw new IllegalArgumentException("Capsule ID must not be null");
        }

        UserEntity currentUser = getAuthenticatedUser(authentication);

        // Get capsule
        TimeCapsuleEntity capsule;
        try {
            capsule = timeCapsuleRepository.findById(capsuleId)
                    .orElseThrow(() -> new EntityNotFoundException("Time capsule not found with ID: " + capsuleId));
            logger.debug("Found time capsule: {}", capsule.getId());
        } catch (Exception e) {
            logger.error("Error retrieving time capsule with ID {}: {}", capsuleId, e.getMessage());
            throw new DatabaseOperationException("Error retrieving time capsule", e);
        }

        // Get accesses
        try {
            List<CapsuleAccessEntity> accesses = capsuleAccessRepository.findByCapsule(capsule);
            logger.info("Retrieved {} access entries for capsule ID {}", accesses.size(), capsuleId);
            return accesses;
        } catch (Exception e) {
            logger.error("Error retrieving accesses for capsule ID {}: {}", capsuleId, e.getMessage(), e);
            throw new DatabaseOperationException("Error retrieving capsule accesses", e);
        }
    }

    /**
     * Get all access entries for a specific user
     *
     * @param userId ID of the user
     * @param auth Current authenticated user
     * @return List of CapsuleAccessEntity
     * @throws EntityNotFoundException if user not found
     */
    @Cacheable(value = "contentMetadata", key = "'userAccesses_' + #userId")
    public List<CapsuleAccessEntity> getAccessesByUser(Long userId, Authentication auth) {
        logger.info("Getting accesses for user ID {}", userId);

        if (userId == null) {
            logger.warn("Invalid parameter for getAccessesByUser: userId is null");
            throw new IllegalArgumentException("User ID must not be null");
        }

        UserEntity currentUser = getAuthenticatedUser(auth);

        // Get user
        UserEntity user;
        try {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
            logger.debug("Found user: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Error retrieving user with ID {}: {}", userId, e.getMessage());
            throw new DatabaseOperationException("Error retrieving user", e);
        }

        // Get accesses
        try {
            List<CapsuleAccessEntity> accesses = capsuleAccessRepository.findByUser(user);
            logger.info("Retrieved {} access entries for user ID {}", accesses.size(), userId);
            return accesses;
        } catch (Exception e) {
            logger.error("Error retrieving accesses for user ID {}: {}", userId, e.getMessage(), e);
            throw new DatabaseOperationException("Error retrieving user accesses", e);
        }
    }




    /**
     * Check if a user has specific access to a capsule
     *
     * @param capsuleId ID of the time capsule
     * @param role Role to check for
     * @param auth Current authenticated user
     * @return boolean indicating if user has the specified access
     */
    @Cacheable(value = "contentMetadata", key = "'hasAccess_' + #capsuleId + '_' + #auth.name + '_' + #role")
    public boolean hasAccess(Long capsuleId, String role, Authentication auth) {
        logger.info("Checking {} access for capsule ID {}", role, capsuleId);

        if (capsuleId == null || role == null) {
            logger.warn("Invalid parameters for hasAccess: capsuleId={}, role={}", capsuleId, role);
            throw new IllegalArgumentException("Capsule ID and role must not be null");
        }

        UserEntity user = getAuthenticatedUser(auth);

        try {
            boolean hasAccess = capsuleAccessRepository.existsByUserIdAndCapsuleIdAndRole(user.getId(), capsuleId, role);
            logger.info("User {} {} {} access to capsule ID {}",
                    user.getUsername(), hasAccess ? "has" : "does not have", role, capsuleId);
            return hasAccess;
        } catch (Exception e) {
            logger.error("Error checking access for user ID {} and capsule ID {}: {}",
                    user.getId(), capsuleId, e.getMessage(), e);
            throw new DatabaseOperationException("Error checking capsule access", e);
        }
    }
}