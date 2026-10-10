
package com.securityauditor.github;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class GitHubAppTokenService {

    @Value("${github.app.id}")
    private String appId;

    @Value("${github.app.private-key-path}")
    private String privateKeyPath;

    @Value("${github.app.installation-id}")
    private String installationId;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    private PrivateKey loadPrivateKey() throws IOException {
        try (Reader reader =
                 Files.newBufferedReader(Path.of(privateKeyPath));
             PEMParser parser = new PEMParser(reader)) {

            Object pem = parser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            if (pem instanceof PEMKeyPair pair) {
                return converter.getKeyPair(pair).getPrivate();
            }

            if (pem instanceof PrivateKeyInfo info) {
                return converter.getPrivateKey(info);
            }

            throw new IllegalStateException(
                "Unsupported PEM private-key format");
        }
    }

    private String generateAppJwt() throws IOException {
        Instant now = Instant.now();

        return Jwts.builder()
            .issuer(appId)
            .issuedAt(Date.from(now.minusSeconds(60)))
            .expiration(Date.from(now.plusSeconds(540)))
            .signWith(loadPrivateKey(), Jwts.SIG.RS256)
            .compact();
    }

    public String createInstallationToken()
            throws IOException, InterruptedException {

        String jwt = generateAppJwt();

        String url =
            "https://api.github.com/app/installations/"
            + installationId + "/access_tokens";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/vnd.github+json")
            .header("Authorization", "Bearer " + jwt)
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{}"))
            .build();

        HttpResponse<String> response = client.send(
            request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            throw new IllegalStateException(
                "GitHub token request failed with HTTP "
                + response.statusCode()
                + ". Check App ID, installation ID, private key "
                + "and installation permissions.");
        }

        JsonNode json = mapper.readTree(response.body());
        JsonNode token = json.get("token");

        if (token == null || token.asText().isBlank()) {
            throw new IllegalStateException(
                "GitHub did not return an installation token");
        }

        return token.asText();
    }
}
