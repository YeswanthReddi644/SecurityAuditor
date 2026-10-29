package com.securityauditor.finding.dto;

import java.time.LocalDateTime;

public record FindingResponse(

        Long id,

        Long scanId,

        String ruleId,

        String message,

        String filePath,

        Integer startLine,

        Integer endLine,

        String severity,

        String category,

        String cwe,

        LocalDateTime createdAt,

        String aiExplanation

) {

}