    package cit.edu.mmr.controller;

    import cit.edu.mmr.dto.ReportRequest;
    import cit.edu.mmr.entity.ReportEntity;
    import cit.edu.mmr.entity.UserEntity;
    import cit.edu.mmr.repository.UserRepository;
    import cit.edu.mmr.service.ReportService;
    import cit.edu.mmr.service.serviceInterfaces.report.ReportEntityFactory;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.security.core.Authentication;
    import org.springframework.web.bind.annotation.*;

    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.web.bind.annotation.*;

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

        @PostMapping
        public ResponseEntity<ReportEntity> createReport(@RequestBody ReportRequest request, Authentication auth) {
            // Validation: throws if not found

            // Validate itemType handler exists (throws if invalid)
            entityFactory.getHandler(request.getItemType());

            ReportEntity report = reportService.createReport(
                    request.getReportedID(),
                    request.getItemType(),
                    request.getStatus(),
                    auth
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(report);
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<Map<String, Object>> getReportWithDetails(@PathVariable long id) {
            ReportEntity report = reportService.getReportById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Report not found"));

            Object reportedEntity = reportService.getReportedEntity(id);

            Map<String, Object> response = new HashMap<>();
            response.put("report", report);
            response.put("reportedEntity", reportedEntity);

            return ResponseEntity.ok(response);
        }

        @GetMapping("/reporter/{reporterId}")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<List<ReportEntity>> getReportsByReporter(@PathVariable long reporterId) {
            UserEntity reporter = userRepository.findById(reporterId)
                    .orElseThrow(() -> new IllegalArgumentException("Reporter not found"));

            return ResponseEntity.ok(reportService.getReportsByReporter(reporter));
        }

        @GetMapping("/getAll")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public List<ReportEntity> getAllReports(Authentication auth) {
            return reportService.getReports(auth);
        }

        @GetMapping("/itemType/{itemType}")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<List<ReportEntity>> getReportsByItemType(@PathVariable String itemType) {
            entityFactory.getHandler(itemType); // validate itemType
            return ResponseEntity.ok(reportService.getReportsByItemType(itemType));
        }

        @PutMapping("/{id}/status")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ReportEntity> updateReportStatus(@PathVariable long id, @RequestParam String status, Authentication auth) {
            ReportEntity updatedReport = reportService.updateReportStatus(id, status, auth)
                    .orElseThrow(() -> new IllegalArgumentException("Report not found"));

            return ResponseEntity.ok(updatedReport);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteReport(@PathVariable long id) {
            reportService.deleteReport(id);
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/{id}/entity")
        public ResponseEntity<Object> getReportedEntity(@PathVariable long id) {
            reportService.getReportById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Report not found"));

            Object entity = reportService.getReportedEntity(id);
            return ResponseEntity.ok(entity);
        }
    }
