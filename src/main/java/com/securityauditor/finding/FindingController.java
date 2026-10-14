package com.securityauditor.finding;




import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scans")
public class FindingController {

    private final FindingRepository findingRepository;

    public FindingController(
            FindingRepository findingRepository) {
        this.findingRepository = findingRepository;
    }

    @GetMapping("/{scanId}/findings")
    public ResponseEntity<List<Finding>> getFindings(
            @PathVariable Long scanId) {

        List<Finding> findings =
                findingRepository.findByScanId(scanId);

        return ResponseEntity.ok(findings);
    }
}
