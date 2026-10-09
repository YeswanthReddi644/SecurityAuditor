package com.securityauditor.scan.dto;

import java.time.LocalDateTime;

import com.securityauditor.scan.entity.ScanStatus;

public class ScanResponse {

    private Long id;
    private ScanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long repositoryId;
    private String repositoryName;

    public ScanResponse(
            Long id,
            ScanStatus status,
            LocalDateTime createdAt,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            Long repositoryId,
            String repositoryName) {

        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.repositoryId = repositoryId;
        this.repositoryName = repositoryName;
    }

    public Long getId() {
        return id;
    }

    public ScanStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public Long getRepositoryId() {
        return repositoryId;
    }

    public String getRepositoryName() {
        return repositoryName;
    }
}