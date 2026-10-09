
package com.securityauditor.scan.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

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

    public ScanService(
            ScanRepository scanRepository,
            GitRepositoryRepository gitRepositoryRepository) {

        this.scanRepository = scanRepository;
        this.gitRepositoryRepository = gitRepositoryRepository;
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
