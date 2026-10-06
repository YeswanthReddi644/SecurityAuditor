package com.securityauditor.repository.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.securityauditor.repository.dto.GitHubRepositoryResponse;
import com.securityauditor.repository.exception.RepositoryNotFoundException;

@Service
public class GitHubService {

    private final RestClient restClient;

    public GitHubService() {

        this.restClient = RestClient
                .builder()
                .baseUrl("https://api.github.com")
                .build();
    }

    public GitHubRepositoryResponse getRepository(
            String owner,
            String repo) {

        try {

            GitHubApiResponse response = restClient
                    .get()
                    .uri("/repos/{owner}/{repo}", owner, repo)
                    .retrieve()
                    .body(GitHubApiResponse.class);

            return new GitHubRepositoryResponse(
                    response.getName(),
                    response.getOwner().getLogin()
            );

        } catch (HttpClientErrorException.NotFound exception) {

            throw new RepositoryNotFoundException(
                    "GitHub repository not found"
            );
        }
    }

    private static class GitHubApiResponse {

        private String name;
        private GitHubOwner owner;

        public String getName() {
            return name;
        }

        public GitHubOwner getOwner() {
            return owner;
        }
    }

    private static class GitHubOwner {

        private String login;

        public String getLogin() {
            return login;
        }
    }
}