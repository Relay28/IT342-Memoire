package cit.edu.mmr.service;


import cit.edu.mmr.dto.TimeCapsuleDTO;
import cit.edu.mmr.entity.NotificationEntity;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.TimeCapsuleRepository;
import cit.edu.mmr.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
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
    private final NotificationService notificationService;
    private final ScheduledExecutorService scheduler;

    public TimeCapsuleService(TimeCapsuleRepository tcRepo, UserRepository userRepository, NotificationService notificationService,ScheduledExecutorService scheduledExecutorService) {
        this.tcRepo = tcRepo;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.scheduler = scheduledExecutorService;
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
        capsule.setStatus("ACTIVE");

        return convertToDTO(tcRepo.save(capsule));
    }

    public TimeCapsuleDTO updateTimeCapsule(Long id, TimeCapsuleDTO dto, Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        if (!(capsule.getCreatedBy().getId() ==(user.getId()))) {
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

                notificationService.sendNotificationToUser(capsule.getCreatedBy().getId(), notification);
            }, delay, TimeUnit.MILLISECONDS);
        } else {
            // If open date is in the past, unlock immediately
            capsule.setLocked(false);
            tcRepo.save(capsule);
        }
    }

    public TimeCapsuleDTO getTimeCapsule(Long id) {
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));
        return convertToDTO(capsule);
    }

    public List<TimeCapsuleDTO> getUserTimeCapsules(Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);
        List<TimeCapsuleEntity> capsules = tcRepo.findByCreatedById(user.getId());
        return capsules.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Page<TimeCapsuleDTO> getAllTimeCapsules(Pageable pageable) {
        return tcRepo.findAll(pageable).map(this::convertToDTO);
    }

    public String deleteTimeCapsule(Long id, Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        if (!(capsule.getCreatedBy().getId() ==(user.getId()))) {
            throw new AccessDeniedException("You do not have permission to delete this capsule");
        }

        tcRepo.delete(capsule);
        return "Time Capsule Successfully Deleted";
    }

    public void lockTimeCapsule(Long id, Date openDate, Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        if (!(capsule.getCreatedBy().getId() ==(user.getId()))) {
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
        tcRepo.save(capsule);

        // Schedule automatic unlocking at the open date
        scheduleUnlockNotification(capsule);
    }

    private void scheduleUnlockNotification(TimeCapsuleEntity capsule) {
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

                notificationService.sendNotificationToUser(capsule.getCreatedBy().getId(), notification);
            }, delay, TimeUnit.MILLISECONDS);
        }
    }
    public void unlockTimeCapsule(Long id, Authentication authentication) {
        UserEntity user = getAuthenticatedUser(authentication);
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        if (!(capsule.getCreatedBy().getId() ==(user.getId()))) {
            throw new AccessDeniedException("You do not have permission to unlock this capsule");
        }

        capsule.setOpenDate(null);
        capsule.setLocked(false);
        tcRepo.save(capsule);
    }

    private TimeCapsuleDTO convertToDTO(TimeCapsuleEntity entity) {
        TimeCapsuleDTO dto = new TimeCapsuleDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setOpenDate(entity.getOpenDate());
        dto.setContents(entity.getContents());
        dto.setLocked(entity.isLocked());
        dto.setCreatedById(entity.getCreatedBy().getId());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
