
package com.securityauditor.github;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;

@Service
public class GitHubSourceDownloadService {

    private final GitHubApiService gitHubApiService;

    private static final long MAX_FILE_SIZE = 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        ".java", ".py", ".cpp", ".c", ".h", ".hpp",
        ".js", ".jsx", ".ts", ".tsx", ".html", ".css",
        ".xml", ".json", ".yml", ".yaml", ".properties",
        ".sql", ".sh", ".go", ".php", ".rb"
    );

    private static final Set<String> EXCLUDED_DIRECTORIES = Set.of(
        ".git", "node_modules", "target", "build",
        "dist", "vendor", ".idea"
    );

    public GitHubSourceDownloadService(
            GitHubApiService gitHubApiService) {
        this.gitHubApiService = gitHubApiService;
    }

    public int downloadSourceFiles(
            String owner,
            String repo,
            Path destination)
            throws IOException, InterruptedException {

        JsonNode repositoryFiles =
            gitHubApiService.getRepositoryFiles(owner, repo);

        if (repositoryFiles.path("truncated").asBoolean()) {
            throw new IllegalStateException(
                "GitHub returned an incomplete file tree. "
                + "A complete tree is required before scanning."
            );
        }

        JsonNode files = repositoryFiles.path("files");
        int downloadedCount = 0;

        Path root = destination.toAbsolutePath().normalize();
        Files.createDirectories(root);

        for (JsonNode file : files) {

            String relativePath = file.path("path").asText();
            String blobSha = file.path("sha").asText();
            long size = file.path("size").asLong();

            if (!isEligible(relativePath, size)) {
                continue;
            }

            Path output = root.resolve(relativePath).normalize();

            // Prevent paths from escaping the destination directory.
            if (!output.startsWith(root)) {
                continue;
            }

            Files.createDirectories(output.getParent());

            String content = gitHubApiService.downloadTextFile(
                owner, repo, blobSha, size
            );

            Files.writeString(
                output,
                content,
                StandardCharsets.UTF_8
            );

            downloadedCount++;
        }

        return downloadedCount;
    }

    private boolean isEligible(String relativePath, long size) {

        if (relativePath == null || relativePath.isBlank()) {
            return false;
        }

        if (size < 0 || size > MAX_FILE_SIZE) {
            return false;
        }

        String normalizedPath = relativePath.replace('\\', '/');

        for (String part : normalizedPath.split("/")) {
            if (EXCLUDED_DIRECTORIES.contains(part)) {
                return false;
            }
        }

        String lowerPath = normalizedPath.toLowerCase();

        for (String extension : ALLOWED_EXTENSIONS) {
            if (lowerPath.endsWith(extension)) {
                return true;
            }
        }

        return false;
    }
}
