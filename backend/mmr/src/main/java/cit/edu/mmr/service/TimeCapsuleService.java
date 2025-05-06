package cit.edu.mmr.service;

import cit.edu.mmr.dto.CapsuleContentDTO;
import cit.edu.mmr.dto.TimeCapsuleDTO;
import cit.edu.mmr.entity.*;
import cit.edu.mmr.repository.CapsuleAccessRepository;
import cit.edu.mmr.repository.TimeCapsuleRepository;
import cit.edu.mmr.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.access.AccessDeniedException;

@Service
@Transactional
public class TimeCapsuleService {

    @Autowired
    private TimeCapsuleRepository tcRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CapsuleAccessRepository capsuleAccessRepository;

    @Autowired
    private final NotificationService notificationService;
    private final ScheduledExecutorService scheduler;

    public TimeCapsuleService(TimeCapsuleRepository tcRepo, UserRepository userRepository, NotificationService notificationService, ScheduledExecutorService scheduledExecutorService, CapsuleAccessRepository capsuleAccessRepository) {
        this.tcRepo = tcRepo;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.scheduler = scheduledExecutorService;
        this.capsuleAccessRepository = capsuleAccessRepository;
    }

    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public TimeCapsuleDTO createTimeCapsule(TimeCapsuleDTO dto, Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);

        TimeCapsuleEntity capsule = new TimeCapsuleEntity();
        capsule.setTitle(dto.getTitle());
        capsule.setDescription(dto.getDescription());
        capsule.setCreatedAt(new Date());
        capsule.setOpenDate(null);
        capsule.setLocked(false);
        capsule.setCreatedBy(user);
        capsule.setStatus("UNPUBLISHED");

