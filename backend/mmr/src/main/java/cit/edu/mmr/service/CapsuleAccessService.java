package cit.edu.mmr.service;

import cit.edu.mmr.entity.CapsuleAccessEntity;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.CapsuleAccessRepository;
import cit.edu.mmr.repository.TimeCapsuleRepository;
import cit.edu.mmr.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.List;
import java.util.Optional;
@Service
@Transactional
public class CapsuleAccessService {
    private final CapsuleAccessRepository capsuleAccessRepository;
    private final TimeCapsuleRepository timeCapsuleRepository;
    private final UserRepository userRepository;

    @Autowired
    public CapsuleAccessService(CapsuleAccessRepository capsuleAccessRepository,
                                TimeCapsuleRepository timeCapsuleRepository,
                                UserRepository userRepository) {
        this.capsuleAccessRepository = capsuleAccessRepository;
        this.timeCapsuleRepository = timeCapsuleRepository;
        this.userRepository = userRepository;
    }

    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Grant access to a time capsule for a specific user
     *
     * @param capsuleId      ID of the time capsule
     * @param userId         ID of the user to grant access to
     * @param uploaderId     ID of the user granting access = currentUser
     * @param role           Role to be assigned
     * @param authentication
     * @return Created CapsuleAccessEntity
     * @throws EntityNotFoundException  if capsule or users not found
     * @throws IllegalArgumentException if invalid parameters
     */
    public CapsuleAccessEntity grantAccess(Long capsuleId, Long userId, String role, Authentication authentication) {
        UserEntity currentUser = getAuthenticatedUser(authentication);
        if (capsuleId == null || userId == null || role == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }

        TimeCapsuleEntity capsule = timeCapsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));


        // Check if access already exists
        Optional<CapsuleAccessEntity> existingAccess = capsuleAccessRepository
                .findByCapsuleAndUser(capsule, user);


        if (existingAccess.isPresent()) {
            throw new IllegalStateException("Access already exists for this user and capsule");
        }

        CapsuleAccessEntity access = new CapsuleAccessEntity();
        access.setCapsule(capsule);
        access.setUser(user);
        access.setUploadedBy(currentUser);
        access.setRole(role);
        List<CapsuleAccessEntity> caps = capsule.getCapsuleAccesses();
        caps.add(access);
        capsule.setCapsuleAccesses(caps);

        return capsuleAccessRepository.save(access);
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
     */public CapsuleAccessEntity updateAccessRole(Long accessId, String newRole, Authentication authentication) {
        UserEntity currentUser = getAuthenticatedUser(authentication);
        CapsuleAccessEntity access = capsuleAccessRepository.findById(accessId)
                .orElseThrow(() -> new EntityNotFoundException("Capsule access not found"));

        if (!access.getUploadedBy().equals(currentUser)) {
            throw new AccessDeniedException("Only the user who granted this access can modify it");
        }

        // Validate the role
        if (!"EDITOR".equals(newRole) && !"VIEWER".equals(newRole)) {
            throw new IllegalArgumentException("Role must be either EDITOR or VIEWER");
        }

        access.setRole(newRole);
        return capsuleAccessRepository.save(access);
    }
    /**
     * Remove access for a specific capsule access entry
     *
     * @param accessId       ID of the capsule access to remove
     * @param authentication
     * @throws EntityNotFoundException if access not found
     */
    public void removeAccess(Long accessId, Authentication authentication) {
        UserEntity currentUser = getAuthenticatedUser(authentication);
        CapsuleAccessEntity access = capsuleAccessRepository.findById(accessId)
                .orElseThrow(() -> new EntityNotFoundException("Capsule access not found"));

        // Optional: Add authorization check here if needed
        capsuleAccessRepository.deleteById(accessId);
    }

    /**
     * Get all access entries for a specific capsule
     *
     * @param capsuleId      ID of the time capsule
     * @param authentication
     * @return List of CapsuleAccessEntity
     */
    public List<CapsuleAccessEntity> getAccessesByCapsule(Long capsuleId, Authentication authentication) {
        UserEntity currentUser = getAuthenticatedUser(authentication);
        TimeCapsuleEntity capsule = timeCapsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        // Optional: Add authorization check here if needed
        return capsuleAccessRepository.findByCapsule(capsule);
    }

    /**
     * Get all access entries for a specific user
     *
     * @param userId ID of the user
     * @param auth
     * @return List of CapsuleAccessEntity
     */
    public List<CapsuleAccessEntity> getAccessesByUser(Long userId, Authentication auth) {
        UserEntity currentUser = getAuthenticatedUser(auth);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Optional: Add authorization check here if needed
        return capsuleAccessRepository.findByUser(user);
    }

    /**
     * Check if a user has specific access to a capsule
     *
     * @param userId ID of the user
     * @param capsuleId ID of the time capsule
     * @param role Role to check for
     * @return boolean indicating if user has the specified access
     */
    public boolean hasAccess(Long capsuleId, String role, Authentication auth) {
        UserEntity user = getAuthenticatedUser(auth);
        return capsuleAccessRepository.existsByUserIdAndCapsuleIdAndRole(user.getId(), capsuleId, role);
    }
}