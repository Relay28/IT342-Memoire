package cit.edu.mmr.service;


import cit.edu.mmr.entity.ReportEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.ReportRepository;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.serviceInterfaces.report.CommentHandler;
import cit.edu.mmr.service.serviceInterfaces.report.ReportEntityFactory;
import cit.edu.mmr.service.serviceInterfaces.report.ReportableEntity;
import cit.edu.mmr.service.serviceInterfaces.report.TimeCapsuleHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportEntityFactory entityFactory;
    private final UserRepository userRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository, ReportEntityFactory entityFactory, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.entityFactory = entityFactory;
        this.userRepository = userRepository;
    }

    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    /**
     * Creates a new report with entity validation.
     */
    public ReportEntity createReport(long reportedID, String itemType, String status,Authentication auth) {
       UserEntity reporter = getAuthenticatedUser(auth);
        // Get the appropriate handler
        ReportableEntity handler = entityFactory.getHandler(itemType);

        // Validate the entity exists
        if (handler instanceof TimeCapsuleHandler) {
            ((TimeCapsuleHandler) handler).getEntity(reportedID);
        } else if (handler instanceof CommentHandler) {
            ((CommentHandler) handler).getEntity(reportedID);
        }

        // Create the report
        ReportEntity report = new ReportEntity();
        report.setReportedID(reportedID);
        report.setItemType(itemType);
        report.setReporter(reporter);
        report.setStatus(status);
        return reportRepository.save(report);
    }

    /**
     * Gets the reported entity with proper typing.
     */
    public Object getReportedEntity(long reportId) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        ReportableEntity handler = entityFactory.getHandler(report.getItemType());

        if (handler instanceof TimeCapsuleHandler) {
            return ((TimeCapsuleHandler) handler).getEntity(report.getReportedID());
        } else if (handler instanceof CommentHandler) {
            return ((CommentHandler) handler).getEntity(report.getReportedID());
        }

        throw new IllegalStateException("Unsupported entity type: " + report.getItemType());
    }

    // Existing methods remain unchanged...
    public Optional<ReportEntity> getReportById(long id) {
        return reportRepository.findById(id);
    }

    public List<ReportEntity> getReportsByReporter(UserEntity reporter) {
        return reportRepository.findByReporter(reporter);
    }

    public List<ReportEntity> getReportsByItemType(String itemType) {
        return reportRepository.findByItemType(itemType);
    }

    public Optional<ReportEntity> updateReportStatus(long reportId, String newStatus, Authentication auth) {
        Optional<ReportEntity> optionalReport = reportRepository.findById(reportId);
        if (optionalReport.isPresent()) {
            ReportEntity report = optionalReport.get();
            report.setStatus(newStatus);
            reportRepository.save(report);
            return Optional.of(report);
        }
        return Optional.empty();
    }

    public void deleteReport(long reportId) {
        reportRepository.deleteById(reportId);
    }
}