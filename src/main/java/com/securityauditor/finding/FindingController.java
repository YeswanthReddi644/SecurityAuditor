
package com.securityauditor.finding;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securityauditor.finding.dto.FindingResponse;

@RestController
@RequestMapping("/api/v1/scans")
public class FindingController {

    private final FindingService findingService;

    public FindingController(FindingService findingService) {
        this.findingService = findingService;
    }

    @GetMapping("/{scanId}/findings")
    public ResponseEntity<List<FindingResponse>> getFindings(
            @PathVariable Long scanId) {

        List<FindingResponse> findings =
                findingService.getFindingsByScanId(scanId);

        return ResponseEntity.ok(findings);
    }
}
