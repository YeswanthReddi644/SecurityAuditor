
package com.securityauditor.github;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class GitHubApiService {

    private final GitHubAppTokenService tokenService;
    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
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
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "GitHub API request failed with HTTP "
                    + response.statusCode() + ": " + response.body());
        }

        return mapper.readTree(response.body());
    }

    public JsonNode getRepository(String owner, String repo)
            throws IOException, InterruptedException {

        String token = tokenService.createInstallationToken();

        JsonNode json = githubGet(
                "https://api.github.com/repos/" + owner + "/" + repo,
                token);

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

        JsonNode repository = githubGet(baseUrl, token);
        String branch = repository.path("default_branch").asText();

        if (branch.isBlank()) {
            throw new IllegalStateException(
                    "Could not determine the default branch");
        }

        String encodedBranch = URLEncoder
                .encode(branch, StandardCharsets.UTF_8)
                .replace("+", "%20");

        JsonNode branchData = githubGet(
                baseUrl + "/branches/" + encodedBranch,
                token);

        String commitSha = branchData.path("commit")
                .path("sha").asText();

        if (commitSha.isBlank()) {
            throw new IllegalStateException(
                    "Could not determine the latest commit SHA");
        }

        JsonNode tree = githubGet(
                baseUrl + "/git/trees/" + commitSha + "?recursive=1",
                token);

        var result = mapper.createObjectNode();
        result.put("repository", repository.path("full_name").asText());
        result.put("branch", branch);
        result.put("commit_sha", commitSha);
        result.put("truncated", tree.path("truncated").asBoolean());

        var files = mapper.createArrayNode();

        for (JsonNode item : tree.path("tree")) {
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

        long maxFileSize = 1024L * 1024L;

        if (size < 0 || size > maxFileSize) {
            throw new IllegalArgumentException(
                    "File exceeds the allowed 1 MB size limit");
        }

        if (blobSha == null
                || !blobSha.matches("[a-fA-F0-9]{40,64}")) {
            throw new IllegalArgumentException("Invalid Git blob SHA");
        }

        String token = tokenService.createInstallationToken();

        String url = "https://api.github.com/repos/"
                + owner + "/" + repo + "/git/blobs/" + blobSha;

        JsonNode blob = githubGet(url, token);

        if (!"base64".equalsIgnoreCase(
                blob.path("encoding").asText())) {
            throw new IllegalStateException(
                    "GitHub did not return Base64-encoded content");
        }

        byte[] decodedBytes = Base64.getMimeDecoder()
                .decode(blob.path("content").asText());

        if (decodedBytes.length > maxFileSize) {
            throw new IllegalStateException(
                    "Downloaded file exceeds the 1 MB limit");
        }

        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    public JsonNode testActionsPermission(String owner, String repo)
            throws IOException, InterruptedException {

        String token = tokenService.createInstallationToken();

        String url = "https://api.github.com/repos/"
                + owner + "/" + repo + "/actions/permissions";

        return githubGet(url, token);
    }
    public String extractSemgrepReport(byte[] zipBytes)
            throws IOException {

        if (zipBytes == null || zipBytes.length == 0) {
            throw new IllegalArgumentException(
                    "Artifact ZIP cannot be empty");
        }

        try (ZipInputStream zip = new ZipInputStream(
                new ByteArrayInputStream(zipBytes))) {

            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {

                if ("semgrep-results.json".equals(entry.getName())
                        || entry.getName().endsWith(
                                "/semgrep-results.json")) {

                    ByteArrayOutputStream output =
                            new ByteArrayOutputStream();

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    int totalBytes = 0;
                    int maxSize = 5 * 1024 * 1024;

                    while ((bytesRead = zip.read(buffer)) != -1) {
                        totalBytes += bytesRead;

                        if (totalBytes > maxSize) {
                            throw new IOException(
                                    "Semgrep report exceeds 5 MB");
                        }

                        output.write(buffer, 0, bytesRead);
                    }

                    return output.toString(
                            StandardCharsets.UTF_8);
                }

                zip.closeEntry();
            }
        }

        throw new IOException(
                "semgrep-results.json was not found in the artifact");
    }

public String downloadSemgrepReport(
        String owner,
        String repo,
        long runId)
        throws IOException, InterruptedException {

    String token = tokenService.createInstallationToken();

    String artifactsUrl = "https://api.github.com/repos/"
            + owner + "/" + repo
            + "/actions/runs/" + runId + "/artifacts";

    JsonNode artifactsJson = githubGet(artifactsUrl, token);
    JsonNode artifacts = artifactsJson.path("artifacts");

    String downloadUrl = null;

    for (JsonNode artifact : artifacts) {
        if ("semgrep-results".equals(
                artifact.path("name").asText())) {

            if (artifact.path("expired").asBoolean()) {
                throw new IllegalStateException(
                        "Semgrep artifact has expired");
            }

            downloadUrl = artifact.path("archive_download_url")
                    .asText();

            break;
        }
    }

    if (downloadUrl == null || downloadUrl.isBlank()) {
        throw new IllegalStateException(
                "semgrep-results artifact not found for run "
                        + runId);
    }

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(downloadUrl))
            .header("Accept", "application/vnd.github+json")
            .header("Authorization", "Bearer " + token)
            .header("X-GitHub-Api-Version", "2022-11-28")
            .GET()
            .build();

    HttpResponse<byte[]> response = client.send(
            request,
            HttpResponse.BodyHandlers.ofByteArray());

    if (response.statusCode() != 200) {
        throw new IllegalStateException(
                "Artifact download failed with HTTP "
                        + response.statusCode());
    }

    return extractSemgrepReport(response.body());
}


    public JsonNode triggerSemgrepWorkflow(
            String owner,
            String repo,
            long scanId)
            throws IOException, InterruptedException {

        if (owner == null || owner.isBlank()
                || repo == null || repo.isBlank()
                || scanId <= 0) {
            throw new IllegalArgumentException(
                    "Owner, repository, and a valid scan ID are required");
        }

        String token = tokenService.createInstallationToken();

        String url = "https://api.github.com/repos/"
                + owner + "/" + repo
                + "/actions/workflows/semgrep.yml/dispatches";

        var inputs = Map.of(
                "scan_id", String.valueOf(scanId));

        var body = Map.of(
                "ref", "main",
                "inputs", inputs);

        String requestBody = mapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + token)
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204) {
            throw new IllegalStateException(
                    "GitHub workflow dispatch failed with HTTP "
                    + response.statusCode() + ": " + response.body());
        }

        var result = mapper.createObjectNode();
        result.put("status", "dispatched");
        result.put("scan_id", scanId);

        return result;
    }
}
