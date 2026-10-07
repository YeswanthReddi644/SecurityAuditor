package com.securityauditor.scan.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.securityauditor.repository.entity.GitRepository;
import com.securityauditor.repository.repository.GitRepositoryRepository;
import com.securityauditor.scan.dto.CreateScanRequest;
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

    public Scan createScan(CreateScanRequest request) {

        // 1. Find the repository
        GitRepository repository =
                gitRepositoryRepository
                        .findById(request.getRepositoryId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Repository not found"));

        // 2. Create a new scan
        Scan scan = new Scan(
                ScanStatus.QUEUED,
                LocalDateTime.now(),
                repository
        );

        // 3. Save scan in PostgreSQL
        return scanRepository.save(scan);
    }
}