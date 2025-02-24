package cit.edu.mmr.controller;

import cit.edu.mmr.dto.ReportRequest;
import cit.edu.mmr.entity.ReportEntity;
import cit.edu.mmr.entity.UserEntity;
import cit.edu.mmr.repository.UserRepository;
import cit.edu.mmr.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    public ReportController(ReportService reportService, UserRepository userRepository) {
        this.reportService = reportService;
        this.userRepository = userRepository;
    }

    // Create a new report
    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody ReportRequest request) {
        UserEntity reporter = userRepository.findById(request.getReporterId()).orElse(null);
        if (reporter == null) {
            return new ResponseEntity<>("Reporter not found", HttpStatus.NOT_FOUND);
        }
        ReportEntity report = reportService.createReport(request.getReportedID(),
                request.getItemType(), reporter, request.getStatus());
        return new ResponseEntity<>(report, HttpStatus.CREATED);
    }

    // Get a report by its id
    @GetMapping("/{id}")
    public ResponseEntity<?> getReportById(@PathVariable long id) {
        return reportService.getReportById(id)
                .<ResponseEntity<?>>map(report -> new ResponseEntity<>(report, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>("Report not found", HttpStatus.NOT_FOUND));
    }

    // Get all reports by reporter (using reporter id)
    @GetMapping("/reporter/{reporterId}")
    public ResponseEntity<?> getReportsByReporter(@PathVariable long reporterId) {
        UserEntity reporter = userRepository.findById(reporterId).orElse(null);
        if (reporter == null) {
            return new ResponseEntity<>("Reporter not found", HttpStatus.NOT_FOUND);
        }
        List<ReportEntity> reports = reportService.getReportsByReporter(reporter);
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }

    // Get all reports by item type
    @GetMapping("/itemType/{itemType}")
    public ResponseEntity<?> getReportsByItemType(@PathVariable String itemType) {
        List<ReportEntity> reports = reportService.getReportsByItemType(itemType);
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }

    // Update report status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateReportStatus(@PathVariable long id, @RequestParam String status) {
        return reportService.updateReportStatus(id, status)
                .<ResponseEntity<?>>map(report -> new ResponseEntity<>(report, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>("Report not found", HttpStatus.NOT_FOUND));
    }

    // Delete a report
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable long id) {
        if (reportService.getReportById(id).isPresent()) {
            reportService.deleteReport(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>("Report not found", HttpStatus.NOT_FOUND);
    }
}
