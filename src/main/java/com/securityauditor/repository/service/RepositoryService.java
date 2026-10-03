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

        // 3. Remove trailing slash if present
        String cleanUrl = url.endsWith("/")
                ? url.substring(0, url.length() - 1)
                : url;

        // 4. Split GitHub URL
        String[] parts = cleanUrl.split("/");

        // Example:
        // https://github.com/spring-projects/spring-boot
        //
        // owner = spring-projects
        // repo  = spring-boot

        String owner = parts[parts.length - 2];
        String repo = parts[parts.length - 1];

        // 5. Call GitHub API
        GitHubRepositoryResponse githubRepository =
                gitHubService.getRepository(owner, repo);

        // 6. Create our database entity
        GitRepository repository = new GitRepository(
                githubRepository.getName(),
                url,
                githubRepository.getOwner(),
                LocalDateTime.now(),
                user
        );

        // 7. Save repository in PostgreSQL
        GitRepository savedRepository =
                gitRepositoryRepository.save(repository);

        // 8. Convert entity to response DTO
        return new GitRepositoryResponse(
                savedRepository.getId(),
                savedRepository.getName(),
                savedRepository.getUrl(),
                savedRepository.getOwner(),
                savedRepository.getCreatedAt()
        );
    }
}