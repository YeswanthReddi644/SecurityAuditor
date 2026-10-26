package com.securityauditor.scan.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securityauditor.scan.dto.CreateScanRequest;
import com.securityauditor.scan.dto.ScanResponse;
import com.securityauditor.scan.service.ScanService;

import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/v1/scans")
public class ScanController {

    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @PostMapping
    public ResponseEntity<ScanResponse> createScan(
            @Valid @RequestBody CreateScanRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        ScanResponse response =
                scanService.createScan(request, userEmail);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


@GetMapping("/{scanId}/process")
public ResponseEntity<String> processScan(
        @org.springframework.web.bind.annotation.PathVariable Long scanId) {

    try {
        int findingsCount =
                scanService.processCompletedScan(scanId);

        return ResponseEntity.ok(
                "Scan processed successfully.\n"
                        + "Scan ID: " + scanId + "\n"
                        + "Findings saved: " + findingsCount
        );

    } catch (IllegalStateException e) {
        throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.CONFLICT,
                e.getMessage()
        );

    } catch (IllegalArgumentException e) {
        throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.NOT_FOUND,
                e.getMessage()
        );

    } catch (Exception e) {
        throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to process scan."
        );
    }
}

}