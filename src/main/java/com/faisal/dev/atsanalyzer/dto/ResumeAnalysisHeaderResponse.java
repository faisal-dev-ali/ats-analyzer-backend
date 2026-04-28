package com.faisal.dev.atsanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResumeAnalysisHeaderResponse(
        String fileName,
        LocalDateTime uploadedAt,
        LocalDateTime processedAt,
        Integer totalPages,
        Integer totalWords,
        Integer totalCharacters,
        String processingStatus,
        Double overallScore,
        Double universalScore,
        Double domainRelevanceScore,
        String detectedRole,
        String seniority,
        Integer experienceYears,
        String overallStatus,
        DashboardColor overallColor,
        String summary
) {
}
