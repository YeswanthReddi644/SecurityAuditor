
package com.securityauditor.github;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/github")
public class GitHubTestController {

    private final GitHubApiService gitHubApiService;
    private final GitHubSourceDownloadService sourceDownloadService;

    public GitHubTestController(
            GitHubApiService gitHubApiService,
            GitHubSourceDownloadService sourceDownloadService) {

        this.gitHubApiService = gitHubApiService;
        this.sourceDownloadService = sourceDownloadService;
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
            "DSA-PRACTICE"
        );

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
                size
            );

            return ResponseEntity.ok(content);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }
    
    @GetMapping("/test-download")
    public ResponseEntity<String> testDownload()
            throws IOException, InterruptedException {

        Path destination = Paths.get(
            System.getProperty("java.io.tmpdir"),
            "security-auditor-test",
            "DSA-PRACTICE"
        );

        int count = sourceDownloadService.downloadSourceFiles(
            "YeswanthReddi644",
            "DSA-PRACTICE",
            destination
        );

        return ResponseEntity.ok(
            "Download completed successfully.\n"
            + "Files downloaded: " + count + "\n"
            + "Directory: " + destination.toAbsolutePath()
        );
    }

    
  
}