        return convertToDTO(tcRepo.save(capsule));
    }
    public TimeCapsuleDTO getPublicCapsuleById(Long id) {
        // Find the capsule by ID
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        // Ensure the capsule is public and published
        if (!capsule.isPublic() || !"PUBLISHED".equals(capsule.getStatus())) {
            throw new AccessDeniedException("This capsule is not publicly accessible");
        }

        // Convert the entity to a DTO and return it
        return convertToDTO(capsule);
    }

    public TimeCapsuleDTO updateTimeCapsule(Long id, TimeCapsuleDTO dto, Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        if (!(capsule.getCreatedBy().getId() == (user.getId()))) {
            throw new AccessDeniedException("You do not have permission to update this capsule");
        }

        capsule.setTitle(dto.getTitle());
        capsule.setDescription(dto.getDescription());
        capsule.setOpenDate(dto.getOpenDate());

        return convertToDTO(tcRepo.save(capsule));
    }

    private void scheduleOpeningNotification(TimeCapsuleEntity capsule) {
        long delay = capsule.getOpenDate().getTime() - System.currentTimeMillis();

        if (delay > 0) {
            scheduler.schedule(() -> {
                // Unlock the capsule when open date arrives
                TimeCapsuleEntity updatedCapsule = tcRepo.findById(capsule.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

                updatedCapsule.setLocked(false);
                tcRepo.save(updatedCapsule);

                // Send notification
                NotificationEntity notification = new NotificationEntity();
                notification.setType("TIME_CAPSULE_OPEN");
                notification.setText("Your time capsule \"" + capsule.getTitle() + "\" is now available for viewing!");
                notification.setRelatedItemId(capsule.getId());
                notification.setItemType("TIME_CAPSULE");
                updatedCapsule.setStatus("PUBLISHED");
                notificationService.sendNotificationToUser(capsule.getCreatedBy().getId(), notification);
            }, delay, TimeUnit.MILLISECONDS);
        } else {
            // If open date is in the past, unlock immediately
            capsule.setLocked(false);
            tcRepo.save(capsule);
        }
    }

    public List<TimeCapsuleDTO> getPublicPublishedTimeCapsules() {
        List<TimeCapsuleEntity> publishedCapsules = tcRepo.findByStatus("PUBLISHED");
        return publishedCapsules.stream()
                .filter(TimeCapsuleEntity::isPublic) // Only include public capsules
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Transactional // Ensure this annotation is present
    public TimeCapsuleDTO getTimeCapsule(Long id, Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        // Check if user is the owner
        boolean isOwner = capsule.getCreatedBy().getId() == (user.getId());

        // If not owner, check if user has editor or viewer access
        boolean hasAccess = false;
        String accessRole = null;
        if (!isOwner) {
            Optional<CapsuleAccessEntity> accessOptional = capsuleAccessRepository
                    .findByCapsuleIdAndUserId(capsule.getId(), user.getId());

            if (accessOptional.isPresent()) {
                accessRole = accessOptional.get().getRole();
                hasAccess = "EDITOR".equals(accessRole) || "VIEWER".equals(accessRole);
            }
        }

        // For non-PUBLISHED capsules, only owners and editors can access
        if (!isOwner && (!hasAccess ||
                (!"PUBLISHED".equals(capsule.getStatus()) && "VIEWER".equals(accessRole)))) {
            throw new AccessDeniedException("You do not have permission to view this capsule");
        }

        // Explicitly initialize the contents collection to avoid LazyInitializationException
        // This should load the contents within the transaction
        capsule.getContents().size();

        return convertToDTO(capsule);
    }
    public List<TimeCapsuleDTO> getUserTimeCapsules(Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);

        // Get capsules owned by the user
        List<TimeCapsuleEntity> ownedCapsules = tcRepo.findByCreatedById(user.getId());

        // Get capsules where user has been granted access
        List<TimeCapsuleEntity> accessGrantedCapsules = capsuleAccessRepository
                .findByUserId(user.getId())
                .stream()
                .map(CapsuleAccessEntity::getCapsule)
                .collect(Collectors.toList());

        // Combine both lists (avoiding duplicates)
        Set<TimeCapsuleEntity> allAccessibleCapsules = new HashSet<>();
        allAccessibleCapsules.addAll(ownedCapsules);
        allAccessibleCapsules.addAll(accessGrantedCapsules);

        // Convert to DTOs
        return allAccessibleCapsules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<TimeCapsuleDTO> getAllTimeCapsules(Pageable pageable, Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);
        return tcRepo.findByCreatedById(user.getId(), pageable).map(this::convertToDTO);
    }

    public String deleteTimeCapsule(Long id, Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        if (!(capsule.getCreatedBy().getId() == (user.getId()))) {
            throw new AccessDeniedException("You do not have permission to delete this capsule");
        }

        tcRepo.delete(capsule);
        return "Time Capsule Successfully Deleted";
    }

    public void lockTimeCapsule(Long id, Date openDate, Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        if (!(capsule.getCreatedBy().getId() == (user.getId()))) {
            throw new AccessDeniedException("You do not have permission to lock this capsule");
        }

        if (capsule.isLocked()) {
            throw new IllegalStateException("Capsule is already locked");
        }

        if (openDate == null) {
            throw new IllegalArgumentException("Open date must be specified when locking");
        }

        if (openDate.before(new Date())) {
            throw new IllegalArgumentException("Open date must be in the future");
        }

        // Set open date and lock the capsule
        capsule.setOpenDate(openDate);
        capsule.setLocked(true);
        capsule.setStatus("CLOSED");
        tcRepo.save(capsule);

        // Schedule automatic unlocking at the open date
        scheduleUnlockNotification(capsule);
    }

    public void scheduleUnlockNotification(TimeCapsuleEntity capsule) {
        long delay = capsule.getOpenDate().getTime() - System.currentTimeMillis();

        if (delay > 0) {
            scheduler.schedule(() -> {
                // Unlock the capsule when open date arrives
                TimeCapsuleEntity updatedCapsule = tcRepo.findById(capsule.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

                updatedCapsule.setStatus("PUBLISHED");
                tcRepo.save(updatedCapsule);

                // Send notification
                NotificationEntity notification = new NotificationEntity();
                notification.setType("TIME_CAPSULE_OPEN");
                notification.setText("Your time capsule \"" + capsule.getTitle() + "\" is now available for viewing!");
                notification.setRelatedItemId(capsule.getId());
                notification.setItemType("TIME_CAPSULE");

                notificationService.sendNotificationToUser(capsule.getCreatedBy().getId(), notification);
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    public void unlockTimeCapsule(Long id, Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        if (!(capsule.getCreatedBy().getId() == (user.getId()))) {
            throw new AccessDeniedException("You do not have permission to unlock this capsule");
        }

        capsule.setOpenDate(null);
        capsule.setStatus("UNPUBLISHED");
        capsule.setLocked(false);
        if ("CLOSED".equals(capsule.getStatus())) {
            capsule.setStatus("UNPUBLISHED");
        }
        tcRepo.save(capsule);
    }

    private TimeCapsuleDTO convertToDTO(TimeCapsuleEntity entity) {
        TimeCapsuleDTO dto = new TimeCapsuleDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setOpenDate(entity.getOpenDate());
        dto.setLocked(entity.isLocked());
        dto.setCreatedById(entity.getCreatedBy().getId());
        dto.setStatus(entity.getStatus());

        // Convert entity contents to DTOs
        List<CapsuleContentDTO> contentDTOs = new ArrayList<>();
        if (entity.getContents() != null) {
            for (CapsuleContentEntity content : entity.getContents()) {
                CapsuleContentDTO contentDTO = new CapsuleContentDTO();
                contentDTO.setId(content.getId());
                contentDTO.setContentType(content.getContentType());
                contentDTO.setUploadedAt(content.getUploadedAt());
                if (content.getContentUploadedBy() != null) {
                    contentDTO.setContentUploadedById(content.getContentUploadedBy().getId());
                }
                contentDTOs.add(contentDTO);
            }
        }
        dto.setContents(contentDTOs);

        return dto;
    }
    // Method to get all time capsules user has access to (owned + shared with them)
    public List<TimeCapsuleDTO> getAllAccessibleTimeCapsules(Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);

        // Get capsules owned by the user
        List<TimeCapsuleEntity> ownedCapsules = tcRepo.findByCreatedById(user.getId());

        // Get capsules where user has been granted access
        List<TimeCapsuleEntity> accessGrantedCapsules = capsuleAccessRepository
                .findByUserId(user.getId())
                .stream()
                .map(CapsuleAccessEntity::getCapsule)
                .collect(Collectors.toList());

        // Combine both lists (avoiding duplicates)
        Set<TimeCapsuleEntity> allAccessibleCapsules = new HashSet<>();
        allAccessibleCapsules.addAll(ownedCapsules);
        allAccessibleCapsules.addAll(accessGrantedCapsules);

        // Convert to DTOs
        return allAccessibleCapsules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // New method to get capsules by status with proper access control
    public List<TimeCapsuleDTO> getTimeCapsulesByStatus(String status, Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);

        // Get all capsules with the specified status
        List<TimeCapsuleEntity> statusCapsules = tcRepo.findByStatus(status);

        // Filter based on access rights
        List<TimeCapsuleEntity> accessibleCapsules = new ArrayList<>();

        for (TimeCapsuleEntity capsule : statusCapsules) {
            boolean isOwner = capsule.getCreatedBy().getId() == user.getId();

            // For PUBLISHED status, include all capsules the user has access to
            if ("PUBLISHED".equals(status)) {
                if (isOwner) {
                    accessibleCapsules.add(capsule);
                    continue;
                }

                // Check if user has viewer or editor access
                Optional<CapsuleAccessEntity> accessOptional = capsuleAccessRepository
                        .findByCapsuleIdAndUserId(capsule.getId(), user.getId());

                if (accessOptional.isPresent()) {
                    accessibleCapsules.add(capsule);
                }
            }
            // For non-PUBLISHED statuses, only include if user is owner or has editor access
            else {
                if (isOwner) {
                    accessibleCapsules.add(capsule);
                    continue;
                }

                // Check if user has editor access
                Optional<CapsuleAccessEntity> accessOptional = capsuleAccessRepository
                        .findByCapsuleIdAndUserId(capsule.getId(), user.getId());

                if (accessOptional.isPresent() && "EDITOR".equals(accessOptional.get().getRole())) {
                    accessibleCapsules.add(capsule);
                }
            }
        }

        // Convert to DTOs
        return accessibleCapsules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void archiveTimeCapsule(Long id, Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        if (!(capsule.getCreatedBy().getId() == (user.getId()))) {
            throw new AccessDeniedException("You do not have permission to archive this capsule");
        }

        // Only allow archiving of published capsules
        if (!"PUBLISHED".equals(capsule.getStatus())) {
            throw new IllegalStateException("Only published capsules can be archived");
        }

        capsule.setStatus("ARCHIVED");
        tcRepo.save(capsule);
    }
    // Add this to your TimeCapsuleService.java
    public void publishTimeCapsule(Long id, Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        if (!(capsule.getCreatedBy().getId() == (user.getId()))) {
            throw new AccessDeniedException("You do not have permission to publish this capsule");
        }


        // Only allow publishing of archived capsules
        if (!"ARCHIVED".equals(capsule.getStatus())) {
            throw new IllegalStateException("Only archived capsules can be published");
        }

        capsule.setStatus("PUBLISHED");
        tcRepo.save(capsule);
    }
}