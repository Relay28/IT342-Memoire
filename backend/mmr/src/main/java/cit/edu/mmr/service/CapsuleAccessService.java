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
import org.springframework.stereotype.Service;

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

    /**
     * Grant access to a time capsule for a specific user
     *
     * @param capsuleId ID of the time capsule
     * @param userId ID of the user to grant access to
     * @param uploaderId ID of the user granting access
     * @param role Role to be assigned
     * @return Created CapsuleAccessEntity
     * @throws EntityNotFoundException if capsule or users not found
     * @throws IllegalArgumentException if invalid parameters
     */
    public CapsuleAccessEntity grantAccess(Long capsuleId, Long userId, Long uploaderId, String role) {
        if (capsuleId == null || userId == null || uploaderId == null || role == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }

        TimeCapsuleEntity capsule = timeCapsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserEntity uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new EntityNotFoundException("Uploader not found"));

        // Check if access already exists
        Optional<CapsuleAccessEntity> existingAccess = capsuleAccessRepository
                .findByCapsuleAndUser(capsule, user);

        if (existingAccess.isPresent()) {
            throw new IllegalStateException("Access already exists for this user and capsule");
        }

        CapsuleAccessEntity access = new CapsuleAccessEntity();
        access.setCapsule(capsule);
        access.setUser(user);
        access.setUploadedBy(uploader);
        access.setRole(role);

        return capsuleAccessRepository.save(access);
    }

    /**
     * Update access role for a specific capsule access
     *
     * @param accessId ID of the capsule access to update
     * @param newRole New role to be assigned
     * @return Updated CapsuleAccessEntity
     * @throws EntityNotFoundException if access not found
     */
    public CapsuleAccessEntity updateAccessRole(Long accessId, String newRole) {
        CapsuleAccessEntity access = capsuleAccessRepository.findById(accessId)
                .orElseThrow(() -> new EntityNotFoundException("Capsule access not found"));

        access.setRole(newRole);
        return capsuleAccessRepository.save(access);
    }

    /**
     * Remove access for a specific capsule access entry
     *
     * @param accessId ID of the capsule access to remove
     * @throws EntityNotFoundException if access not found
     */
    public void removeAccess(Long accessId) {
        if (!capsuleAccessRepository.existsById(accessId)) {
            throw new EntityNotFoundException("Capsule access not found");
        }
        capsuleAccessRepository.deleteById(accessId);
    }

    /**
     * Get all access entries for a specific capsule
     *
     * @param capsuleId ID of the time capsule
     * @return List of CapsuleAccessEntity
     */
    public List<CapsuleAccessEntity> getAccessesByCapsule(Long capsuleId) {
        TimeCapsuleEntity capsule = timeCapsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        return capsuleAccessRepository.findByCapsule(capsule);
    }

    /**
     * Get all access entries for a specific user
     *
     * @param userId ID of the user
     * @return List of CapsuleAccessEntity
     */
    public List<CapsuleAccessEntity> getAccessesByUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

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
    public boolean hasAccess(Long userId, Long capsuleId, String role) {
        return capsuleAccessRepository.existsByUserIdAndCapsuleIdAndRole(userId, capsuleId, role);
    }
}
