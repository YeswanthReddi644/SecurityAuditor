
package com.securityauditor.scan.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.securityauditor.github.GitHubApiService;
import com.securityauditor.repository.entity.GitRepository;
import com.securityauditor.repository.repository.GitRepositoryRepository;
import com.securityauditor.scan.dto.CreateScanRequest;
import com.securityauditor.scan.dto.ScanResponse;
import com.securityauditor.scan.entity.Scan;
import com.securityauditor.scan.entity.ScanStatus;
import com.securityauditor.scan.repository.ScanRepository;

@Service
public class ScanService {

    private final ScanRepository scanRepository;
    private final GitRepositoryRepository gitRepositoryRepository;
    private final GitHubApiService gitHubApiService;

    public ScanService(
            ScanRepository scanRepository,
            GitRepositoryRepository gitRepositoryRepository,
            GitHubApiService gitHubApiService) {

        this.scanRepository = scanRepository;
        this.gitRepositoryRepository = gitRepositoryRepository;
        this.gitHubApiService = gitHubApiService;
    }

    public ScanResponse createScan(
            CreateScanRequest request,
            String userEmail) {

        GitRepository repository =
                gitRepositoryRepository
                        .findByIdAndUser_Email(
                                request.getRepositoryId(),
                                userEmail)
                        .orElseThrow(() ->
                                new RuntimeException("Repository not found"));

        Scan scan = new Scan(
                ScanStatus.QUEUED,
                LocalDateTime.now(),
                repository
        );

        Scan savedScan = scanRepository.save(scan);

        try {
            gitHubApiService.triggerSemgrepWorkflow(
                    repository.getOwner(),
                    repository.getName(),
                    savedScan.getId()
            );

            savedScan.setStatus(ScanStatus.RUNNING);
            savedScan.setStartedAt(LocalDateTime.now());

        } catch (Exception e) {
            e.printStackTrace();
            savedScan.setStatus(ScanStatus.FAILED);
        }

        savedScan = scanRepository.save(savedScan);

        return new ScanResponse(
                savedScan.getId(),
                savedScan.getStatus(),
                savedScan.getCreatedAt(),
                savedScan.getStartedAt(),
                savedScan.getCompletedAt(),
                savedScan.getRepository().getId(),
                savedScan.getRepository().getName()
        );
    }
}
