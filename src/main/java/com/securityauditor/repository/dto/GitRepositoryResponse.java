package com.securityauditor.repository.dto;

import java.time.LocalDateTime;

public class GitRepositoryResponse {

    private Long id;
    private String name;
    private String url;
    private String owner;
    private LocalDateTime createdAt;

    public GitRepositoryResponse(
            Long id,
            String name,
            String url,
            String owner,
            LocalDateTime createdAt) {

        this.id = id;
        this.name = name;
        this.url = url;
        this.owner = owner;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getOwner() {
        return owner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}