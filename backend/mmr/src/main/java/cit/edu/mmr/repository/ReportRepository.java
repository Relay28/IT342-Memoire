package cit.edu.mmr.repository;

import cit.edu.mmr.entity.ReportEntity;
import cit.edu.mmr.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
    // Get all reports made by a specific user
    List<ReportEntity> findByReporter(UserEntity reporter);
    
    // Get reports by their current status
    List<ReportEntity> findByStatus(String status);
    
    // Get reports by the type of item reported
    List<ReportEntity> findByItemType(String itemType);
    
    // Get all reports for a specific item
    List<ReportEntity> findByReportedID(Long reportedId);
}