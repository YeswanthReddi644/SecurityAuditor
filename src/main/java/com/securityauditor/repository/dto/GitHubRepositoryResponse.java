package com.securityauditor.repository.dto;

public class GitHubRepositoryResponse {

    private String name;
    private String owner;

    public GitHubRepositoryResponse(
            String name,
            String owner) {

        this.name = name;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }
}