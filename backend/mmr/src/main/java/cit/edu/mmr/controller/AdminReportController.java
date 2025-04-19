

package cit.edu.mmr.controller;

import cit.edu.mmr.entity.ReportEntity;
import cit.edu.mmr.service.ReportResolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminReportController {

    private final ReportResolutionService reportResolutionService;

    @Autowired
    public AdminReportController(ReportResolutionService reportResolutionService) {
        this.reportResolutionService = reportResolutionService;
    }

    /**
     * Endpoint for resolving reports by administrators
     *
     * @param reportId ID of the report to resolve
     * @param resolutionRequest Object containing resolution decision
     * @param authentication Current authenticated user
     * @return Updated report entity
     */
    @PutMapping("/{reportId}/resolve")
    public ResponseEntity<?> resolveReport(
            @PathVariable long reportId,
            @RequestBody ResolutionRequest resolutionRequest,
            Authentication authentication) {

        try {
            ReportEntity resolvedReport = reportResolutionService.resolveReport(
                    reportId,
                    resolutionRequest.getResolution(),
                    authentication
            );

            return ResponseEntity.ok(resolvedReport);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error resolving report: " + e.getMessage());
        }
    }

    /**
     * Endpoint for retrieving all confiscated content
     *
     * @param authentication Current authenticated user
     * @return Response containing all confiscated time capsules and comments
     */
    @GetMapping("/confiscated-content")
    public ResponseEntity<?> getConfiscatedContent(Authentication authentication) {
        try {
            ReportResolutionService.ConfiscatedContentResponse confiscatedContent =
                    reportResolutionService.getConfiscatedContent(authentication);

            return ResponseEntity.ok(confiscatedContent);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving confiscated content: " + e.getMessage());
        }
    }

    /**
     * Request class for resolution decisions
     */
    public static class ResolutionRequest {
        private String resolution;

        public String getResolution() {
            return resolution;
        }

        public void setResolution(String resolution) {
            this.resolution = resolution;
        }
    }
}