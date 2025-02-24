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

@Service
@Transactional
public class TimeCapsuleService {

    @Autowired
    private TimeCapsuleRepository tcRepo;

    @Autowired
    private UserRepository userRepository;

    public TimeCapsuleDTO createTimeCapsule(TimeCapsuleDTO dto, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        TimeCapsuleEntity capsule = new TimeCapsuleEntity();
        capsule.setTitle(dto.getTitle());
        capsule.setDescription(dto.getDescription());
        capsule.setCreatedAt(new Date());
        capsule.setOpenDate(dto.getOpenDate());
        capsule.setLocked(false);
        capsule.setCreatedBy(user);
        capsule.setStatus("ACTIVE");

        TimeCapsuleEntity savedCapsule = tcRepo.save(capsule);
        return convertToDTO(savedCapsule);
    }

    public TimeCapsuleDTO updateTimeCapsule(Long id, TimeCapsuleDTO dto) {
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        capsule.setTitle(dto.getTitle());
        capsule.setDescription(dto.getDescription());
        capsule.setOpenDate(dto.getOpenDate());

        TimeCapsuleEntity updatedCapsule = tcRepo.save(capsule);
        return convertToDTO(updatedCapsule);
    }

    public TimeCapsuleDTO getTimeCapsule(Long id) {
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));
        return convertToDTO(capsule);
    }

    public List<TimeCapsuleDTO> getUserTimeCapsules(Long userId) {
        List<TimeCapsuleEntity> capsules = tcRepo.findByCreatedById(userId);
        return capsules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<TimeCapsuleDTO> getAllTimeCapsules(Pageable pageable) {
        return tcRepo.findAll(pageable)
                .map(this::convertToDTO);
    }

    public void deleteTimeCapsule(Long id) {
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));
        tcRepo.delete(capsule);
    }

    public void lockTimeCapsule(Long id) {
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));
        capsule.setLocked(true);
        tcRepo.save(capsule);
    }

    public void unlockTimeCapsule(Long id) {
        TimeCapsuleEntity capsule = tcRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));
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
        dto.setLocked(entity.isLocked());
        dto.setCreatedById(entity.getCreatedBy().getId());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}

