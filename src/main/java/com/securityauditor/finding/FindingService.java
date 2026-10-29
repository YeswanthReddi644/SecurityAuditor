package com.securityauditor.finding;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securityauditor.finding.dto.FindingResponse;
import com.securityauditor.scan.entity.Scan;
import com.securityauditor.scan.repository.ScanRepository;
import com.securityauditor.service.GroqService;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class FindingService {

    private final FindingRepository findingRepository;
    private final ScanRepository scanRepository;
    private final GroqService groqService;

    private final ObjectMapper mapper = new ObjectMapper();

    public FindingService(
            FindingRepository findingRepository,
            ScanRepository scanRepository,
            GroqService groqService) {

        this.findingRepository = findingRepository;
        this.scanRepository = scanRepository;
        this.groqService = groqService;
    }

    @Transactional
    public List<Finding> saveSemgrepResults(
            Long scanId,
            String jsonReport) throws Exception {

        Scan scan = scanRepository.findById(scanId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Scan not found: " + scanId));

        JsonNode root = mapper.readTree(jsonReport);
        JsonNode results = root.path("results");

        if (!results.isArray()) {
            throw new IllegalArgumentException(
                    "Invalid Semgrep report: results must be an array");
        }

        List<Finding> findings = new ArrayList<>();

        for (JsonNode result : results) {

            JsonNode extra = result.path("extra");
            JsonNode metadata = extra.path("metadata");

            String ruleId = result.path("check_id").asText();
            String message = extra.path("message").asText();
            String filePath = result.path("path").asText();

            int startLine = result.path("start").path("line").asInt();
            int endLine = result.path("end").path("line").asInt();

            String severity = extra.path("severity").asText();
            String category = metadata.path("category").asText();

            List<String> cweValues = new ArrayList<>();

            JsonNode cweNode = metadata.path("cwe");

            if (cweNode.isArray()) {

                for (JsonNode cwe : cweNode) {
                    cweValues.add(cwe.asText());
                }

            } else if (cweNode.isTextual()) {

                cweValues.add(cweNode.asText());
            }

            Finding finding = new Finding(
                    ruleId,
                    message,
                    filePath,
                    startLine,
                    endLine,
                    severity,
                    category,
                    String.join(", ", cweValues),
                    LocalDateTime.now(),
                    scan);

            // Create prompt for Groq AI
            String prompt = """
                    Explain this security vulnerability in simple terms.

                    Rule ID: %s
                    Message: %s
                    Severity: %s
                    Category: %s
                    CWE: %s
                    File: %s
                    Line: %d

                    Include:
                    1. What the vulnerability means.
                    2. Why it is dangerous.
                    3. How to fix it.
                    """.formatted(
                            ruleId,
                            message,
                            severity,
                            category,
                            String.join(", ", cweValues),
                            filePath,
                            startLine
                    );

            // Generate AI explanation using Groq
            String explanation =
                    groqService.generateExplanation(prompt);

            // Store AI explanation in Finding entity
            finding.setAiExplanation(explanation);

            findings.add(finding);
        }

        // Save findings and AI explanations in PostgreSQL
        return findingRepository.saveAll(findings);
    }

    @Transactional(readOnly = true)
    public List<FindingResponse> getFindingsByScanId(Long scanId) {

        List<Finding> findings =
                findingRepository.findByScanId(scanId);

        return findings.stream()
                .map(finding -> new FindingResponse(
                        finding.getId(),
                        finding.getScan().getId(),
                        finding.getRuleId(),
                        finding.getMessage(),
                        finding.getFilePath(),
                        finding.getStartLine(),
                        finding.getEndLine(),
                        finding.getSeverity(),
                        finding.getCategory(),
                        finding.getCwe(),
                        finding.getCreatedAt(),
                        finding.getAiExplanation()
                ))
                .collect(Collectors.toList());
    }
}