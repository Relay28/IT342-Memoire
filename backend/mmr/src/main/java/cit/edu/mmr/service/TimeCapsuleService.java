package cit.edu.mmr.service;


import cit.edu.mmr.dto.TimeCapsuleDTO;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.TimeCapsuleRepository;
import cit.edu.mmr.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.access.AccessDeniedException;


@Service
@Transactional
public class TimeCapsuleService {

    @Autowired
    private TimeCapsuleRepository tcRepo;

    @Autowired
    private UserRepository userRepository;

    private UserEntity getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public TimeCapsuleDTO createTimeCapsule(TimeCapsuleDTO dto) {
        UserEntity user = getAuthenticatedUser();

        TimeCapsuleEntity capsule = new TimeCapsuleEntity();
        capsule.setTitle(dto.getTitle());
        capsule.setDescription(dto.getDescription());
        capsule.setCreatedAt(new Date());
        capsule.setOpenDate(dto.getOpenDate());
        capsule.setLocked(false);
        capsule.setCreatedBy(user);
        capsule.setStatus("ACTIVE");
        List<TimeCapsuleEntity> cap = user.getTimeCapsules();
        cap.add(capsule);
        user.setTimeCapsules(cap);

        return convertToDTO(tcRepo.save(capsule));
    }

    public TimeCapsuleDTO updateTimeCapsule(Long id, TimeCapsuleDTO dto) {
        UserEntity user = getAuthenticatedUser();
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

    public TimeCapsuleDTO getTimeCapsule(Long id) {
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));
        return convertToDTO(capsule);
    }

    public List<TimeCapsuleDTO> getUserTimeCapsules() {
        UserEntity user = getAuthenticatedUser();
        List<TimeCapsuleEntity> capsules = tcRepo.findByCreatedById(user.getId());
        return capsules.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Page<TimeCapsuleDTO> getAllTimeCapsules(Pageable pageable) {
        return tcRepo.findAll(pageable).map(this::convertToDTO);
    }

    public String deleteTimeCapsule(Long id) {
        UserEntity user = getAuthenticatedUser();
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        if (!(capsule.getCreatedBy().getId() ==(user.getId()))) {
            throw new AccessDeniedException("You do not have permission to delete this capsule");
        }

        tcRepo.delete(capsule);

       return "Time Capsule Successfully Deleted";
    }

    public void lockTimeCapsule(Long id) {
        UserEntity user = getAuthenticatedUser();
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        if (!(capsule.getCreatedBy().getId() ==(user.getId()))) {
            throw new AccessDeniedException("You do not have permission to lock this capsule");
        }

        capsule.setLocked(true);
        tcRepo.save(capsule);
    }

    public void unlockTimeCapsule(Long id) {
        UserEntity user = getAuthenticatedUser();
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));
        long usid = capsule.getCreatedBy().getId();
        if (!(capsule.getCreatedBy().getId() ==(user.getId()))) {
            throw new AccessDeniedException("You do not have permission to unlock this capsule");
        }

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
