package cit.edu.mmr.controller;

import cit.edu.mmr.dto.ReportRequest;
import cit.edu.mmr.entity.ReportEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.ReportService;
import cit.edu.mmr.service.serviceInterfaces.report.ReportEntityFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;
    private final ReportEntityFactory entityFactory;

    public ReportController(ReportService reportService,
                            UserRepository userRepository,
                            ReportEntityFactory entityFactory) {
        this.reportService = reportService;
        this.userRepository = userRepository;
        this.entityFactory = entityFactory;
    }

    // Create a new report with entity validation
    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody ReportRequest request, Authentication auth) {
        try {
            // Validate reporter exists
            UserEntity reporter = userRepository.findById(request.getReporterId())
                    .orElseThrow(() -> new IllegalArgumentException("Reporter not found"));

            // Validate the entity type is supported
            entityFactory.getHandler(request.getItemType());

            // Create the report
            ReportEntity report = reportService.createReport(
                    request.getReportedID(),
                    request.getItemType(),
                    request.getStatus(),
                    auth


            );

            return new ResponseEntity<>(report, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error creating report", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get a report by its id with the reported entity details
    @GetMapping("/{id}")
    public ResponseEntity<?> getReportWithDetails(@PathVariable long id,Authentication auth) {
        try {
            ReportEntity report = reportService.getReportById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Report not found"));

            Object reportedEntity = reportService.getReportedEntity(id);

            Map<String, Object> response = new HashMap<>();
            response.put("report", report);
            response.put("reportedEntity", reportedEntity);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving report", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all reports by reporter (using reporter id)
    @GetMapping("/reporter/{reporterId}")
    public ResponseEntity<?> getReportsByReporter(@PathVariable long reporterId,Authentication auth) {
        try {
            UserEntity reporter = userRepository.findById(reporterId)
                    .orElseThrow(() -> new IllegalArgumentException("Reporter not found"));

            List<ReportEntity> reports = reportService.getReportsByReporter(reporter);
            return new ResponseEntity<>(reports, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving reports", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all reports by item type with entity validation
    @GetMapping("/itemType/{itemType}")
    public ResponseEntity<?> getReportsByItemType(@PathVariable String itemType,Authentication auth) {
        try {
            // Validate the item type is supported
            entityFactory.getHandler(itemType);

            List<ReportEntity> reports = reportService.getReportsByItemType(itemType);
            return new ResponseEntity<>(reports, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving reports", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update report status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateReportStatus(@PathVariable long id, @RequestParam String status,Authentication auth) {
        try {
            ReportEntity updatedReport = reportService.updateReportStatus(id, status,auth)
                    .orElseThrow(() -> new IllegalArgumentException("Report not found"));

            return new ResponseEntity<>(updatedReport, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating report status", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete a report
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable long id,Authentication auth) {
        try {
            if (!reportService.getReportById(id).isPresent()) {
                throw new IllegalArgumentException("Report not found");
            }

            reportService.deleteReport(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting report", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // New endpoint to get the reported entity directly
    @GetMapping("/{id}/entity")
    public ResponseEntity<?> getReportedEntity(@PathVariable long id,Authentication auth) {
        try {
            ReportEntity report = reportService.getReportById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Report not found"));



            Object entity = reportService.getReportedEntity(id);
            return new ResponseEntity<>(entity, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving reported entity", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
