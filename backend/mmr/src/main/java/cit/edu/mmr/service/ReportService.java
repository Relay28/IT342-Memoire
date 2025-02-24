package cit.edu.mmr.service;


import cit.edu.mmr.entity.ReportEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /**
     * Creates a new report.
     *
     * @param reportedID the ID of the reported entity (regardless of type)
     * @param itemType the type of item being reported (e.g., "User", "Comment", etc.)
     * @param reporter the user reporting the item
     * @param status the status of the report (e.g., "Pending", "Reviewed")
     * @return the created ReportEntity
     */
    public ReportEntity createReport(long reportedID, String itemType, UserEntity reporter, String status) {
        ReportEntity report = new ReportEntity();
        report.setReportedID(reportedID);
        report.setItemType(itemType);
        report.setReporter(reporter);
        report.setStatus(status);
        return reportRepository.save(report);
    }

    /**
     * Retrieves a report by its id.
     *
     * @param id the report id
     * @return an Optional containing the report if found
     */
    public Optional<ReportEntity> getReportById(long id) {
        return reportRepository.findById(id);
    }

    /**
     * Retrieves all reports submitted by a given reporter.
     *
     * @param reporter the user who submitted the report
     * @return list of ReportEntity
     */
    public List<ReportEntity> getReportsByReporter(UserEntity reporter) {
        return reportRepository.findByReporter(reporter);
    }

    /**
     * Retrieves all reports for a given item type.
     *
     * @param itemType the type of item being reported
     * @return list of ReportEntity
     */
    public List<ReportEntity> getReportsByItemType(String itemType) {
        return reportRepository.findByItemType(itemType);
    }

    /**
     * Updates the status of an existing report.
     *
     * @param reportId the report id
     * @param newStatus the new status to set
     * @return an Optional containing the updated ReportEntity if found
     */
    public Optional<ReportEntity> updateReportStatus(long reportId, String newStatus) {
        Optional<ReportEntity> optionalReport = reportRepository.findById(reportId);
        if (optionalReport.isPresent()) {
            ReportEntity report = optionalReport.get();
            report.setStatus(newStatus);
            reportRepository.save(report);
            return Optional.of(report);
        }
        return Optional.empty();
    }

    /**
     * Deletes a report by its id.
     *
     * @param reportId the report id
     */
    public void deleteReport(long reportId) {
        reportRepository.deleteById(reportId);
    }
}
