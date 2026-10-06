package com.securityauditor.repository.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.securityauditor.repository.dto.CreateRepositoryRequest;
import com.securityauditor.repository.dto.GitHubRepositoryResponse;
import com.securityauditor.repository.dto.GitRepositoryResponse;
import com.securityauditor.repository.entity.GitRepository;
import com.securityauditor.repository.repository.GitRepositoryRepository;
import com.securityauditor.user.entity.User;
import com.securityauditor.user.repository.UserRepository;

@Service
public class RepositoryService {

    private final GitRepositoryRepository gitRepositoryRepository;
    private final UserRepository userRepository;
    private final GitHubService gitHubService;

    public RepositoryService(
            GitRepositoryRepository gitRepositoryRepository,
            UserRepository userRepository,
            GitHubService gitHubService) {

        this.gitRepositoryRepository = gitRepositoryRepository;
        this.userRepository = userRepository;
        this.gitHubService = gitHubService;
    }

    public GitRepositoryResponse createRepository(
            CreateRepositoryRequest request,
            String email) {

        // 1. Find the currently logged-in user
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // 2. Get repository URL from request
        String url = request.getUrl();

        // 3. Validate GitHub URL
        if (!isValidGitHubUrl(url)) {
            throw new IllegalArgumentException(
                    "Invalid GitHub repository URL");
        }

        // 4. Remove trailing slash if present
        String cleanUrl = url.endsWith("/")
                ? url.substring(0, url.length() - 1)
                : url;

        // 5. Split GitHub URL
        String[] parts = cleanUrl.split("/");

        String owner = parts[parts.length - 2];
        String repo = parts[parts.length - 1];

        // 6. Remove .git from repository name if present
        if (repo.endsWith(".git")) {
            repo = repo.substring(0, repo.length() - 4);
        }

        // 7. Call GitHub API
        GitHubRepositoryResponse githubRepository =
                gitHubService.getRepository(owner, repo);

        // 8. Create database entity
        GitRepository repository = new GitRepository(
                githubRepository.getName(),
                url,
                githubRepository.getOwner(),
                LocalDateTime.now(),
                user
        );

        // 9. Save repository in PostgreSQL
        GitRepository savedRepository =
                gitRepositoryRepository.save(repository);

        // 10. Convert entity to response DTO
        return new GitRepositoryResponse(
                savedRepository.getId(),
                savedRepository.getName(),
                savedRepository.getUrl(),
                savedRepository.getOwner(),
                savedRepository.getCreatedAt()
        );
    }

    // Validates GitHub repository URL
    private boolean isValidGitHubUrl(String url) {

        return url != null
                && url.matches(
                    "^https://github\\.com/[^/]+/[^/]+(?:\\.git)?/?$"
                );
    }
}