
package com.securityauditor.github;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.securityauditor.finding.Finding;
import com.securityauditor.finding.FindingService;

import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/github")
public class GitHubTestController {

    private final GitHubApiService gitHubApiService;
    private final GitHubSourceDownloadService sourceDownloadService;
    private final FindingService findingService;

    public GitHubTestController(
            GitHubApiService gitHubApiService,
            GitHubSourceDownloadService sourceDownloadService,
            FindingService findingService) {

        this.gitHubApiService = gitHubApiService;
        this.sourceDownloadService = sourceDownloadService;
        this.findingService = findingService;
    }

    @GetMapping("/test-semgrep-report")
    public ResponseEntity<String> testSemgrepReport()
            throws IOException, InterruptedException {

        String report = gitHubApiService.downloadSemgrepReport(
                "YeswanthReddi644",
                "DSA-PRACTICE",
                38141622667L);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/test-repo")
    public ResponseEntity<JsonNode> testRepository()
            throws IOException, InterruptedException {

        JsonNode repository = gitHubApiService.getRepository(
                "YeswanthReddi644",
                "DSA-PRACTICE");

        return ResponseEntity.ok(repository);
    }

    @GetMapping("/test-files")
    public ResponseEntity<JsonNode> testRepositoryFiles()
            throws IOException, InterruptedException {

        JsonNode files = gitHubApiService.getRepositoryFiles(
                "YeswanthReddi644",
                "DSA-PRACTICE");

        return ResponseEntity.ok(files);
    }

    @GetMapping("/test-file")
    public ResponseEntity<String> testDownloadFile(
            @RequestParam String blobSha,
            @RequestParam long size)
            throws IOException, InterruptedException {

        try {
            String content = gitHubApiService.downloadTextFile(
                    "YeswanthReddi644",
                    "DSA-PRACTICE",
                    blobSha,
                    size);

            return ResponseEntity.ok(content);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage());
        }
    }

    @GetMapping("/test-download")
    public ResponseEntity<String> testDownload()
            throws IOException, InterruptedException {

        Path destination = Paths.get(
                System.getProperty("java.io.tmpdir"),
                "security-auditor-test",
                "DSA-PRACTICE");

        int count = sourceDownloadService.downloadSourceFiles(
                "YeswanthReddi644",
                "DSA-PRACTICE",
                destination);

        return ResponseEntity.ok(
                "Download completed successfully.\n"
                        + "Files downloaded: " + count + "\n"
                        + "Directory: " + destination.toAbsolutePath());
    }

    @GetMapping("/test-actions-permission")
    public ResponseEntity<JsonNode> testActionsPermission()
            throws IOException, InterruptedException {

        JsonNode permissions =
                gitHubApiService.testActionsPermission(
                        "YeswanthReddi644",
                        "DSA-PRACTICE");

        return ResponseEntity.ok(permissions);
    }

@GetMapping("/test-save-findings")
public ResponseEntity<String> testSaveFindings(
        @RequestParam Long scanId)
        throws IOException, InterruptedException {

    try {
        // 1. Download the Semgrep JSON report
    	System.out.println(">>> testSaveFindings endpoint reached");
        String report = gitHubApiService.downloadSemgrepReport(
                "YeswanthReddi644",
                "DSA-PRACTICE",
                38141622667L);

        // 2. Parse the report and save findings to PostgreSQL
        List<Finding> findings =
                findingService.saveSemgrepResults(scanId, report);

        // 3. Return the result
        return ResponseEntity.ok(
                "Semgrep report processed successfully.\n"
                        + "Scan ID: " + scanId + "\n"
                        + "Findings saved: " + findings.size());

    } catch (IllegalArgumentException e) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage());
    } catch (Exception e) {
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to save Semgrep findings");
    }
}

}
