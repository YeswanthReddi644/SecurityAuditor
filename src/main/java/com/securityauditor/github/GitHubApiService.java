
package com.securityauditor.github;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;


import java.util.Base64;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class GitHubApiService {

    private final GitHubAppTokenService tokenService;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public GitHubApiService(GitHubAppTokenService tokenService) {
        this.tokenService = tokenService;
    }

    private JsonNode githubGet(String url, String token)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/vnd.github+json")
            .header("Authorization", "Bearer " + token)
            .header("X-GitHub-Api-Version", "2022-11-28")
            .GET()
            .build();

        HttpResponse<String> response = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                "GitHub API request failed with HTTP "
                + response.statusCode()
            );
        }

        return mapper.readTree(response.body());
    }

    public JsonNode getRepository(String owner, String repo)
            throws IOException, InterruptedException {

        String token = tokenService.createInstallationToken();

        JsonNode json = githubGet(
            "https://api.github.com/repos/" + owner + "/" + repo,
            token
        );

        var result = mapper.createObjectNode();
        result.put("name", json.path("name").asText());
        result.put("full_name", json.path("full_name").asText());
        result.put("private", json.path("private").asBoolean());
        result.put("html_url", json.path("html_url").asText());
        result.put("default_branch",
                   json.path("default_branch").asText());

        return result;
    }

    public JsonNode getRepositoryFiles(String owner, String repo)
            throws IOException, InterruptedException {

        String token = tokenService.createInstallationToken();

        String baseUrl =
            "https://api.github.com/repos/" + owner + "/" + repo;

        // 1. Get repository information and default branch
        JsonNode repository = githubGet(baseUrl, token);

        String branch = repository.path("default_branch").asText();

        if (branch.isBlank()) {
            throw new IllegalStateException(
                "Could not determine the default branch"
            );
        }

        // 2. Get the latest commit SHA for that branch
        String encodedBranch = URLEncoder
            .encode(branch, StandardCharsets.UTF_8)
            .replace("+", "%20");

        JsonNode branchData = githubGet(
            baseUrl + "/branches/" + encodedBranch,
            token
        );

        String commitSha = branchData
            .path("commit")
            .path("sha")
            .asText();

        if (commitSha.isBlank()) {
            throw new IllegalStateException(
                "Could not determine the latest commit SHA"
            );
        }

        // 3. Get the complete file tree for that commit
        JsonNode tree = githubGet(
            baseUrl + "/git/trees/" + commitSha + "?recursive=1",
            token
        );

        // 4. Return useful file information
        var result = mapper.createObjectNode();
        result.put("repository", repository.path("full_name").asText());
        result.put("branch", branch);
        result.put("commit_sha", commitSha);
        result.put("truncated", tree.path("truncated").asBoolean());

        var files = mapper.createArrayNode();
        JsonNode treeItems = tree.path("tree");

        for (JsonNode item : treeItems) {
            if ("blob".equals(item.path("type").asText())) {
                var file = mapper.createObjectNode();
                file.put("path", item.path("path").asText());
                file.put("size", item.path("size").asLong());
                file.put("sha", item.path("sha").asText());
                files.add(file);
            }
        }

        result.set("files", files);

        return result;
    }
    
   
    public String downloadTextFile(
            String owner,
            String repo,
            String blobSha,
            long size)
            throws IOException, InterruptedException {

        long maxFileSize = 1024 * 1024; // 1 MB limit

        if (size < 0 || size > maxFileSize) {
            throw new IllegalArgumentException(
                "File exceeds the allowed 1 MB size limit");
        }

        if (blobSha == null ||
                !blobSha.matches("[a-fA-F0-9]{40,64}")) {
            throw new IllegalArgumentException("Invalid Git blob SHA");
        }

        String token = tokenService.createInstallationToken();

        String url = "https://api.github.com/repos/"
                + owner + "/" + repo
                + "/git/blobs/" + blobSha;

        JsonNode blob = githubGet(url, token);

        if (!"base64".equalsIgnoreCase(
                blob.path("encoding").asText())) {
            throw new IllegalStateException(
                "GitHub did not return Base64-encoded content");
        }

        String encodedContent = blob.path("content").asText();

        byte[] decodedBytes = Base64.getMimeDecoder()
                .decode(encodedContent);

        if (decodedBytes.length > maxFileSize) {
            throw new IllegalStateException(
                "Downloaded file exceeds the 1 MB limit");
        }

        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
    
}
