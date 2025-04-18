package cit.edu.mmr.service;


import cit.edu.mmr.entity.ReportEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.ReportRepository;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.serviceInterfaces.report.CommentHandler;
import cit.edu.mmr.service.serviceInterfaces.report.ReportEntityFactory;
import cit.edu.mmr.service.serviceInterfaces.report.ReportableEntity;
import cit.edu.mmr.service.serviceInterfaces.report.TimeCapsuleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
@Service
public class ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final ReportRepository reportRepository;
    private final ReportEntityFactory entityFactory;
    private final UserRepository userRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository,
                         ReportEntityFactory entityFactory,
                         UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.entityFactory = entityFactory;
        this.userRepository = userRepository;
    }

    @CacheEvict(value = "reports", allEntries = true)
    public ReportEntity createReport(long reportedID, String itemType, String status, Authentication auth) {
        UserEntity reporter = getAuthenticatedUser(auth);

        ReportableEntity handler = entityFactory.getHandler(itemType);
        if (handler instanceof TimeCapsuleHandler) {
            ((TimeCapsuleHandler) handler).getEntity(reportedID);
        } else if (handler instanceof CommentHandler) {
            ((CommentHandler) handler).getEntity(reportedID);
        } else {
            throw new IllegalStateException("Unsupported handler for itemType: " + itemType);
        }

        ReportEntity report = new ReportEntity();
        report.setReportedID(reportedID);
        report.setItemType(itemType);
        report.setDate((new Date()));
        report.setReporter(reporter);
        report.setStatus(status);

        logger.info("Creating report: {}", report);
        return reportRepository.save(report);
    }

    @Cacheable(value = "reportedEntities", key = "'reportedEntity_' + #reportId + '_' + @reportService.getReportById(#reportId).orElse(null)?.itemType")
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

    @Cacheable(value = "reports", key = "'report_' + #id")
    public Optional<ReportEntity> getReportById(long id) {
        return reportRepository.findById(id);
    }

    @Cacheable(value = "reports", key = "'reporterReports_' + #reporter.id")
    public List<ReportEntity> getReportsByReporter(UserEntity reporter) {
        return reportRepository.findByReporter(reporter);
    }

    @Cacheable(value = "reports", key = "'reportsByType_' + #itemType")
    public List<ReportEntity> getReportsByItemType(String itemType) {
        return reportRepository.findByItemType(itemType);
    }

    public List<ReportEntity> getReports(Authentication auth) {
        List<ReportEntity> list = null;
      UserEntity user=  getAuthenticatedUser(auth);

          try {
              if(!user.getRole().equals("ROLE_ADMIN"))
                 throw new AccessDeniedException("ACCESS DENIED");
              else
                  list = reportRepository.findAll();
          } catch (AccessDeniedException e) {
              throw new RuntimeException(e);
          }

       return list;
    }

    @Caching(evict = {
            @CacheEvict(value = "reports", key = "'report_' + #reportId"),
            @CacheEvict(value = "reports", key = "'reporterReports_' + @reportService.getReportById(#reportId).orElse(null)?.reporter.id"),
            @CacheEvict(value = "reports", key = "'reportsByType_' + @reportService.getReportById(#reportId).orElse(null)?.itemType")
    })
    public Optional<ReportEntity> updateReportStatus(long reportId, String newStatus, Authentication auth) {
        Optional<ReportEntity> optionalReport = reportRepository.findById(reportId);
        if (optionalReport.isPresent()) {
            ReportEntity report = optionalReport.get();
            report.setStatus(newStatus);
            logger.info("Updating report status. Report ID: {}, New Status: {}", reportId, newStatus);
            return Optional.of(reportRepository.save(report));
        }
        return Optional.empty();
    }

    @Caching(evict = {
            @CacheEvict(value = "reports", key = "'report_' + #reportId"),
            @CacheEvict(value = "reportedEntities", key = "'reportedEntity_' + #reportId + '_*'"),
            @CacheEvict(value = "reports", key = "'reporterReports_' + @reportService.getReportById(#reportId).orElse(null)?.reporter.id"),
            @CacheEvict(value = "reports", key = "'reportsByType_' + @reportService.getReportById(#reportId).orElse(null)?.itemType")
    })
    public void deleteReport(long reportId) {
        if (!reportRepository.existsById(reportId)) {
            throw new IllegalArgumentException("Report not found");
        }
        logger.info("Deleting report with ID: {}", reportId);
        reportRepository.deleteById(reportId);
    }

    private UserEntity getAuthenticatedUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));
    }
}