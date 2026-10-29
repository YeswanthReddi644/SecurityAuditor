package com.securityauditor.finding;

//package com.securityauditor.finding.entity;

import java.time.LocalDateTime;

import com.securityauditor.scan.entity.Scan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "findings")
public class Finding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String ruleId;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false, length = 1000)
    private String filePath;

    private Integer startLine;

    private Integer endLine;

    @Column(length = 50)
    private String severity;

    @Column(length = 100)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String aiExplanation;

@Column(length = 1000)
private String cwe;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scan_id", nullable = false)
    private Scan scan;
    

    public Finding() {
    }

    public Finding(
            String ruleId,
            String message,
            String filePath,
            Integer startLine,
            Integer endLine,
            String severity,
            String category,
            String cwe,
            LocalDateTime createdAt,
            Scan scan) {

        this.ruleId = ruleId;
        this.message = message;
        this.filePath = filePath;
        this.startLine = startLine;
        this.endLine = endLine;
        this.severity = severity;
        this.category = category;
        this.cwe = cwe;
        this.createdAt = createdAt;
        this.scan = scan;
    }

    public Long getId() {
        return id;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getMessage() {
        return message;
    }

    public String getFilePath() {
        return filePath;
    }

    public Integer getStartLine() {
        return startLine;
    }

    public Integer getEndLine() {
        return endLine;
    }

    public String getSeverity() {
        return severity;
    }

    public String getCategory() {
        return category;
    }

    public String getCwe() {
        return cwe;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Scan getScan() {
        return scan;
    }
    public String getAiExplanation() {
        return aiExplanation;
    }
    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
    }
}

