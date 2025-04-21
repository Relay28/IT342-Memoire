package cit.edu.mmr.service;

import cit.edu.mmr.dto.CountdownDTO;
import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.repository.TimeCapsuleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CountdownService {

    private final TimeCapsuleRepository tcRepo;
    private final TimeCapsuleService tcServ;

    @Autowired
    public CountdownService(TimeCapsuleRepository tcRepo, TimeCapsuleService tcServ) {
        this.tcRepo = tcRepo;
        this.tcServ = tcServ;
    }

    public CountdownDTO getTimeUntilOpening(Long capsuleId) {
        TimeCapsuleEntity capsule = tcRepo.findById(capsuleId)
                .orElseThrow(() -> new EntityNotFoundException("Time capsule not found"));

        if (!capsule.isLocked()) {
            throw new IllegalStateException("Capsule is not locked - no countdown available");
        }

        if (capsule.getOpenDate() == null) {
            throw new IllegalStateException("Capsule has no open date set");
        }

        long remainingMillis = capsule.getOpenDate().getTime() - System.currentTimeMillis();

        if (remainingMillis <= 0) {
            tcServ.scheduleUnlockNotification(capsule);
            return new CountdownDTO(0, 0, 0, 0, true);
        }

        return calculateCountdown(remainingMillis);
    }

    private CountdownDTO calculateCountdown(long millis) {
        long seconds = millis / 1000;
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        return new CountdownDTO(days, hours, minutes, (int) seconds, false);
    }
}
