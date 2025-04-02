package cit.edu.mmr.service.serviceInterfaces.report;

import cit.edu.mmr.entity.TimeCapsuleEntity;
import cit.edu.mmr.repository.TimeCapsuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimeCapsuleHandler implements ReportableEntity {
    private final TimeCapsuleRepository timeCapsuleRepository;

    @Autowired
    public TimeCapsuleHandler(TimeCapsuleRepository timeCapsuleRepository) {
        this.timeCapsuleRepository = timeCapsuleRepository;
    }

    @Override
    public Long getId() {
        return null; // Not used for the handler itself
    }

    @Override
    public String getEntityType() {
        return "TimeCapsule";
    }

    @Override
    public void validate() throws IllegalArgumentException {
        // Validation logic specific to TimeCapsule
    }

    public TimeCapsuleEntity getEntity(Long id) {
        return timeCapsuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TimeCapsule not found with id: " + id));
    }
}
