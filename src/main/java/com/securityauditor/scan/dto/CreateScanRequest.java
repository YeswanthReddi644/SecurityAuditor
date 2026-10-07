package com.securityauditor.scan.dto;

import jakarta.validation.constraints.NotNull;

public class CreateScanRequest {

    @NotNull
    private Long repositoryId;

    public Long getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Long repositoryId) {
        this.repositoryId = repositoryId;
    }
}